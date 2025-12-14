package id.example.sehatin.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityRiwayatBinding;
import id.example.sehatin.utils.SessionManager;

public class RiwayatActivity extends AppCompatActivity {

    private ActivityRiwayatBinding binding;
    private static final String PREFS_NAME = "sehatin_prefs";
    private static final String KEY_RIWAYAT = "riwayat_list";
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiwayatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupTopBar();
        setupBottomNavigation();

        // tombol simpan
        binding.btnSimpanRiwayat.setOnClickListener(v -> onSaveClicked());
    }

    private void setupTopBar() {
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Riwayat Kesehatan");

        ImageView btnSignOut = findViewById(R.id.btnSignOut);
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNavigationView;
        FloatingActionButton fab = binding.fabEmergency;

        // Highlight Home as the parent category
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.getMenu().findItem(R.id.nav_placeholder).setEnabled(false);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_article) {
                startActivity(new Intent(this, InfoRubrikActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_chatbot) {
                // Placeholder for Chatbot
                return true;
            } else if (id == R.id.nav_profile) {
                // Placeholder for Profile
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
    }

    private void onSaveClicked() {
        String bbStr = binding.etBeratBadan.getText().toString().trim();
        String tbStr = binding.etTinggiBadan.getText().toString().trim();
        String catatan = binding.etCatatan.getText().toString().trim();

        // Validasi sederhana
        if (TextUtils.isEmpty(bbStr) || TextUtils.isEmpty(tbStr)) {
            Toast.makeText(this, "Berat dan Tinggi Badan harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pastikan input angka valid
        double bb;
        double tb;
        try {
            bb = Double.parseDouble(bbStr);
            tb = Double.parseDouble(tbStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Masukkan angka yang valid untuk Berat/Tinggi.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buat objek riwayat
        JSONObject entry = new JSONObject();
        try {
            entry.put("bb", bb);
            entry.put("tb", tb);
            entry.put("catatan", catatan);
            entry.put("tanggal", currentTimestamp()); // string tanggal saat ini
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat data riwayat.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan ke SharedPreferences (sebagai JSONArray string)
        JSONArray arr = loadHistory();
        arr.put(entry);
        boolean saved = saveHistory(arr);

        if (saved) {
            Toast.makeText(this, "Catatan Kesehatan disimpan! BB: " + bb + " kg, TB: " + tb + " cm.", Toast.LENGTH_LONG).show();

            // Kosongkan input
            binding.etBeratBadan.setText("");
            binding.etTinggiBadan.setText("");
            binding.etCatatan.setText("");

            // Tampilkan dialog berisi seluruh riwayat (terbaru di bawah)
            showHistoryDialog(arr);
        } else {
            Toast.makeText(this, "Gagal menyimpan data. Coba lagi.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray loadHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RIWAYAT, null);
        if (raw == null) return new JSONArray();

        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private boolean saveHistory(JSONArray arr) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_RIWAYAT, arr.toString());
        return ed.commit();
    }

    private void showHistoryDialog(JSONArray arr) {
        int n = arr.length();
        if (n == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Riwayat Kesehatan")
                    .setMessage("Belum ada riwayat tersimpan.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] items = new String[n];
        for (int i = 0; i < n; i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);
                String tanggal = obj.optString("tanggal", "-");
                String bb = String.valueOf(obj.optDouble("bb", 0));
                String tb = String.valueOf(obj.optDouble("tb", 0));
                String note = obj.optString("catatan", "");
                String line = tanggal + " — BB: " + bb + " kg, TB: " + tb + " cm";
                if (!note.isEmpty()) line += "\nCatatan: " + note;
                items[i] = line;
            } catch (JSONException e) {
                items[i] = "Data tidak valid";
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Riwayat Kesehatan (" + n + ")")
                .setItems(items, (dialog, which) -> {
                    try {
                        JSONObject obj = arr.getJSONObject(which);
                        String tanggal = obj.optString("tanggal", "-");
                        String bb = String.valueOf(obj.optDouble("bb", 0));
                        String tb = String.valueOf(obj.optDouble("tb", 0));
                        String note = obj.optString("catatan", "");
                        String msg = "Tanggal: " + tanggal + "\nBB: " + bb + " kg\nTB: " + tb + " cm\n\nCatatan:\n" + (note.isEmpty() ? "-" : note);

                        new AlertDialog.Builder(RiwayatActivity.this)
                                .setTitle("Detail Riwayat")
                                .setMessage(msg)
                                .setPositiveButton("OK", null)
                                .show();
                    } catch (JSONException e) {
                        Toast.makeText(RiwayatActivity.this, "Gagal menampilkan detail.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Tutup", null)
                .show();
    }

    private String currentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}