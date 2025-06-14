package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private PreferenceManager preferenceManager;

    public LoginFragment() {
        // Required empty constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(requireContext());

        // Initialize views
        etEmail = view.findViewById(R.id.Email);
        etPassword = view.findViewById(R.id.Password);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.ForgotPassword);

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Navigate to ForgotPasswordFragment
            if (getActivity() != null) {
                ForgotPasswordFragment forgotPasswordFragment = com.example.quicknotes.ForgotPasswordFragment.newInstance();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, forgotPasswordFragment)
                        .addToBackStack(null)  // Allows back navigation
                        .commit();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void loginUser() {
        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Simulate network delay
        btnLogin.postDelayed(() -> {
            // Check if credentials match what's in SharedPreferences
            String storedEmail = preferenceManager.getUserEmail();
            String storedPassword = preferenceManager.getUserPassword();

            if (email.equals(storedEmail) && password.equals(storedPassword)) {
                // Set login status
                preferenceManager.setLoggedIn(true);

                // Navigate to HomeActivity
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish(); // Close MainActivity
                }
            } else {
                Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
            }
        }, 1500);
    }
}
