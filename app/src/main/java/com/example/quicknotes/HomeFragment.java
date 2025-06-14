package com.example.quicknotes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvNotes, noOfNotes;
    private ListView notesListView;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tvNotes = view.findViewById(R.id.tvNotes);
        noOfNotes = view.findViewById(R.id.Noofnotes);
        notesListView = view.findViewById(R.id.notesListView);

        notesList = new ArrayList<>();
        noteAdapter = new NoteAdapter(getContext(), notesList);
        notesListView.setAdapter(noteAdapter);

        notesListView.setOnItemClickListener((parent, view1, position, id) -> {
            Note selectedNote = notesList.get(position);
            // Use the HomeActivity to launch the NoteActivity
            if(getActivity() instanceof HomeActivity) {
                ((HomeActivity)getActivity()).openNoteActivity(selectedNote);
            }
        });

        loadNotes();
    }

    @Override
    public void onResume() {
        super.onResume();
        // onResume is a good place to refresh data
        refreshNotes();
    }

    public void loadNotes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("notes")
                .whereEqualTo("userId", userId) // The most important line for security
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        notesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Note note = document.toObject(Note.class);
                            note.setId(document.getId());
                            notesList.add(note);
                        }
                        noteAdapter.notifyDataSetChanged();
                        updateUI();
                    } else {
                        Log.e("HomeFragment", "Error loading notes: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load notes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (notesList.isEmpty()) {
            tvNotes.setText("No Notes");
            noOfNotes.setText("0");
            notesListView.setVisibility(View.GONE);
        } else {
            tvNotes.setText("My Notes");
            noOfNotes.setText(String.valueOf(notesList.size()));
            notesListView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshNotes() {
        if (isAdded() && getView() != null) {
            loadNotes();
        }
    }
}
