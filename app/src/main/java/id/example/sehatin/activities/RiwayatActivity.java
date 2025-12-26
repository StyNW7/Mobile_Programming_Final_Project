package id.example.sehatin.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityRiwayatBinding;
import id.example.sehatin.utils.SessionManager;

public class RiwayatActivity extends AppCompatActivity {

    private ActivityRiwayatBinding binding;
    private SessionManager sessionManager;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Spinner Data
    private Spinner spinnerAnak;
    private final List<String[]> childList = new ArrayList<>();
    private final List<String> childNames = new ArrayList<>();
    private String selectedChildId = null;

    // RecyclerView components
    private RiwayatAdapter adapter;
    private final List<RiwayatModel> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiwayatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupTopBar();
        setupBottomNavigation();
        setupRecyclerView();

        // Setup Spinner
        spinnerAnak = findViewById(R.id.spinner_anak_riwayat);
        loadChildrenForSpinner();

        // tombol simpan
        binding.btnSimpanRiwayat.setOnClickListener(v -> onSaveClicked());
    }

    private void setupTopBar() {
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText(R.string.title_riwayat_kesehatan);

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

    private void loadChildrenForSpinner() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "";
        if (userId.isEmpty()) return;

        db.collection("children").whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        childList.clear();
                        childNames.clear();
                        childNames.add("-- Pilih Anak --");

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            childList.add(new String[]{id, name});
                            childNames.add(name);
                        }

                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, childNames);
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAnak.setAdapter(spinnerAdapter);

                        spinnerAnak.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // Clear inputs when switching child
                                binding.etBeratBadan.setText("");
                                binding.etTinggiBadan.setText("");
                                binding.etCatatan.setText("");

                                // Immediate Clear UI for feedback
                                historyList.clear();
                                // Data set is small, full refresh is acceptable
                                //noinspection NotifyDataSetChanged
                                adapter.notifyDataSetChanged();
                                binding.rvRiwayat.setVisibility(View.GONE);
                                binding.cvEmptyState.setVisibility(View.GONE);

                                if (position > 0) {
                                    selectedChildId = childList.get(position - 1)[0];
                                    binding.progressBar.setVisibility(View.VISIBLE); // Show loader
                                    loadHistoryData(selectedChildId);
                                } else {
                                    selectedChildId = null;
                                    binding.progressBar.setVisibility(View.GONE);
                                    updateEmptyState();
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }
                });
    }

    private void loadHistoryData(String childId) {
        if (childId == null) return;
        
        // Remove .orderBy to avoid needing a Composite Index (which causes failure if missing)
        db.collection("child_growth_records")
                .whereEqualTo("childId", childId)
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE); // Hide loader
                    if (task.isSuccessful()) {
                        historyList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Date createdAt = doc.getDate("createdAt");
                            if (createdAt == null) createdAt = new Date();

                            // Safe unboxing
                            Double wVal = doc.getDouble("weight");
                            double weight = (wVal != null) ? wVal : 0.0;

                            Double hVal = doc.getDouble("height");
                            double height = (hVal != null) ? hVal : 0.0;

                            RiwayatModel item = new RiwayatModel(
                                    doc.getString("dateString"),
                                    weight,
                                    height,
                                    doc.getString("notes"),
                                    createdAt
                            );
                            historyList.add(item);
                        }
                        
                        // Sort client-side (Descending)
                        historyList.sort((o1, o2) -> o2.realDate.compareTo(o1.realDate));

                        // small list
                        //noinspection NotifyDataSetChanged
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    } else {
                        Log.e("Riwayat", "Error loading history", task.getException());
                        Toast.makeText(RiwayatActivity.this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }
                });
    }

    private void onSaveClicked() {
        if (selectedChildId == null) {
            Toast.makeText(this, "Silakan pilih anak terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        
        // Save to Firestore
        Map<String, Object> record = new HashMap<>();
        record.put("childId", selectedChildId);
        record.put("dateString", timestamp);
        record.put("weight", bb);
        record.put("height", tb);
        record.put("notes", catatan);
        Date now = new Date();
        record.put("createdAt", now); // For sorting

        db.collection("child_growth_records")
                .add(record)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                         // Update UI
                        RiwayatModel newItem = new RiwayatModel(timestamp, bb, tb, catatan, now);
                        historyList.add(0, newItem);
                        adapter.notifyItemInserted(0);
                        binding.rvRiwayat.smoothScrollToPosition(0);
                        updateEmptyState();

                        // Reset Input
                        binding.etBeratBadan.setText("");
                        binding.etTinggiBadan.setText("");
                        binding.etCatatan.setText("");

                        Toast.makeText(this, "Riwayat tersimpan.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gagal menyimpan riwayat.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    // --- SHARED PREFERENCES HELPER METHODS REMOVED ---

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
        Date realDate;

        public RiwayatModel(String tanggal, double bb, double tb, String catatan, Date realDate) {
            this.tanggal = tanggal;
            this.bb = bb;
            this.tb = tb;
            this.catatan = catatan;
            this.realDate = realDate;
        }
    }

    // 2. RecyclerView Adapter
    public static class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {
        private final List<RiwayatModel> list;

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
            holder.tvBb.setText(String.format(Locale.getDefault(), "%.1f kg", item.bb));
            holder.tvTb.setText(String.format(Locale.getDefault(), "%.1f cm", item.tb));

            if (item.catatan != null && !item.catatan.isEmpty()) {
                holder.tvNotes.setVisibility(View.VISIBLE);
                holder.tvNotes.setText(String.format("Catatan: %s", item.catatan));
            } else {
                holder.tvNotes.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
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