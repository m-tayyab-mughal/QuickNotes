package com.example.quicknotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {
    private Context context;
    private List<Note> notes;

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        }

        Note currentNote = notes.get(position);

        TextView titleTextView = listItem.findViewById(R.id.noteTitleText);
        TextView previewTextView = listItem.findViewById(R.id.notePreviewText);
        TextView dateTextView = listItem.findViewById(R.id.noteDateText);

        titleTextView.setText(currentNote.getTitle());

        // Create a preview of the content (first 50 characters)
        String contentPreview = currentNote.getContent();
        if (contentPreview.length() > 50) {
            contentPreview = contentPreview.substring(0, 50) + "...";
        }
        previewTextView.setText(contentPreview);

        dateTextView.setText(currentNote.getFormattedDate());

        return listItem;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes.clear();
        this.notes.addAll(newNotes);
        notifyDataSetChanged();
    }
}