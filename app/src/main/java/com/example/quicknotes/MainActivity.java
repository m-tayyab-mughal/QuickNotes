package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout loginTab, signupTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is already logged in and start HomeActivity if they are
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close this activity
            return;
        }

        // If no user is logged in, show the login/signup UI
        setContentView(R.layout.activity_main);

        loginTab = findViewById(R.id.login_tab);
        signupTab = findViewById(R.id.signup_tab);

        loginTab.setOnClickListener(v -> loadLoginFragment());
        signupTab.setOnClickListener(v -> loadSignupFragment());

        // Load LoginFragment by default
        if (savedInstanceState == null) {
            loadLoginFragment();
        }
    }

    private void loadLoginFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
        updateTabSelection(true);
    }

    private void loadSignupFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SignupFragment())
                .commit();
        updateTabSelection(false);
    }


    private void updateTabSelection(boolean isLoginSelected) {
        loginTab.setAlpha(isLoginSelected ? 1.0f : 0.6f);
        signupTab.setAlpha(isLoginSelected ? 0.6f : 1.0f);
    }
}
