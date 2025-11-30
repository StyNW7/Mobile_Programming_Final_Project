package id.example.sehatin.activities;

import id.example.sehatin.R;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.VaccineSchedule;
import id.example.sehatin.workers.VaccineWorker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;

    LinearLayout btnPanic, btnJadwal, btnInfo, btnRiwayat, btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        helper = new DatabaseHelper();
        helper.seedDummyData();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnPanic = findViewById(R.id.btnPanic);
        btnJadwal = findViewById(R.id.btnJadwal);
        btnInfo = findViewById(R.id.btnInfo);
        btnRiwayat = findViewById(R.id.btnRiwayat);
        btnChat = findViewById(R.id.btnChat);

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

        // test pke dummy data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        Button btnTes = findViewById(R.id.testButton); // Pastikan buat button dulu di XML atau panggil langsung tanpa tombol

        // fixed userId
        String userId = "user_tes_124";
        btnTes.setOnClickListener(v -> {

            // Hitung tanggal besok
            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.DAY_OF_YEAR, 1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String tglToday = sdf.format(cal.getTime());

            // Buat object dummy

//            String currUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            VaccineSchedule dummyData = new VaccineSchedule(
                    null, // ID null biar digenerate otomatis
                    userId,
                    "Budi123",
                    "Vaksin Cacar (Tes)",
                    tglToday, // Masukkan tanggal besok agar Worker mendeteksi
                    "2025-11-30",
                    false,
                    null,
                    ""
            );

            helper.addVaccineSchedule(dummyData, task -> {
                if (task.isSuccessful()) {
                    Log.d("TEST_DATA", "Data dummy berhasil masuk!");
                    Toast.makeText(this, "Data Dummy Masuk!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("TEST_DATA", "Gagal: " + task.getException());
                }
            });
        });

        // buat testing nnt diapus
        OneTimeWorkRequest testWork = new OneTimeWorkRequest.Builder(VaccineWorker.class)
                .build();
        WorkManager.getInstance(this).enqueue(testWork);

        // testing db udh connect ke app blm
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tesData = new HashMap<>();
        tesData.put("pesan", "yo firebase ini testing 30/11/2025");
        tesData.put("waktu", FieldValue.serverTimestamp());

        db.collection("cek_koneksi")
                .add(tesData)
                .addOnSuccessListener(documentReference -> {
                    // SUKSES!
                    Log.d("CEK_FIREBASE", "Terhubung dengan ID: " + documentReference.getId());
                    Toast.makeText(this, "Firebase Terhubung!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    // GAGAL!
                    Log.e("CEK_FIREBASE", "Gagal connect", e);
                    Toast.makeText(this, "Gagal connect: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }
}