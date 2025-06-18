package com.example.quicknotes;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private final NoteDao noteDao;
    private final FirebaseFirestore firestore;
    private final String userId;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        noteDao = NoteDatabase.getDatabase(context).noteDao();
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public Result doWork() {
        if (userId == null) {
            Log.d(TAG, "User not logged in, skipping sync.");
            return Result.success();
        }

        Log.d(TAG, "Starting sync for user: " + userId);

        try {
            syncLocalDeletionsToFirestore();
            syncLocalChangesToFirestore();
            syncFirestoreChangesToLocal();
            Log.d(TAG, "Sync completed successfully.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry();
        }
    }

    private void syncLocalDeletionsToFirestore() {
        List<DeletedNote> deletedNotes = noteDao.getDeletedNoteIds();
        if (deletedNotes.isEmpty()) return;

        Log.d(TAG, "Syncing " + deletedNotes.size() + " deletions to Firestore.");
        WriteBatch batch = firestore.batch();
        for (DeletedNote deletedNote : deletedNotes) {
            batch.delete(firestore.collection("notes").document(deletedNote.getId()));
        }

        try {
            Tasks.await(batch.commit());
            for (DeletedNote deletedNote : deletedNotes) {
                noteDao.removeDeletedNoteId(deletedNote.getId());
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error deleting notes from Firestore", e);
        }
    }

    private void syncLocalChangesToFirestore() {
        List<Note> unsyncedNotes = noteDao.getUnsyncedNotes(userId);
        if (unsyncedNotes.isEmpty()) return;

        Log.d(TAG, "Syncing " + unsyncedNotes.size() + " local changes to Firestore.");
        WriteBatch batch = firestore.batch();
        for (Note note : unsyncedNotes) {
            batch.set(firestore.collection("notes").document(note.getId()), note);
        }

        try {
            Tasks.await(batch.commit());
            for (Note note : unsyncedNotes) {
                note.setSynced(true);
                noteDao.updateNote(note);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error syncing local notes to Firestore", e);
        }
    }

    private void syncFirestoreChangesToLocal() throws ExecutionException, InterruptedException {
        Log.d(TAG, "Fetching remote changes from Firestore.");
        List<Note> remoteNotes = Tasks.await(firestore.collection("notes")
                        .whereEqualTo("userId", userId).get())
                .toObjects(Note.class);

        for (Note remoteNote : remoteNotes) {
            Note localNote = noteDao.getNoteById(remoteNote.getId(), userId);
            if (localNote == null || remoteNote.getLastModified() > localNote.getLastModified()) {
                Log.d(TAG, "Updating local note: " + remoteNote.getId());
                remoteNote.setSynced(true);
                noteDao.insertOrUpdateNote(remoteNote);
            }
        }
    }
}