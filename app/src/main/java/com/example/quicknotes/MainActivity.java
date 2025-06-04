package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private LinearLayout loginTab, signupTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(this);

        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            // Navigate to HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity
            return;
        }

        // Initialize tab views
        loginTab = findViewById(R.id.login_tab);
        signupTab = findViewById(R.id.signup_tab);

        // Set up click listeners for tabs
        loginTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLoginFragment();
            }
        });

        signupTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSignupFragment();
            }
        });

        // Load login fragment by default if no saved instance
        if (savedInstanceState == null) {
            loadLoginFragment();
        }
    }

    private void loadLoginFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();

        // Update the UI to show which tab is selected
        updateTabSelection(true);
    }

    private void loadSignupFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SignupFragment())
                .commit();

        // Update the UI to show which tab is selected
        updateTabSelection(false);
    }

    private void updateTabSelection(boolean isLoginSelected) {
        // Visual indication of selected tab
        loginTab.setAlpha(isLoginSelected ? 1.0f : 0.6f);
        signupTab.setAlpha(isLoginSelected ? 0.6f : 1.0f);


    }
}