package id.example.sehatin.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class ImunisasiActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference vaccinesCollectionReference = db.collection("vaccineSchedules");
    private final CollectionReference childrenCollectionReference = db.collection("children");
    private ActivityImunisasiBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private JadwalImunisasiAdapter adapter;
    private final List<JadwalItem> jadwalItems = new ArrayList<>();
    private final List<Object[]> children = new ArrayList<>();

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

        // Removed setupSpinner() and loadChildrenFromFirestore()
        // because the XML layout does not have the Spinner element anymore.

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

        loadSchedulesFromFirestore();
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

    private void loadSchedulesFromFirestore() {
        childrenCollectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                children.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    children.add(new Object[]{doc.getId(), doc.getString("userId"), doc.getString("name"), doc.getString("birthdate")});
                }
                vaccinesCollectionReference.get().addOnCompleteListener(vaccineTask -> {
                    if (vaccineTask.isSuccessful()) {
                        jadwalItems.clear();
                        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        SimpleDateFormat uiFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

                        for (QueryDocumentSnapshot doc : vaccineTask.getResult()) {
                            String childId = doc.getString("childId");
                            String vaccineName = doc.getString("vaccineName");
                            String scheduledDateStr = doc.getString("scheduledDate");

                            if (childId == null) continue;

                            for (Object[] child : children) {
                                if (childId.equals(child[0])) {
                                    String childBirthdateStr = (String) child[3];
                                    try {
                                        if (childBirthdateStr == null || scheduledDateStr == null) continue;
                                        Date birthDate = dbFormat.parse(childBirthdateStr);
                                        Date scheduledDate = dbFormat.parse(scheduledDateStr);

                                        if (birthDate != null && scheduledDate != null) {
                                            Calendar startCalendar = Calendar.getInstance();
                                            startCalendar.setTime(birthDate);
                                            Calendar endCalendar = Calendar.getInstance();
                                            endCalendar.setTime(scheduledDate);

                                            int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                                            int usiaBulan = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

                                            String formattedScheduledDate = uiFormat.format(scheduledDate);
                                            jadwalItems.add(new JadwalItem(vaccineName, formattedScheduledDate, usiaBulan));
                                        }
                                    } catch (ParseException e) {
                                        Log.e("DateParseError", "Failed to parse date string.", e);
                                    }
                                    break;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvJadwalImunisasi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JadwalImunisasiAdapter(jadwalItems);
        binding.rvJadwalImunisasi.setAdapter(adapter);
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

            Toast.makeText(this, "Menghitung & menyimpan jadwal...", Toast.LENGTH_SHORT).show();

            for (Object[] item : masterJadwal) {
                String namaVaksin = (String) item[0];
                int jarakBulan = (int) item[1];

                Calendar targetCal = getVaccineCalendar(birthCalendar, jarakBulan);
                String tglUI = uiFormat.format(targetCal.getTime());
                String tglDB = dbFormat.format(targetCal.getTime());

                jadwalItems.add(new JadwalItem(namaVaksin, tglUI, jarakBulan));

                if (currentUser != null) {
                    // Placeholder: Because we removed the Child Spinner from the layout,
                    // we cannot determine which child this schedule belongs to.
                    // For now, it just calculates visually.
                    String childId = "child_id_placeholder";

                    VaccineSchedule schedule = new VaccineSchedule(
                            null, userId, childId, namaVaksin, tglDB, tglDB, false, null, "Otomatis dari Kalkulator"
                    );
                    dbHelper.addVaccineSchedule(schedule, task -> {
                        if (task.isSuccessful()) {
                            Log.d("JADWAL", "Tersimpan: " + namaVaksin);
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