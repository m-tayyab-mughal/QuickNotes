package com.example.quicknotes;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction; // Sahi Transaction import (agar zaroorat ho)
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateNote(Note note);

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY lastModified DESC")
    LiveData<List<Note>> getAllNotes(String userId);

    @Query("SELECT * FROM notes WHERE isSynced = 0 AND userId = :userId")
    List<Note> getUnsyncedNotes(String userId);

    @Query("SELECT * FROM notes WHERE id = :noteId AND userId = :userId")
    Note getNoteById(String noteId, String userId);

    @Update
    void updateNote(Note note);

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteNoteById(String noteId);

    // Yeh naya method delete kiye gaye notes ko track karne ke liye hai
    @Query("INSERT INTO deleted_notes (id) VALUES (:noteId)")
    void addDeletedNoteId(String noteId);

    @Query("SELECT * FROM deleted_notes")
    List<DeletedNote> getDeletedNoteIds();

    // --- SAHI CODE YEH HAI ---
    // @Query.Transaction wali ghalat line hata di gayi hai.
    @Query("DELETE FROM deleted_notes WHERE id = :noteId")
    void removeDeletedNoteId(String noteId);
}