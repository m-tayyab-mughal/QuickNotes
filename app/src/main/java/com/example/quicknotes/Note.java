package com.example.quicknotes;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Note implements Serializable {

    private String id;
    private String title;
    private String content;
    private String userId; // User se link karne ke liye
    @ServerTimestamp
    private Date timestamp;

    // Firestore ke liye yeh khaali constructor zaroori hai
    public Note() {}

    // App mein naya note banane ke liye constructor
    public Note(String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
    }

    // Getters - Yeh Firestore ko data read karne ke liye chahiye
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getUserId() { return userId; }
    public Date getTimestamp() { return timestamp; }


    // Setters - Yeh Firestore ko data se object banane ke liye chahiye
    // YEH SAB SE ZAROORI HAI
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }


    // UI mein date display karne ke liye helper method
    public String getFormattedDate() {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(timestamp);
        }
        return "";
    }
}
