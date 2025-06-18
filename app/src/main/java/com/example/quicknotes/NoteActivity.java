package com.example.quicknotes;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView backButton, saveButton, deleteButton;
    private TextView toolbarTitle;

    private NoteRepository noteRepository; // Repository ka istemal
    private AlertDialog loadingDialog;

    private Note existingNote;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        noteRepository = new NoteRepository(getApplication()); // Repository initialize karein

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

        noteRepository.insertOrUpdateNote(noteToSave);

        // Repository background mein kaam karega, hum UI foran band kar sakte hain
        hideLoadingDialog();
        Toast.makeText(this, "Note saved.", Toast.LENGTH_SHORT).show();
        finish();
    }

    // --- YEH METHOD UPDATE KIYA GAYA HAI ---
    // Ab yeh default dialog ki jagah custom dialog istemal karega
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