package com.example.quicknotes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "deleted_notes")
public class DeletedNote {

    @PrimaryKey
    @NonNull
    private String id;

    public DeletedNote(@NonNull String id) {
        this.id = id;
    }

    // Getter
    @NonNull
    public String getId() {
        return id;
    }

    // Setter
    public void setId(@NonNull String id) {
        this.id = id;
    }
}