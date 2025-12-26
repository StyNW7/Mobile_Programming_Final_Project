package id.example.sehatin.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.example.sehatin.R;
import id.example.sehatin.adapters.JadwalImunisasiAdapter;
import id.example.sehatin.databinding.ActivityImunisasiBinding;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.JadwalItem;
import id.example.sehatin.models.VaccineSchedule;
import id.example.sehatin.utils.SessionManager;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
// import id.example.sehatin.models.Child;


public class ImunisasiActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference vaccinesCollectionReference = db.collection("vaccineSchedules");
    private final CollectionReference childrenCollectionReference = db.collection("children");
    private ActivityImunisasiBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private JadwalImunisasiAdapter adapter;
    private final List<JadwalItem> jadwalItems = new ArrayList<>();
    // Format: [id, userId, name, birthdate, gender]
    private final List<String[]> childList = new ArrayList<>(); 
    private final List<String> childNames = new ArrayList<>();
    private String selectedChildId = null;
    // private String selectedChildName = null; // Unused
    private Spinner spinnerAnak;
    private ArrayAdapter<String> spinnerAdapter;
    private final List<VaccineSchedule> existingSchedules = new ArrayList<>(); // To store status from DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImunisasiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();
        sessionManager = new SessionManager(this);

        setupTopBar();
        setupBottomNavigation();
        setupRecyclerView();

        // Setup Spinner
        spinnerAnak = findViewById(R.id.spinner_anak);
        loadChildrenForSpinner();

        binding.etTglLahir.setFocusable(false);
        binding.etTglLahir.setClickable(true);
        binding.etTglLahir.setOnClickListener(v -> showDatePickerDialog());

        binding.btnHitungJadwal.setOnClickListener(v -> {
            String tglLahir = binding.etTglLahir.getText().toString().trim();
            if (!tglLahir.isEmpty()) {
                calculateAndDisplaySchedule(tglLahir);
            } else {
                Toast.makeText(this, "Mohon masukkan Tanggal Lahir anak.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTopBar() {
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Jadwal Imunisasi");

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
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_article) {
                startActivity(new Intent(this, InfoRubrikActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChatbotActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
    }



    private void loadChildrenForSpinner() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "";
        if (userId.isEmpty()) return;

        childrenCollectionReference.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        childList.clear();
                        childNames.clear();
                        childNames.add("-- Pilih Anak --"); // Default option

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String birthDate = doc.getString("birthDate"); // Assuming 'birthDate' field matches
                            if (birthDate == null) birthDate = doc.getString("birthdate"); // Try lowercase
                            
                            childList.add(new String[]{id, name, birthDate});
                            childNames.add(name);
                        }

                        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, childNames);
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAnak.setAdapter(spinnerAdapter);

                        spinnerAnak.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position > 0) {
                                    // Adjusted index because of default option
                                    String[] childData = childList.get(position - 1);
                                    selectedChildId = childData[0];
                                    // selectedChildName = childData[1]; // Unused
                                    String birthDateIso = childData[2];
                                    
                                    // 1. Fetch existing status first
                                    fetchExistingSchedules(selectedChildId, () -> {
                                        // 2. Then calculate/display (using birthdate)
                                        if (birthDateIso != null) {
                                            try {
                                                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                SimpleDateFormat displayFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
                                                Date date = isoFormat.parse(birthDateIso);
                                                if (date != null) {
                                                    binding.etTglLahir.setText(displayFormat.format(date));
                                                    // Automatically calculate/show schedule when child selected
                                                    calculateAndDisplaySchedule(displayFormat.format(date));
                                                }
                                            } catch (ParseException e) {
                                                Log.e("Imunisasi", "Date Parse Error", e);
                                            }
                                        }
                                    });

                                } else {
                                    selectedChildId = null;
                                    // selectedChildName = null;
                                    binding.etTglLahir.setText("");
                                    jadwalItems.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    }
                });
    }

    private void setupRecyclerView() {
        binding.rvJadwalImunisasi.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJadwalImunisasi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JadwalImunisasiAdapter(jadwalItems, item -> {
            // On Item Checked
            if (selectedChildId == null) return;
            updateVaccineStatus(item);
        });
        binding.rvJadwalImunisasi.setAdapter(adapter);
    }
    
    private void fetchExistingSchedules(String childId, Runnable onComplete) {
        vaccinesCollectionReference.whereEqualTo("childId", childId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        existingSchedules.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Manual mapping or use helper
                            VaccineSchedule schedule = new VaccineSchedule(
                                    doc.getId(),
                                    doc.getString("userId"),
                                    doc.getString("childId"),
                                    doc.getString("vaccineName"),
                                    doc.getString("scheduledDate"),
                                    doc.getString("reminderDate"),
                                    Boolean.TRUE.equals(doc.getBoolean("isCompleted")), // Safe unboxing
                                    doc.getString("completedDate"),
                                    doc.getString("notes")
                            );
                            existingSchedules.add(schedule);
                        }
                    }
                    if (onComplete != null) onComplete.run();
                });
    }

    private void updateVaccineStatus(JadwalItem item) {
        // Find if this vaccine already has a doc in 'existingSchedules' (and thus Firestore)
        VaccineSchedule targetSchedule = null;
        for (VaccineSchedule s : existingSchedules) {
            if (s.vaccineName.equals(item.getNamaVaksin())) {
                targetSchedule = s;
                break;
            }
        }

        if (targetSchedule != null) {
            // Update existing
            vaccinesCollectionReference.document(targetSchedule.id)
                    .update("isCompleted", item.isCompleted())
                    .addOnSuccessListener(aVoid -> Log.d("Imunisasi", "Status updated"));
        } else {
            // Create new if not exists (should rarely happen if we sync properly, but safe to have)
             FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
             String userId = (currentUser != null) ? currentUser.getUid() : "";
             
             // We need db format date
             // For simplicity, we just save what we have. Ideally we pass real dates.
             VaccineSchedule newSchedule = new VaccineSchedule(
                     null, userId, selectedChildId, item.getNamaVaksin(), 
                     "", // scheduledDate (we might need to keep this from calculation)
                     null, item.isCompleted(), null, ""
             );
             dbHelper.addVaccineSchedule(newSchedule, task -> {
                 if(task.isSuccessful()) {
                      // refresh or add to existing list
                      // ID is already set by helper synchronously
                      existingSchedules.add(newSchedule);
                 }
             });
        }

    }

    private Calendar getVaccineCalendar(Calendar birthDate, int bulanDitambah) {
        Calendar targetDate = (Calendar) birthDate.clone();
        targetDate.add(Calendar.MONTH, bulanDitambah);
        return targetDate;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%02d / %02d / %d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                    );
                    binding.etTglLahir.setText(date);
                }, year, month, day
        );
        datePickerDialog.show();
    }

    private void calculateAndDisplaySchedule(String tglLahir) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.getDefault());
        SimpleDateFormat uiFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "guest";
        try {
            Date date = inputFormat.parse(tglLahir);
            Calendar birthCalendar = Calendar.getInstance();
            if (date != null) {
                birthCalendar.setTime(date);
            }

            jadwalItems.clear();

            Object[][] masterJadwal = {
                    {"Hepatitis B0", 0},
                    {"BCG", 1},
                    {"Polio 1", 1},
                    {"DPT-HB-Hib 1", 2},
                    {"Polio 2", 2},
                    {"DPT-HB-Hib 2", 3},
                    {"Polio 3", 3},
                    {"DPT-HB-Hib 3", 4},
                    {"Polio 4", 4},
                    {"Campak / MR", 9}
            };

            Toast.makeText(this, "Menghitung jadwal...", Toast.LENGTH_SHORT).show();

            for (Object[] item : masterJadwal) {
                String namaVaksin = (String) item[0];
                int jarakBulan = (int) item[1];

                Calendar targetCal = getVaccineCalendar(birthCalendar, jarakBulan);
                String tglUI = uiFormat.format(targetCal.getTime());
                String tglDB = dbFormat.format(targetCal.getTime());

                JadwalItem jadwalItem = new JadwalItem(namaVaksin, tglUI, jarakBulan);
                
                // Check if completed in DB
                boolean isCompleted = false;
                boolean existsInDb = false;
                
                for (VaccineSchedule s : existingSchedules) {
                    if (s.vaccineName.equals(namaVaksin)) {
                        isCompleted = s.isCompleted;
                        existsInDb = true;
                        break;
                    }
                }
                
                jadwalItem.setCompleted(isCompleted);
                jadwalItems.add(jadwalItem);

                // If it doesn't exist in DB, create the initial record so we have a place to store status later
                if (!existsInDb && currentUser != null && selectedChildId != null) {
                     VaccineSchedule schedule = new VaccineSchedule(
                            null, userId, selectedChildId, namaVaksin, tglDB, tglDB, false, null, "Otomatis"
                    );
                    dbHelper.addVaccineSchedule(schedule, task -> {
                        if (task.isSuccessful()) {
                             existingSchedules.add(schedule); // Add to local cache
                        }
                    });
                }
            }

            adapter.notifyDataSetChanged();

            Calendar today = Calendar.getInstance();
            long diff = today.getTimeInMillis() - birthCalendar.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            long months = days / 30;

            binding.tvUmurAnak.setText(String.format(Locale.getDefault(),"👶 Umur: %d Bulan", months));
            binding.tvTotalVaksin.setText(String.format(Locale.getDefault(),"💉 Total: %d Vaksin", masterJadwal.length));

            Toast.makeText(this, "Jadwal berhasil dihitung!", Toast.LENGTH_SHORT).show();

        } catch (ParseException e) {
            Log.e("ParseException", "Error parsing date", e);
            Toast.makeText(this, "Format tanggal salah!", Toast.LENGTH_SHORT).show();
        }
    }
}