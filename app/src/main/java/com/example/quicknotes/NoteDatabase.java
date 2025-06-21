package com.example.quicknotes;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Note.class, DeletedNote.class}, version = 3, exportSchema = false) // <-- VERSION UPDATED TO 3
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {

    public abstract NoteDao noteDao();

    private static volatile NoteDatabase INSTANCE;

    static NoteDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NoteDatabase.class, "note_database")
                            .fallbackToDestructiveMigration() // Production app mein aam taur par migration strategies istemal hoti hain
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}