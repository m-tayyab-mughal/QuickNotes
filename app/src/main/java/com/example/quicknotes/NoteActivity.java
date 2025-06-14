package com.example.quicknotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView; // ✅ FIX: ImageButton ki jagah ImageView import karein
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView backButton, saveButton, deleteButton;
    private TextView toolbarTitle;
    private PreferenceManager preferenceManager;
    private Note existingNote;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        preferenceManager = new PreferenceManager(this);

        // Check if we're editing an existing note
        if (getIntent().hasExtra("note")) {
            isEditMode = true;
            existingNote = (Note) getIntent().getSerializableExtra("note");

            if (existingNote != null) {
                // Populate fields with existing note data
                editTextTitle.setText(existingNote.getTitle());
                editTextContent.setText(existingNote.getContent());

                // Update UI for edit mode
                toolbarTitle.setText("Edit Note");
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());
        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && existingNote != null) {
            // Update existing note
            existingNote.setTitle(title);
            existingNote.setContent(content);
            existingNote.updateTimestamp();

            preferenceManager.updateNote(existingNote);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        } else {
            // Create new note
            // ✅ FIX: Note class ka sahi constructor istemal karein
            Note newNote = new Note(title, content);
            preferenceManager.addNote(newNote);
            Toast.makeText(this, "Note created", Toast.LENGTH_SHORT).show();
        }

        finish();
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
            preferenceManager.deleteNote(existingNote.getId());
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}