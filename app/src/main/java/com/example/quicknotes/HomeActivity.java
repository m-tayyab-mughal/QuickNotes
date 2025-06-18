package com.example.quicknotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

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

        fragmentManager = getSupportFragmentManager();

        initializeViews();
        setupClickListeners(); // Yeh method ab maujood hai
        setupNetworkObserver(); // Network status ke liye

        if (savedInstanceState == null) {
            loadFragment(getHomeFragment(), 0); // Yeh method ab maujood hai
        }
    }

    private void initializeViews() {
        tabHome = findViewById(R.id.tab_home);
        tabAdd = findViewById(R.id.tab_add);
        tabProfile = findViewById(R.id.tab_profile);
        floatingAdd = findViewById(R.id.floating_add);
    }

    // --- YEH MISSING METHOD THA ---
    private void setupClickListeners() {
        tabHome.setOnClickListener(v -> loadFragment(getHomeFragment(), 0));
        tabAdd.setOnClickListener(v -> openNoteActivity(null));
        floatingAdd.setOnClickListener(v -> openNoteActivity(null));
        tabProfile.setOnClickListener(v -> loadFragment(getProfileFragment(), 2));
    }

    // --- NETWORK STATUS OBSERVER ---
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

    // --- YEH SAB MISSING METHODS THE ---
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
                // HomeFragment ab LiveData istemal kar raha hai, to iski zaroorat shayad na ho
                // lekin behtar hai ke rakhein
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