package com.example.quicknotes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class PreferenceManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private NoteDatabaseHelper databaseHelper;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        databaseHelper = new NoteDatabaseHelper(context);
    }

    // Save user data during signup
    public void saveUserData(String name, String email, String password) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    // Set logged in status
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user email
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // Get user name
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    // Get user password (usually not recommended, but for this simple app it's okay)
    public String getUserPassword() {
        return sharedPreferences.getString(KEY_USER_PASSWORD, "");
    }

    // Update user profile information
    public void updateUserProfile(String name, String email, String password) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);

        // Only update password if it's not empty (user might not want to change password)
        if (password != null && !password.isEmpty()) {
            editor.putString(KEY_USER_PASSWORD, password);
        }

        editor.apply();
    }

    // Clear all user data (for logout)
    public void clearUserData() {
        editor.clear();
        editor.apply();

        // Also clear all notes from SQLite database
        databaseHelper.deleteAllNotes();
    }

    // Notes management methods using SQLite

    // Get all notes
    public List<Note> getNotes() {
        return databaseHelper.getAllNotes();
    }

    // Add a new note
    public void addNote(Note note) {
        databaseHelper.addNote(note);
    }

    // Update an existing note
    public void updateNote(Note updatedNote) {
        databaseHelper.updateNote(updatedNote);
    }

    // Delete a note
    public void deleteNote(String noteId) {
        databaseHelper.deleteNote(noteId);
    }

    // Get notes count (additional utility method)
    public int getNotesCount() {
        return databaseHelper.getNotesCount();
    }

    // Remove the old saveNotes method as it's no longer needed
    // Notes are now saved individually through addNote/updateNote methods
}