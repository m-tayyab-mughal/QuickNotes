package com.example.quicknotes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat; // Is import ko add karein
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "note_reminder_channel";
    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_CONTENT = "notification_content";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NOTIFICATION_CONTENT);

        // Notification Channel banayein
        createNotificationChannel(context);

        // Notification banayein
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_note) // Yahan apna note icon dein
                .setContentTitle("Reminder: " + title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // === YEH ZAROORI CHECK HAI ===
        // Notification dikhaane se pehle permission check karein
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Agar permission hai, to hi notification dikhayein
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
        // Agar permission nahin hai, to kuch nahin hoga aur app crash hone se bach jayegi.
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Note Reminders";
            String description = "Channel for note reminder notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}