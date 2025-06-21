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
    private Long reminderTime = null;

    // --- NEW LOCATION FIELDS ---
    private Double latitude;
    private Double longitude;
    private String locationName;


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
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getLocationName() { return locationName; }


    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public void setSynced(boolean synced) { isSynced = synced; }
    public void setReminderTime(Long reminderTime) { this.reminderTime = reminderTime; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLocationName(String locationName) { this.locationName = locationName; }


    public String getFormattedDate() {
        Date dateToShow = (timestamp != null) ? timestamp : new Date(lastModified);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(dateToShow);
    }
}