package com.example.quicknotes;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView backButton, saveButton, deleteButton;
    private TextView toolbarTitle;

    private CardView locationDetailsCard;
    private TextView locationDetailsText;

    private NoteRepository noteRepository;
    private AlertDialog loadingDialog;

    private Note existingNote;
    private boolean isEditMode = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteRepository = new NoteRepository(getApplication());
        initLoadingDialog();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        locationDetailsCard = findViewById(R.id.locationDetailsCard);
        locationDetailsText = findViewById(R.id.locationDetailsText);


        if (getIntent().hasExtra("note")) {
            isEditMode = true;
            existingNote = (Note) getIntent().getSerializableExtra("note");

            if (existingNote != null) {
                editTextTitle.setText(existingNote.getTitle());
                editTextContent.setText(existingNote.getContent());
                toolbarTitle.setText("Edit Note");
                deleteButton.setVisibility(View.VISIBLE);

                if (existingNote.getLatitude() != null && existingNote.getLongitude() != null) {
                    String locationName = existingNote.getLocationName() != null ? existingNote.getLocationName() : "Saved Location";
                    String details = String.format(Locale.US, "%s (Lat: %.2f, Lon: %.2f)",
                            locationName, existingNote.getLatitude(), existingNote.getLongitude());
                    locationDetailsText.setText(details);
                    locationDetailsCard.setVisibility(View.VISIBLE);
                } else {
                    locationDetailsCard.setVisibility(View.GONE);
                }
            }
        }

        backButton.setOnClickListener(v -> onBackPressed());
        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void initLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    // --- SAVE NOTE METHOD HAS BEEN FULLY UPDATED ---
    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Title and content cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog("Saving...");

        Note noteToSave;
        if (isEditMode && existingNote != null) {
            noteToSave = existingNote;
            noteToSave.setTitle(title);
            noteToSave.setContent(content);
        } else {
            noteToSave = new Note();
            noteToSave.setTitle(title);
            noteToSave.setContent(content);
        }

        // --- NEW: Check if device location setting is enabled ---
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Location is off. Saving note without location.", Toast.LENGTH_LONG).show();
            noteRepository.insertOrUpdateNote(noteToSave, null);
            hideLoadingDialog();
            finish();
            return; // Stop here if location is off
        }

        // Check for app-level permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted. Saving without location.", Toast.LENGTH_LONG).show();
            noteRepository.insertOrUpdateNote(noteToSave, null);
            hideLoadingDialog();
            finish();
            return; // Stop here if permissions are not granted
        }

        // --- NEW: Fetching CURRENT location instead of last known location ---
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    // This listener is called when a fresh location is successfully fetched.
                    // 'location' can still be null if a fix could not be obtained.
                    noteRepository.insertOrUpdateNote(noteToSave, location);
                    hideLoadingDialog();
                    Toast.makeText(this, "Note saved.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this, e -> {
                    Log.e("NoteActivity", "Failed to get current location.", e);
                    // If fetching the current location fails (e.g., no signal), save without location.
                    noteRepository.insertOrUpdateNote(noteToSave, null);
                    hideLoadingDialog();
                    Toast.makeText(this, "Note saved, but location could not be determined.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDialogDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            deleteNote();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteNote() {
        if (existingNote != null) {
            showLoadingDialog("Deleting...");
            noteRepository.deleteNote(existingNote);
            hideLoadingDialog();
            Toast.makeText(this, "Note deleted.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null) {
            loadingDialog.show();
            TextView tvMessage = loadingDialog.findViewById(R.id.tv_loading_message);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}