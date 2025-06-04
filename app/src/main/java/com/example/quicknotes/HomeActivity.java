package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvNotes;
    private PreferenceManager preferenceManager;
    private FloatingActionButton fabAdd;
    private ListView notesListView;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initialize preferences
        preferenceManager = new PreferenceManager(this);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        tvNotes = findViewById(R.id.tvNotes);
        fabAdd = findViewById(R.id.fabAdd);
        notesListView = findViewById(R.id.notesListView);

        // Load and display notes
        loadNotes();

        // Profile image click listener
        CardView profileCard = findViewById(R.id.profileCard);
        profileCard.setOnClickListener(v -> {
            // Navigate to ProfileActivity
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // FAB click listener for adding new notes
        fabAdd.setOnClickListener(v -> {
            // Open NoteActivity to create a new note
            Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
            startActivity(intent);
        });

        // Set item click listener for editing notes
        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note selectedNote = notesList.get(position);

                // Open NoteActivity to edit the selected note
                Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
                intent.putExtra("note", selectedNote);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is still logged in (in case they logged out from profile)
        if (!preferenceManager.isLoggedIn()) {
            // Navigate back to MainActivity
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Refresh the notes list
            loadNotes();
        }
    }

    private void loadNotes() {
        // Get notes from PreferenceManager
        notesList = preferenceManager.getNotes();

        if (noteAdapter == null) {
            // First time loading - create adapter
            noteAdapter = new NoteAdapter(this, notesList);
            notesListView.setAdapter(noteAdapter);
        } else {
            // Update existing adapter
            noteAdapter.updateNotes(notesList);
        }

        // Show empty state message if no notes
        if (notesList.isEmpty()) {
            tvNotes.setText("No Notes");
        } else {
            tvNotes.setText("Notes");
        }
    }
}