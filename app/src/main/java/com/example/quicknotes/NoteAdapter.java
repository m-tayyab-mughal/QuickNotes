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

// No changes were needed in this file. It works as-is.
public class NoteAdapter extends ArrayAdapter<Note> {
    private Context context;
    private List<Note> notes;
    private LayoutInflater inflater;

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.note_item, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.noteTitleText);
            holder.previewTextView = convertView.findViewById(R.id.notePreviewText);
            holder.dateTextView = convertView.findViewById(R.id.noteDateText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Note currentNote = getItem(position);
        if (currentNote != null) {
            holder.titleTextView.setText(currentNote.getTitle());

            String contentPreview = currentNote.getContent();
            if (contentPreview.length() > 50) {
                contentPreview = contentPreview.substring(0, 50) + "...";
            }
            holder.previewTextView.setText(contentPreview);
            holder.dateTextView.setText(currentNote.getFormattedDate());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView titleTextView;
        TextView previewTextView;
        TextView dateTextView;
    }
}
