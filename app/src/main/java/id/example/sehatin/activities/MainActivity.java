package id.example.sehatin.activities;

import id.example.sehatin.R;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.User;
import id.example.sehatin.models.VaccineSchedule;
import id.example.sehatin.utils.SessionManager; // IMPORT SESSION MANAGER
import id.example.sehatin.workers.VaccineWorker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;
    SessionManager sessionManager; // 1. Define SessionManager
    User currentUser;              // 2. Define Current User

    LinearLayout btnPanic, btnJadwal, btnInfo, btnRiwayat, btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 3. Initialize Helpers
        helper = new DatabaseHelper();
        sessionManager = new SessionManager(this);

        // 4. CHECK LOGIN STATUS
        if (!sessionManager.isLoggedIn()) {
            // If not logged in, force back to Login Page
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // 5. LOAD USER DATA
        currentUser = sessionManager.getUserDetail();

        // Optional: Show Welcome Toast or Set Title
        // Toast.makeText(this, "Halo, " + currentUser.name, Toast.LENGTH_SHORT).show();

        // --- UI Initialization ---
        btnPanic = findViewById(R.id.btnPanic);
        btnJadwal = findViewById(R.id.btnJadwal);
        btnInfo = findViewById(R.id.btnInfo);
        btnRiwayat = findViewById(R.id.btnRiwayat);
        btnChat = findViewById(R.id.btnChat);

        setupButtonListeners();
        setupTestButton(); // Refactored test button into a method for cleaner code
    }

    private void setupButtonListeners() {
        btnPanic.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
        });

        btnJadwal.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ImunisasiActivity.class));
        });

        btnInfo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, InfoRubrikActivity.class));
        });

        btnRiwayat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RiwayatActivity.class));
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
        });
    }

    private void setupTestButton() {
        // Permission Check for Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Button btnTes = findViewById(R.id.testButton);

        btnTes.setOnClickListener(v -> {
            // Hitung tanggal besok
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String tglToday = sdf.format(cal.getTime());

            // 6. USE REAL USER DATA FOR DUMMY OBJECT
            VaccineSchedule dummyData = new VaccineSchedule(
                    null, // ID generated otomatis
                    currentUser.id,     // <-- Uses Logged In User ID
                    currentUser.name,   // <-- Uses Logged In User Name
                    "Vaksin Cacar (Tes Real User)",
                    tglToday,
                    "2025-11-30",
                    false,
                    null,
                    ""
            );

            helper.addVaccineSchedule(dummyData, task -> {
                if (task.isSuccessful()) {
                    Log.d("TEST_DATA", "Data dummy masuk untuk: " + currentUser.name);
                    Toast.makeText(this, "Data Masuk untuk " + currentUser.name, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("TEST_DATA", "Gagal: " + task.getException());
                }
            });

            // Trigger Worker for testing
            OneTimeWorkRequest testWork = new OneTimeWorkRequest.Builder(VaccineWorker.class)
                    .build();
            WorkManager.getInstance(this).enqueue(testWork);
        });
    }
}