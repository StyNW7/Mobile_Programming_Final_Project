package id.example.sehatin.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityRiwayatBinding;
import id.example.sehatin.utils.SessionManager;

public class RiwayatActivity extends AppCompatActivity {

    private ActivityRiwayatBinding binding;
    private static final String PREFS_NAME = "sehatin_prefs";
    private static final String KEY_RIWAYAT = "riwayat_list";
    private SessionManager sessionManager;

    // RecyclerView components
    private RiwayatAdapter adapter;
    private List<RiwayatModel> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiwayatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupTopBar();
        setupBottomNavigation();
        setupRecyclerView();

        // Load data on startup
        loadHistoryData();

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
                startActivity(new Intent(this, ChatbotActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new RiwayatAdapter(historyList);
        binding.rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRiwayat.setAdapter(adapter);
    }

    private void loadHistoryData() {
        historyList.clear();
        JSONArray jsonArray = loadHistoryFromPrefs();

        // Convert JSON Array to List of Objects
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    RiwayatModel item = new RiwayatModel(
                            obj.optString("tanggal"),
                            obj.optDouble("bb"),
                            obj.optDouble("tb"),
                            obj.optString("catatan")
                    );
                    historyList.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Reverse to show newest first
            Collections.reverse(historyList);
        }

        updateEmptyState();
        adapter.notifyDataSetChanged();
    }

    private void onSaveClicked() {
        String bbStr = binding.etBeratBadan.getText().toString().trim();
        String tbStr = binding.etTinggiBadan.getText().toString().trim();
        String catatan = binding.etCatatan.getText().toString().trim();

        if (TextUtils.isEmpty(bbStr) || TextUtils.isEmpty(tbStr)) {
            Toast.makeText(this, "Berat dan Tinggi Badan harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        double bb, tb;
        try {
            bb = Double.parseDouble(bbStr);
            tb = Double.parseDouble(tbStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Masukkan angka yang valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = currentTimestamp();

        // 1. Create Model object for UI
        RiwayatModel newItem = new RiwayatModel(timestamp, bb, tb, catatan);

        // 2. Add to UI List (Top)
        historyList.add(0, newItem);
        adapter.notifyItemInserted(0);
        binding.rvRiwayat.smoothScrollToPosition(0);
        updateEmptyState();

        // 3. Save to SharedPreferences (JSON)
        saveToPrefs(newItem);

        // 4. Reset Input
        binding.etBeratBadan.setText("");
        binding.etTinggiBadan.setText("");
        binding.etCatatan.setText("");

        Toast.makeText(this, "Riwayat tersimpan.", Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        if (historyList.isEmpty()) {
            binding.cvEmptyState.setVisibility(View.VISIBLE);
            binding.rvRiwayat.setVisibility(View.GONE);
        } else {
            binding.cvEmptyState.setVisibility(View.GONE);
            binding.rvRiwayat.setVisibility(View.VISIBLE);
        }
    }

    // --- SHARED PREFERENCES HELPER METHODS ---

    private JSONArray loadHistoryFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RIWAYAT, null);
        try {
            return raw != null ? new JSONArray(raw) : new JSONArray();
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void saveToPrefs(RiwayatModel item) {
        // We load existing, add new, then save back
        JSONArray arr = loadHistoryFromPrefs();
        JSONObject json = new JSONObject();
        try {
            json.put("tanggal", item.tanggal);
            json.put("bb", item.bb);
            json.put("tb", item.tb);
            json.put("catatan", item.catatan);

            arr.put(json); // Add to end of JSON array

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_RIWAYAT, arr.toString()).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String currentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    // --- INNER CLASSES (Model & Adapter) ---

    // 1. Simple Data Model
    public static class RiwayatModel {
        String tanggal;
        double bb;
        double tb;
        String catatan;

        public RiwayatModel(String tanggal, double bb, double tb, String catatan) {
            this.tanggal = tanggal;
            this.bb = bb;
            this.tb = tb;
            this.catatan = catatan;
        }
    }

    // 2. RecyclerView Adapter
    public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {
        private List<RiwayatModel> list;

        public RiwayatAdapter(List<RiwayatModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RiwayatModel item = list.get(position);
            holder.tvDate.setText(item.tanggal);
            holder.tvBb.setText(item.bb + " kg");
            holder.tvTb.setText(item.tb + " cm");

            if (item.catatan != null && !item.catatan.isEmpty()) {
                holder.tvNotes.setVisibility(View.VISIBLE);
                holder.tvNotes.setText("Catatan: " + item.catatan);
            } else {
                holder.tvNotes.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvBb, tvTb, tvNotes;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvBb = itemView.findViewById(R.id.tv_bb);
                tvTb = itemView.findViewById(R.id.tv_tb);
                tvNotes = itemView.findViewById(R.id.tv_notes);
            }
        }
    }
}