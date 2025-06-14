package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout tabHome, tabAdd, tabProfile;
    private View floatingAdd; // Changed to View
    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fragmentManager = getSupportFragmentManager();

        initializeViews();
        setupClickListeners();

        if (savedInstanceState == null) {
            loadFragment(getHomeFragment(), 0);
        }
    }

    private void initializeViews() {
        tabHome = findViewById(R.id.tab_home);
        tabAdd = findViewById(R.id.tab_add);
        tabProfile = findViewById(R.id.tab_profile);
        floatingAdd = findViewById(R.id.floating_add);
    }

    private void setupClickListeners() {
        tabHome.setOnClickListener(v -> loadFragment(getHomeFragment(), 0));
        tabAdd.setOnClickListener(v -> openNoteActivity(null));
        floatingAdd.setOnClickListener(v -> openNoteActivity(null));
        tabProfile.setOnClickListener(v -> loadFragment(getProfileFragment(), 2));
    }

    private HomeFragment getHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }

    private ProfileFragment getProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    private void loadFragment(Fragment fragment, int tabIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
        updateTabSelection(tabIndex);
    }

    public void openNoteActivity(Note note) {
        Intent intent = new Intent(HomeActivity.this, NoteActivity.class);
        if (note != null) {
            intent.putExtra("note", note);
        }
        startActivity(intent);
    }

    private void updateTabSelection(int selectedTab) {
        tabHome.setAlpha(0.6f);
        tabProfile.setAlpha(0.6f);

        switch (selectedTab) {
            case 0:
                tabHome.setAlpha(1.0f);
                break;
            case 2:
                tabProfile.setAlpha(1.0f);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User signed out, go back to login screen
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Refresh notes if home fragment is visible
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).refreshNotes();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
        if (!(currentFragment instanceof HomeFragment)) {
            loadFragment(getHomeFragment(), 0);
        } else {
            super.onBackPressed();
        }
    }
}
