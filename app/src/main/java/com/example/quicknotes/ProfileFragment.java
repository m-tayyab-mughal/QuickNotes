package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private EditText etName, etEmail, etPassword;
    private EditText etCurrentPassword; // For verification
    private Button btnSave, btnLogout, btnDeluser;
    private PreferenceManager preferenceManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preferences
        preferenceManager = new PreferenceManager(requireContext());

        // Initialize views
        etName = view.findViewById(R.id.etProfileName);
        etEmail = view.findViewById(R.id.etProfileEmail);
        etPassword = view.findViewById(R.id.etProfilePassword);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        btnDeluser = view.findViewById(R.id.btnDeluser);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Setup hint behavior
        setupHintBehavior();

        // Load current user data
        loadUserData();

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                showAuthenticationDialog();
            }
        });

        // Logout button click listener
        btnLogout.setOnClickListener(v -> {
            logout();
        });

        // Delete button click listener
        btnDeluser.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void setupHintBehavior() {
        // Add text change listeners to ensure hints disappear when text is present
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateHintVisibility(etName);
            }
        });

        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateHintVisibility(etEmail);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateHintVisibility(etPassword);
            }
        });

        // Initial setup for all fields
        updateHintVisibility(etName);
        updateHintVisibility(etEmail);
        updateHintVisibility(etPassword);
    }

    // Helper method to update hint visibility
    private void updateHintVisibility(EditText editText) {
        if (editText.getText().length() > 0) {
            editText.setHint(""); // Remove hint when text is present
        } else {
            // Restore appropriate hint based on which field it is
            if (editText == etName) {
                editText.setHint("Enter your full name");
            } else if (editText == etEmail) {
                editText.setHint("Enter your email address");
            } else if (editText == etPassword) {
                editText.setHint("Enter your password");
            }
        }
    }

    // Simple TextWatcher implementation to avoid overriding unused methods
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    }

    private void loadUserData() {
        etName.setText(preferenceManager.getUserName());
        etEmail.setText(preferenceManager.getUserEmail());
        // Show the old password in the password field
        etPassword.setText(preferenceManager.getUserPassword());
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Name validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            if (isValid) etEmail.requestFocus();
            isValid = false;
        } else if (!isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address");
            if (isValid) etEmail.requestFocus();
            isValid = false;
        }

        // Password validation (only if not empty)
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showAuthenticationDialog() {
        // Create a custom dialog for authentication
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_authenticate, null);
        builder.setView(dialogView);

        // Get dialog views
        etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        Button btnVerify = dialogView.findViewById(R.id.btnVerify);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set button listeners
        btnVerify.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String storedPassword = preferenceManager.getUserPassword();

            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Current password is required");
                return;
            }

            if (currentPassword.equals(storedPassword)) {
                dialog.dismiss();
                saveUserData();
            } else {
                etCurrentPassword.setError("Incorrect password");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void saveUserData() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Update user profile
        preferenceManager.updateUserProfile(name, email, password);

        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        Toast.makeText(getContext(), "Account Logout", Toast.LENGTH_SHORT).show();
        // Clear user data
        preferenceManager.setLoggedIn(false);
        // Return to MainActivity (login/signup screen)
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        // Create positive button for confirmation
        builder.setPositiveButton("Yes, Delete", (dialog, which) -> {
            // Show password verification dialog
            showDeleteAuthenticationDialog();
        });

        // Create negative button to cancel the operation
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }

    private void showDeleteAuthenticationDialog() {
        // Create a custom dialog for authentication
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_authenticate, null);
        builder.setView(dialogView);

        // Set dialog title
        TextView tvTitle = dialogView.findViewById(R.id.d_Title);
        if (tvTitle != null) {
            tvTitle.setText("Enter Password to Delete Account");
        }

        // Get dialog views
        etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        Button btnVerify = dialogView.findViewById(R.id.btnVerify);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Update button text
        btnVerify.setText("Delete Account");

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set button listeners
        btnVerify.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String storedPassword = preferenceManager.getUserPassword();

            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Password is required");
                return;
            }

            if (currentPassword.equals(storedPassword)) {
                dialog.dismiss();
                deleteAccount();
            } else {
                etCurrentPassword.setError("Incorrect password");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void deleteAccount() {
        // Clear user data
        preferenceManager.clearUserData();
        preferenceManager.setLoggedIn(false);

        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();

        // Return to MainActivity (login/signup screen)
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}