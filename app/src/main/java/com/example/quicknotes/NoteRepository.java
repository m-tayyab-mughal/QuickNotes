package com.example.quicknotes;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.auth.FirebaseAuth;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class NoteRepository {

    private final NoteDao noteDao;
    private final WorkManager workManager;
    private final String userId;
    private final ExecutorService executorService;
    private final Application application; // Application context for Geocoder

    public NoteRepository(Application application) {
        this.application = application; // Store application context
        NoteDatabase db = NoteDatabase.getDatabase(application);
        this.noteDao = db.noteDao();
        this.workManager = WorkManager.getInstance(application);
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Note>> getAllNotes() {
        startSync();
        return noteDao.getAllNotes(userId);
    }

    // Overloaded method for calls that don't include location (e.g., updating a reminder)
    public void insertOrUpdateNote(final Note note) {
        insertOrUpdateNote(note, null);
    }

    // Main method to save a note, now with optional location
    public void insertOrUpdateNote(final Note note, @Nullable final Location location) {
        executorService.execute(() -> {
            if (userId == null) return;

            // --- NEW: Handle location data ---
            if (location != null) {
                note.setLatitude(location.getLatitude());
                note.setLongitude(location.getLongitude());
                try {
                    Geocoder geocoder = new Geocoder(application.getApplicationContext(), Locale.getDefault());
                    // Get addresses, may be network intensive
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String locationName = address.getLocality(); // e.g., "Sialkot"
                        if (locationName == null || locationName.isEmpty()) {
                            locationName = address.getSubAdminArea(); // Fallback e.g., "Sialkot District"
                        }
                        if (locationName == null || locationName.isEmpty()) {
                            locationName = address.getCountryName(); // Fallback e.g., "Pakistan"
                        }
                        note.setLocationName(locationName);
                    } else {
                        note.setLocationName("Unknown Location");
                    }
                } catch (IOException e) {
                    Log.e("NoteRepository", "Geocoder service failed", e);
                    note.setLocationName("Location N/A"); // Set to indicate an error
                }
            }
            // If location is null, existing location data in the note is preserved.

            // --- Existing logic ---
            note.setSynced(false);
            note.setLastModified(System.currentTimeMillis());
            note.setUserId(userId);
            if (note.getId() == null || note.getId().isEmpty()) {
                note.setId(UUID.randomUUID().toString());
            }
            noteDao.insertOrUpdateNote(note);
            startSync();
        });
    }


    public void deleteNote(final Note note) {
        executorService.execute(() -> {
            if (note == null || note.getId() == null) return;
            noteDao.addDeletedNoteId(note.getId());
            noteDao.deleteNoteById(note.getId());
            startSync();
        });
    }

    public void startSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork("sync_notes_work", ExistingWorkPolicy.KEEP, syncWorkRequest);
    }
}