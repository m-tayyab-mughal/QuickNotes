package com.example.quicknotes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String content;
    private String userId;
    @ServerTimestamp
    private Date timestamp;
    private long lastModified;
    private boolean isSynced;

    // New field for reminder time
    private Long reminderTime = null;

    public Note() {
        // Required empty constructor
    }

    // Getters
    @NonNull
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getUserId() { return userId; }
    public Date getTimestamp() { return timestamp; }
    public long getLastModified() { return lastModified; }
    public boolean isSynced() { return isSynced; }
    public Long getReminderTime() { return reminderTime; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public void setSynced(boolean synced) { isSynced = synced; }
    public void setReminderTime(Long reminderTime) { this.reminderTime = reminderTime; }

    public String getFormattedDate() {
        Date dateToShow = (timestamp != null) ? timestamp : new Date(lastModified);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(dateToShow);
    }
}