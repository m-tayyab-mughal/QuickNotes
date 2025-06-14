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

public class SignupFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private PreferenceManager preferenceManager;

    public SignupFragment() {
        // Required empty constructor
    }

    public static SignupFragment newInstance() {
        return new SignupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(requireContext());

        // Initialize views
        etName = view.findViewById(R.id.Name);
        etEmail = view.findViewById(R.id.Email);
        etPassword = view.findViewById(R.id.Password);
        etConfirmPassword = view.findViewById(R.id.ConfirmPassword);
        btnSignup = view.findViewById(R.id.btnSignUp);

        // Setup focus listeners to clear errors
        setupFocusListeners();

        // Set click listener
        btnSignup.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private void setupFocusListeners() {
        View.OnFocusChangeListener clearErrorListener = (v, hasFocus) -> {
            if (hasFocus) {
                ((TextInputEditText) v).setError(null);
            }
        };

        etName.setOnFocusChangeListener(clearErrorListener);
        etEmail.setOnFocusChangeListener(clearErrorListener);
        etPassword.setOnFocusChangeListener(clearErrorListener);
        etConfirmPassword.setOnFocusChangeListener(clearErrorListener);
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Name validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            etName.setError("Name must be at least 3 characters");
            etName.requestFocus();
            isValid = false;
        } else if (!containsOnlyAlphabets(name)) {
            etName.setError("Name must contain only alphabetic characters");
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
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        } else if (!containsLetterAndDigit(password)) {
            etPassword.setError("Password must contain at least one letter and one number");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm password is required");
            if (isValid) etConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            if (isValid) etConfirmPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }


    private boolean containsOnlyAlphabets(String input) {
        for (char c : input.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }
        return true;
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

    private void registerUser() {
        // Show loading state
        btnSignup.setEnabled(false);
        btnSignup.setText("Creating Account...");

        // Get user data
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Simulate network delay
        btnSignup.postDelayed(() -> {
            // Save user data to SharedPreferences
            preferenceManager.saveUserData(name, email, password);

            btnSignup.setEnabled(true);
            btnSignup.setText("Sign Up");

            // Show success message
            Toast.makeText(getContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show();

            // Navigate to the LoginFragment
            navigateToLogin();
        }, 1500);
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            // Create new instance of LoginFragment
            LoginFragment loginFragment = LoginFragment.newInstance();

            // Replace current fragment with LoginFragment
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, loginFragment)
                    .addToBackStack(null)  // Optional: allows back navigation
                    .commit();
        }
    }
}
