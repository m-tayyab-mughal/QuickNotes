package com.example.quicknotes;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private EditText etName, etEmail, etPassword;
    private Button btnSave, btnLogout, btnDeluser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- CHANGE 1: Replace ProgressDialog with AlertDialog for the custom dialog ---
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- CHANGE 2: Initialize our new custom dialog ---
        initLoadingDialog();

        etName = view.findViewById(R.id.etProfileName);
        etEmail = view.findViewById(R.id.etProfileEmail);
        etPassword = view.findViewById(R.id.etProfilePassword);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        btnDeluser = view.findViewById(R.id.btnDeluser);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                showAuthenticationDialog("save");
            }
        });

        btnLogout.setOnClickListener(v -> logout());
        btnDeluser.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // --- CHANGE 3: Add this new method to build the dialog from the layout file ---
    private void initLoadingDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            etName.setText(currentUser.getDisplayName());
            etEmail.setText(currentUser.getEmail());
            etPassword.setHint("New Password (leave blank to keep current)");
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("Name cannot be empty");
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.setError("Email cannot be empty");
            return false;
        }
        String password = etPassword.getText().toString().trim();
        if (!password.isEmpty() && password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void showAuthenticationDialog(String action) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_authenticate, null);
        builder.setView(dialogView);

        final EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        Button btnVerify = dialogView.findViewById(R.id.btnVerify);
        // === CHANGE 1: Cancel button ko find karein ===
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAuth);

        final AlertDialog dialog = builder.create();

        // Dialog ka background transparent karein taaki CardView ke corners nazar aayein
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // === CHANGE 2: Cancel button ke liye click listener set karein ===
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Sirf dialog ko band kar dein
        });

        btnVerify.setOnClickListener(v -> {
            String password = etCurrentPassword.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                etCurrentPassword.setError("Password is required");
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                showLoadingDialog("Verifying...");
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    hideLoadingDialog();
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        if ("save".equals(action)) {
                            saveUserData();
                        } else if ("delete".equals(action)) {
                            deleteAccount();
                        }
                    } else {
                        Toast.makeText(getContext(), "Authentication failed. Incorrect password.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Re-authentication failed", task.getException());
                    }
                });
            } else {
                Toast.makeText(getContext(), "Cannot verify. User not found.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveUserData() {
        showLoadingDialog("Updating Profile...");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            hideLoadingDialog();
            Toast.makeText(getContext(), "Error: User not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        List<Task<?>> updateTasks = new ArrayList<>();

        if (!Objects.equals(user.getDisplayName(), newName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newName).build();
            updateTasks.add(user.updateProfile(profileUpdates));
        }

        if (!Objects.equals(user.getEmail(), newEmail)) {
            updateTasks.add(user.updateEmail(newEmail));
        }

        if (!newPassword.isEmpty()) {
            updateTasks.add(user.updatePassword(newPassword));
        }

        if (updateTasks.isEmpty()) {
            hideLoadingDialog();
            Toast.makeText(getContext(), "No changes to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        Tasks.whenAllComplete(updateTasks).addOnCompleteListener(task -> {
            hideLoadingDialog();
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                loadUserData();
            } else {
                Log.e(TAG, "Failed to update profile", task.getException());
                Toast.makeText(getContext(), "Failed to update profile. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This is permanent and will delete all your notes.")
                .setPositiveButton("Delete", (dialog, which) -> showAuthenticationDialog("delete"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        showLoadingDialog("Deleting Account...");
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            hideLoadingDialog();
            return;
        }

        db.collection("notes").whereEqualTo("userId", user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }
                        batch.commit().addOnCompleteListener(batchTask -> {
                            if (batchTask.isSuccessful()) {
                                user.delete().addOnCompleteListener(deleteUserTask -> {
                                    hideLoadingDialog();
                                    if (deleteUserTask.isSuccessful()) {
                                        Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                        logout();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Failed to delete user's notes.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        hideLoadingDialog();
                        Toast.makeText(getContext(), "Failed to find user's notes for deletion.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- CHANGE 5: Add these helper methods to control the new dialog ---
    private void showLoadingDialog(String message) {
        if (loadingDialog != null) {
            loadingDialog.show();
            TextView tvMessage = loadingDialog.findViewById(R.id.tv_loading_message);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
