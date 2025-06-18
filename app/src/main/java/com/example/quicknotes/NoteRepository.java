package com.example.quicknotes;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final WorkManager workManager;
    private final String userId;
    private final ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getDatabase(application);
        this.noteDao = db.noteDao();
        this.workManager = WorkManager.getInstance(application);
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Note>> getAllNotes() {
        startSync(); // Jab bhi notes ki zaroorat ho, sync try karein
        return noteDao.getAllNotes(userId);
    }

    public void insertOrUpdateNote(final Note note) {
        executorService.execute(() -> {
            if (userId == null) return;

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

        // Unique work ensure karta hai ke ek hi waqt mein ek sync process chale
        workManager.enqueueUniqueWork("sync_notes_work", ExistingWorkPolicy.KEEP, syncWorkRequest);
    }
}