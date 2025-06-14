package com.example.quicknotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etName, etEmail, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private PreferenceManager preferenceManager;

    public ForgotPasswordFragment() {
        // Required empty constructor
    }

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(requireContext());

        // Initialize views
        etName = view.findViewById(R.id.etForgotName);
        etEmail = view.findViewById(R.id.etForgotEmail);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);

        // Set click listeners
        btnResetPassword.setOnClickListener(v -> {
            if (validateInputs()) {
                resetPassword();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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

        // Password validation
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            if (isValid) etNewPassword.requestFocus();
            isValid = false;
        } else if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            if (isValid) etNewPassword.requestFocus();
            isValid = false;
        } else if (!containsLetterAndDigit(newPassword)) {
            etNewPassword.setError("Password must contain at least one letter and one number");
            if (isValid) etNewPassword.requestFocus();
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm password is required");
            if (isValid) etConfirmPassword.requestFocus();
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            if (isValid) etConfirmPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean containsLetterAndDigit(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }

    private void resetPassword() {
        // Show loading state
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        // Get user input
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Simulate network delay
        btnResetPassword.postDelayed(() -> {
            // Restore button state
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("Reset Password");

            // Check if name and email match what's stored
            String storedName = preferenceManager.getUserName();
            String storedEmail = preferenceManager.getUserEmail();

            if (name.equals(storedName) && email.equals(storedEmail)) {
                // Update password
                preferenceManager.updateUserProfile(storedName, storedEmail, newPassword);

                // Show success message
                Toast.makeText(getContext(), "Password reset successful! Please login with your new password.", Toast.LENGTH_LONG).show();

                // Navigate back to login
                navigateToLogin();
            } else {
                // Show error message
                Toast.makeText(getContext(), "Name or email doesn't match our records", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            // Navigate back to LoginFragment
            LoginFragment loginFragment = LoginFragment.newInstance();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, loginFragment)
                    .commit();
        }
    }
}
