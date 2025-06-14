package com.example.quicknotes;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG = "NoteActivity";

    private EditText editTextTitle, editTextContent;
    private ImageView backButton, saveButton, deleteButton;
    private TextView toolbarTitle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // --- CHANGE 1: Remove ProgressDialog, add AlertDialog ---
    private AlertDialog loadingDialog;

    private Note existingNote;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // --- CHANGE 2: Call the method to create our new dialog ---
        initLoadingDialog();

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        if (getIntent().hasExtra("note")) {
            isEditMode = true;
            existingNote = (Note) getIntent().getSerializableExtra("note");

            if (existingNote != null) {
                editTextTitle.setText(existingNote.getTitle());
                editTextContent.setText(existingNote.getContent());
                toolbarTitle.setText("Edit Note");
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        backButton.setOnClickListener(v -> onBackPressed());
        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    // --- CHANGE 3: This new method builds the dialog from your custom layout ---
    private void initLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            // This makes the dialog's background transparent so only the CardView is visible
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Title and content cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- CHANGE 4: Show the custom dialog with a "Saving..." message ---
        showLoadingDialog("Saving...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            hideLoadingDialog();
            Toast.makeText(this, "Error: User is not logged in.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot save note, user is null.");
            return;
        }
        String currentUserId = currentUser.getUid();

        Note noteToSave;

        if (isEditMode && existingNote != null) {
            noteToSave = existingNote;
            noteToSave.setTitle(title);
            noteToSave.setContent(content);
        } else {
            noteToSave = new Note(title, content);
        }
        noteToSave.setUserId(currentUserId);

        db.collection("notes").document(noteToSave.getId())
                .set(noteToSave)
                .addOnSuccessListener(aVoid -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving note to Firestore", e);
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote() {
        if (existingNote != null) {
            // --- CHANGE 5: Show the custom dialog for deleting too ---
            showLoadingDialog("Deleting...");
            db.collection("notes").document(existingNote.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        hideLoadingDialog();
                        Toast.makeText(this, "Note deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingDialog();
                        Toast.makeText(this, "Failed to delete note.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting note", e);
                    });
        }
    }

    // --- CHANGE 6: Helper methods to control the dialog ---
    private void showLoadingDialog(String message) {
        if (loadingDialog != null) {
            loadingDialog.show();
            // Find the TextView inside the dialog to set the message dynamically
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
