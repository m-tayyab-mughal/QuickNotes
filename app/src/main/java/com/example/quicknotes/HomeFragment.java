package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvNotes;
    private TextView noOfNotes; // Added for Noofnotes TextView
    private ListView notesListView;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;
    private NoteDatabaseHelper databaseHelper;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database helper
        databaseHelper = new NoteDatabaseHelper(requireContext());

        // Initialize views
        tvNotes = view.findViewById(R.id.tvNotes);
        noOfNotes = view.findViewById(R.id.Noofnotes); // Initialize Noofnotes TextView
        notesListView = view.findViewById(R.id.notesListView);

        // Load notes
        loadNotes();

        // Set item click listener for editing notes
        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note selectedNote = notesList.get(position);

                // Open NoteActivity to edit the selected note
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("note", selectedNote);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh notes when fragment becomes visible
        if (getView() != null) {
            loadNotes();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // This method is called when fragment visibility changes in ViewPager/TabLayout
        if (isVisibleToUser && isResumed() && getView() != null) {
            loadNotes();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // This method is called when fragment is shown/hidden
        if (!hidden && getView() != null) {
            loadNotes();
        }
    }

    public void loadNotes() {
        // Get notes from database
        notesList = databaseHelper.getAllNotes();

        // Always recreate the adapter to ensure proper display
        noteAdapter = new NoteAdapter(getContext(), notesList);
        notesListView.setAdapter(noteAdapter);

        // Show empty state message if no notes
        if (notesList.isEmpty()) {
            tvNotes.setText("No Notes");
            noOfNotes.setText("0"); // Set count to 0
            notesListView.setVisibility(View.GONE);
        } else {
            tvNotes.setText("My Notes"); // Display just "My Notes"
            noOfNotes.setText(String.valueOf(notesList.size())); // Set note count
            notesListView.setVisibility(View.VISIBLE);
        }
    }

    // Method to refresh notes from outside the fragment
    public void refreshNotes() {
        if (isAdded() && getView() != null) {
            loadNotes();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // No need to close database as NoteDatabaseHelper handles it
    }
}