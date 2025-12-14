package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import id.example.sehatin.R;
import id.example.sehatin.fragments.HomeFragment;
// You need to create these other fragments or use placeholders
// import id.example.sehatin.fragments.ChatbotFragment;
// import id.example.sehatin.fragments.ArticleFragment;
// import id.example.sehatin.fragments.ProfileFragment;
import id.example.sehatin.utils.SessionManager;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabEmergency;
    private ImageView btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check Login
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // --- SETUP UI ---
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabEmergency = findViewById(R.id.fabEmergency);
        btnSignOut = findViewById(R.id.btnSignOut);

        // 1. Default Fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // 2. Setup Top Bar Sign Out
        btnSignOut.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 3. Setup Bottom Navigation
        // Disable the middle placeholder click
        bottomNavigationView.getMenu().findItem(R.id.nav_placeholder).setEnabled(false);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_chatbot) {
                // selectedFragment = new ChatbotFragment();
                // Placeholder for now:
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_article) {
                // selectedFragment = new ArticleFragment();
                // Placeholder for now:
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_profile) {
                // selectedFragment = new ProfileFragment();
                // Placeholder for now:
                selectedFragment = new HomeFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // 4. Setup Middle FAB (Emergency)
        fabEmergency.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}