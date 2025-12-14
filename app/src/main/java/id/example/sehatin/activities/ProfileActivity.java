package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import id.example.sehatin.R;
import id.example.sehatin.models.User;
import id.example.sehatin.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI Elements
    private TextView tvHeaderName, tvHeaderEmail;
    private EditText etName, etPhone, etEmail;
    private Button btnUpdate, btnLogoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase & Session
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Initialize Views
        initViews();

        // Setup Navigation Bars
        setupTopBar();
        setupBottomNavigation();

        // Load Data
        loadUserData();

        // Button Listeners
        btnUpdate.setOnClickListener(v -> updateProfileName());
        btnLogoutMain.setOnClickListener(v -> logoutUser());
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_header_name);
        tvHeaderEmail = findViewById(R.id.tv_header_email);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);

        btnUpdate = findViewById(R.id.btn_update);
        btnLogoutMain = findViewById(R.id.btn_logout_main);
    }

    private void setupTopBar() {
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Profil Saya");

        ImageView btnSignOut = findViewById(R.id.btnSignOut);
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> logoutUser());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        FloatingActionButton fab = findViewById(R.id.fabEmergency);

        // 1. Highlight Profile
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.getMenu().findItem(R.id.nav_placeholder).setEnabled(false);

        // 2. Navigation Logic
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChatbotActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_article) {
                startActivity(new Intent(this, InfoRubrikActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                // Already here
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // 1. Try to fetch fresh data from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                populateUI(user);
                            }
                        }
                    } else {
                        // Fallback: Use Session Manager if Firestore fails (offline)
                        User cachedUser = sessionManager.getUserDetail();
                        if (cachedUser != null) {
                            populateUI(cachedUser);
                        }
                    }
                });
    }

    private void populateUI(User user) {
        tvHeaderName.setText(user.name);
        tvHeaderEmail.setText(user.email);

        etName.setText(user.name);
        etPhone.setText(user.phoneNumber);
        etEmail.setText(user.email);
    }

    private void updateProfileName() {
        String newName = etName.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (newName.isEmpty()) {
            etName.setError("Nama tidak boleh kosong");
            return;
        }

        if (currentUser == null) return;

        // Disable button during update
        btnUpdate.setEnabled(false);
        btnUpdate.setText("Menyimpan...");

        // Update Firestore
        db.collection("users").document(currentUser.getUid())
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();

                    // Update UI Header immediately
                    tvHeaderName.setText(newName);

                    // Update Session Manager (If your SessionManager has a method for this)
                    // sessionManager.updateName(newName); // Implement this if needed

                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Simpan Perubahan");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Simpan Perubahan");
                });
    }

    private void logoutUser() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}