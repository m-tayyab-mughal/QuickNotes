package com.example.quicknotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvNotes, noOfNotes;
    private ListView notesListView;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;
    private NoteRepository noteRepository; // Repository ka istemal

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Repository initialize karein
        noteRepository = new NoteRepository(requireActivity().getApplication());

        tvNotes = view.findViewById(R.id.tvNotes);
        noOfNotes = view.findViewById(R.id.Noofnotes);
        notesListView = view.findViewById(R.id.notesListView);

        notesList = new ArrayList<>();
        noteAdapter = new NoteAdapter(getContext(), notesList);
        notesListView.setAdapter(noteAdapter);

        notesListView.setOnItemClickListener((parent, view1, position, id) -> {
            Note selectedNote = notesList.get(position);
            if(getActivity() instanceof HomeActivity) {
                ((HomeActivity)getActivity()).openNoteActivity(selectedNote);
            }
        });

        observeNotes(); // Notes ko observe karein
    }

    private void observeNotes() {
        // LiveData ko observe karein, jab bhi data badlega UI update ho jayegi
        noteRepository.getAllNotes().observe(getViewLifecycleOwner(), newNotes -> {
            notesList.clear();
            notesList.addAll(newNotes);
            noteAdapter.notifyDataSetChanged();
            updateUI();
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

    // refreshNotes ab zaroori nahi hai kyunke LiveData khud UI update karega
    public void refreshNotes() {
        // Yeh method ab istemal nahi hoga, lekin agar HomeActivity se call ho raha hai to ise rakhein.
    }
}