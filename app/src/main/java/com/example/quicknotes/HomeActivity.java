package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;  // Only for login credentials
    private LinearLayout tabHome, tabAdd, tabProfile, floatingAdd;
    private FragmentManager fragmentManager;

    // Fragment instances
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize preferences (only for login-related operations)
        preferenceManager = new PreferenceManager(this);

        // Initialize fragment manager
        fragmentManager = getSupportFragmentManager();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadHomeFragment();
        }
    }

    private void initializeViews() {
        tabHome = findViewById(R.id.tab_home);
        tabAdd = findViewById(R.id.tab_add);
        tabProfile = findViewById(R.id.tab_profile);
        floatingAdd = findViewById(R.id.floating_add);
    }

    private void setupClickListeners() {
        // Home tab click listener
        tabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHomeFragment();
                updateTabSelection(0); // 0 for home
            }
        });

        // Add tab click listener (center button)
        tabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteActivity();
            }
        });

        // Floating add button click listener
        floatingAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteActivity();
            }
        });

        // Profile tab click listener
        tabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProfileFragment();
                updateTabSelection(2); // 2 for profile
            }
        });
    }

    private void loadHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, homeFragment);
        transaction.commit();

        // Ensure the fragment refreshes its data
        fragmentManager.executePendingTransactions();
        if (homeFragment.isAdded() && homeFragment.getView() != null) {
            homeFragment.refreshNotes();
        }
    }

    private void loadProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, profileFragment);
        transaction.commit();
    }

    private void openNoteActivity() {
        Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
        startActivity(intent);
    }

    private void updateTabSelection(int selectedTab) {
        // Reset all tabs to default state
        tabHome.setAlpha(0.6f);
        tabProfile.setAlpha(0.6f);

        // Highlight selected tab
        switch (selectedTab) {
            case 0: // Home
                tabHome.setAlpha(1.0f);
                break;
            case 2: // Profile
                tabProfile.setAlpha(1.0f);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is still logged in (using PreferenceManager for login state)
        if (!preferenceManager.isLoggedIn()) {
            // Navigate back to MainActivity
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Refresh current fragment if it's HomeFragment
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof HomeFragment) {
                // Force refresh the home fragment
                ((HomeFragment) currentFragment).refreshNotes();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);

        // If we're not on home fragment, go back to home
        if (!(currentFragment instanceof HomeFragment)) {
            loadHomeFragment();
            updateTabSelection(0);
        } else {
            // If we're on home fragment, exit app
            super.onBackPressed();
        }
    }
}