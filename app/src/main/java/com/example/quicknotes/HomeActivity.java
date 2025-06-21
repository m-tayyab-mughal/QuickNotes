package com.example.quicknotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // --- MODIFIED: A constant code to identify our multiple permissions request ---
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    // --- NEW: Define the notification permission ---
    // Note: This is for Android 13 (API 33) and above.
    private static final String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;

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

        // --- MODIFIED: Check for permissions as soon as the app's main screen loads ---
        checkAndRequestPermissions();

        fragmentManager = getSupportFragmentManager();

        initializeViews();
        setupClickListeners();
        setupNetworkObserver();

        if (savedInstanceState == null) {
            loadFragment(getHomeFragment(), 0);
        }
    }

    // --- MODIFIED METHOD TO CHECK AND REQUEST PERMISSIONS ---
    private void checkAndRequestPermissions() {
        // Create a list to hold the permissions we need to request.
        List<String> permissionsToRequest = new ArrayList<>();

        // 1. Check for Location Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // 2. Check for Notification Permission (only for Android 13 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(NOTIFICATION_PERMISSION);
            }
        }

        // If the list is not empty, it means we need to request one or more permissions.
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    // --- MODIFIED METHOD TO HANDLE THE RESULT OF THE PERMISSION REQUEST ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Loop through all the permissions that were requested
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Location permission denied. Location features will be unavailable.", Toast.LENGTH_LONG).show();
                    }
                } else if (NOTIFICATION_PERMISSION.equals(permission)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Notification permission denied. You may miss important updates.", Toast.LENGTH_LONG).show();
                    }
                }
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