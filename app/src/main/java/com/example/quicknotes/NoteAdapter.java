package com.example.quicknotes;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {

    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private final NoteRepository noteRepository;

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
        this.noteRepository = new NoteRepository(((Activity) context).getApplication());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_item, parent, false);
        }

        Note currentNote = getItem(position);
        ImageView bellIcon = convertView.findViewById(R.id.bellIcon);

        // --- NEW: Get references to location UI elements ---
        LinearLayout locationContainer = convertView.findViewById(R.id.locationContainer);
        TextView noteLocationText = convertView.findViewById(R.id.noteLocationText);

        if (currentNote != null) {
            ((TextView) convertView.findViewById(R.id.noteTitleText)).setText(currentNote.getTitle());
            ((TextView) convertView.findViewById(R.id.notePreviewText)).setText(currentNote.getContent());
            ((TextView) convertView.findViewById(R.id.noteDateText)).setText(currentNote.getFormattedDate());

            // --- NEW: Logic to show or hide location ---
            if (currentNote.getLocationName() != null && !TextUtils.isEmpty(currentNote.getLocationName())) {
                noteLocationText.setText(currentNote.getLocationName());
                locationContainer.setVisibility(View.VISIBLE);
            } else {
                locationContainer.setVisibility(View.GONE);
            }

            updateBellIconColor(bellIcon, currentNote);
            bellIcon.setOnClickListener(v -> handleBellIconClick(currentNote, bellIcon));
        }

        return convertView;
    }

    private void updateBellIconColor(ImageView bellIcon, Note note) {
        if (note.getReminderTime() != null && note.getReminderTime() > System.currentTimeMillis()) {
            bellIcon.setColorFilter(Color.parseColor("#FFC107")); // Yellow for active reminder
        } else {
            bellIcon.setColorFilter(Color.parseColor("#FF5252")); // Red for no reminder or past
        }
    }

    private void handleBellIconClick(Note note, ImageView bellIcon) {
        if (note.getReminderTime() != null && note.getReminderTime() > System.currentTimeMillis()) {
            showCancelReminderDialog(note, bellIcon);
        } else {
            checkPermissionsAndShowDateTimePicker(note, bellIcon);
        }
    }

    private void showCancelReminderDialog(Note note, ImageView bellIcon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        TextView dialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogDelete);

        dialogTitle.setText("Cancel Reminder");
        dialogMessage.setText("Are you sure you want to cancel the reminder for this note?");
        btnConfirm.setText("Yes");

        if (btnConfirm instanceof MaterialButton) {
            ((MaterialButton) btnConfirm).setBackgroundColor(Color.parseColor("#4285F4"));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            cancelNotification(note, bellIcon);
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void checkPermissionsAndShowDateTimePicker(Note note, ImageView bellIcon) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Permission Required")
                        .setMessage("To set reminders, please allow the app to schedule exact alarms in your system settings.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:" + getContext().getPackageName()));
                            getContext().startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                return;
            }
        }
        showDateTimePicker(note, bellIcon);
    }

    private void showDateTimePicker(Note note, ImageView bellIcon) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                Calendar reminderTime = Calendar.getInstance();
                reminderTime.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                if (reminderTime.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleNotification(note, reminderTime.getTimeInMillis(), bellIcon);
                } else {
                    Toast.makeText(getContext(), "Please select a future time.", Toast.LENGTH_SHORT).show();
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleNotification(Note note, long timeInMillis, ImageView bellIcon) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION_TITLE, note.getTitle());
        intent.putExtra(NotificationReceiver.NOTIFICATION_CONTENT, note.getContent());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), note.getId().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            note.setReminderTime(timeInMillis);
            noteRepository.insertOrUpdateNote(note);
            updateBellIconColor(bellIcon, note);
            Toast.makeText(getContext(), "Reminder set!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Permission to schedule reminders was denied.", Toast.LENGTH_LONG).show();
        }
    }

    private void cancelNotification(Note note, ImageView bellIcon) {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), note.getId().hashCode(), intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        note.setReminderTime(null);
        noteRepository.insertOrUpdateNote(note);
        updateBellIconColor(bellIcon, note);
        Toast.makeText(getContext(), "Reminder canceled.", Toast.LENGTH_SHORT).show();
    }
}