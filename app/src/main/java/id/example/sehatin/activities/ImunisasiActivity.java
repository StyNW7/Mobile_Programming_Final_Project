package id.example.sehatin.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import id.example.sehatin.databinding.ActivityImunisasiBinding;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.VaccineSchedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImunisasiActivity extends AppCompatActivity {

    private ActivityImunisasiBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImunisasiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // Pastikan EditText tidak bisa diketik manual
        binding.etTglLahir.setFocusable(false);
        binding.etTglLahir.setClickable(true);

        // Klik tanggal lahir → buka DatePicker
        binding.etTglLahir.setOnClickListener(v -> showDatePickerDialog());

        // Klik tombol → hitung jadwal
        binding.btnHitungJadwal.setOnClickListener(v -> {
            String tglLahir = binding.etTglLahir.getText().toString().trim();

            if (!tglLahir.isEmpty()) {
                calculateAndDisplaySchedule(tglLahir);
            } else {
                Toast.makeText(this, "Mohon masukkan Tanggal Lahir anak.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String calculateVaccineDate(Calendar birthDate, int bulanDitambah) {
        Calendar targetDate = (Calendar) birthDate.clone(); // clone biar tgl lahir asli gak berubah
        targetDate.add(Calendar.MONTH, bulanDitambah);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        return sdf.format(targetDate.getTime());
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

        // Format untuk Database (Wajib ISO yyyy-MM-dd agar WorkManager bisa baca)
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Ambil User ID saat ini
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "guest";
        try {
            Date date = inputFormat.parse(tglLahir);
            Calendar birthCalendar = Calendar.getInstance();
            if (date != null) {
                birthCalendar.setTime(date);
            }

            List<String> hasilJadwal = new ArrayList<>();

            // format: vaksin: jarak bulan dr tgl lahir
            Object[][] masterJadwal = {
                    {"Hepatitis B0", 0}, // 0 bulan (saat lahir)
                    {"BCG", 1},          // 1 bulan
                    {"Polio 1", 1},
                    {"DPT-HB-Hib 1", 2}, // 2 bulan
                    {"Polio 2", 2},
                    {"DPT-HB-Hib 2", 3}, // 3 bulan
                    {"Polio 3", 3},
                    {"DPT-HB-Hib 3", 4}, // 4 bulan
                    {"Polio 4", 4},
                    {"Campak / MR", 9}   // 9 bulan
            };

            Toast.makeText(this, "Menghitung & menyimpan jadwal...", Toast.LENGTH_SHORT).show();

            // hitung jadwal tiap vaksin
            for (Object[] item : masterJadwal) {
                String namaVaksin = (String) item[0];
                int jarakBulan = (int) item[1];

                Calendar targetCal = getVaccineCalendar(birthCalendar, jarakBulan);

                //  Siapkan String Tampilan & Database
                String tglUI = uiFormat.format(targetCal.getTime()); // "12 Januari 2025"
                String tglDB = dbFormat.format(targetCal.getTime()); // "2025-01-12"

                // Masukkan ke List UI
                String tampilan = namaVaksin + " (Usia " + jarakBulan + " bulan)\n📅 " + tglUI;
                hasilJadwal.add(tampilan);

                if (currentUser != null) {
                    String childId = "child_id_placeholder";

                    VaccineSchedule schedule = new VaccineSchedule(
                            null,               // ID Auto-generate
                            userId,             // ID Parent
                            childId,            // ID Anak
                            namaVaksin,         // Nama Vaksin
                            tglDB,              // Tanggal Jadwal (yyyy-MM-dd)
                            tglDB,              // Reminder date (disamakan dulu)
                            false,              // Belum selesai
                            null,               // Tgl selesai null
                            "Otomatis dari Kalkulator" // Notes
                    );

                    dbHelper.addVaccineSchedule(schedule, task -> {
                        if (task.isSuccessful()) {
                            Log.d("JADWAL", "Tersimpan: " + namaVaksin);
                        }
                    });
                }
            }

            // UI adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    hasilJadwal
            );
            binding.lvJadwalImunisasi.setAdapter(adapter);
            setListViewHeightBasedOnChildren(binding.lvJadwalImunisasi);

            // Update Info Umur (Hitung selisih bulan sederhana)
            Calendar today = Calendar.getInstance();
            long diff = today.getTimeInMillis() - birthCalendar.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            long months = days / 30;

            binding.tvUmurAnak.setText("👶 Umur: " + months + " Bulan");
            binding.tvTotalVaksin.setText("💉 Total: " + masterJadwal.length + " Vaksin");

            Toast.makeText(this, "Jadwal berhasil dihitung!", Toast.LENGTH_SHORT).show();

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Format tanggal salah!", Toast.LENGTH_SHORT).show();
        }
    }

    // method ini hrsny ga dipake kl diganti jadi recycler view
    public static void setListViewHeightBasedOnChildren(android.widget.ListView listView) {
        android.widget.ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            android.view.View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        android.view.ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
