package com.example.quicknotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    // --- NEW: A constant code to identify our location permission request ---
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private LinearLayout tabHome, tabAdd, tabProfile;
    private View floatingAdd;
    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    // Network status ke liye
    private ConnectionLiveData connectionLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- NEW: Check for permissions as soon as the app's main screen loads ---
        checkAndRequestLocationPermissions();

        fragmentManager = getSupportFragmentManager();

        initializeViews();
        setupClickListeners();
        setupNetworkObserver();

        if (savedInstanceState == null) {
            loadFragment(getHomeFragment(), 0);
        }
    }

    // --- NEW METHOD TO CHECK AND REQUEST PERMISSIONS ---
    private void checkAndRequestLocationPermissions() {
        // Check if location permissions are already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If permissions are not granted, request them from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // --- NEW METHOD TO HANDLE THE RESULT OF THE PERMISSION REQUEST ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permission was granted by the user
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission was denied
                Toast.makeText(this, "Location permission denied. Note location features will be unavailable.", Toast.LENGTH_LONG).show();
            }
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

    private void setupNetworkObserver() {
        connectionLiveData = new ConnectionLiveData(this);
        connectionLiveData.observe(this, status -> {
            if (status != null) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                if (status.startsWith("Online")) {
                    NoteRepository noteRepository = new NoteRepository(getApplication());
                    noteRepository.startSync();
                }
            }
        });
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
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof HomeFragment) {
                // This block can be used for refreshing if needed
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