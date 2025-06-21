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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText etEmail;
    private Button btnResetPassword;
    private FirebaseAuth mAuth;

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etEmail = view.findViewById(R.id.etForgotEmail);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);

        // Set OnClickListener for the reset button
        btnResetPassword.setOnClickListener(v -> {
            if (validateInput()) {
                sendResetLink();
            }
        });
    }

    private void sendResetLink() {
        String email = etEmail.getText().toString().trim();

        // Disable button to prevent multiple clicks
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Sending Link...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset link sent to your email.", Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(getContext(), "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // Re-enable button
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Send Reset Link");
                });
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        etEmail.setError(null);
        return true;
    }

    private void navigateToLogin() {
        // Navigate back to the LoginFragment after sending the link
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }
}