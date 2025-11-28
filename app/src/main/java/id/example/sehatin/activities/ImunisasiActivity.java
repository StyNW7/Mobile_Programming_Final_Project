package id.example.sehatin.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.example.sehatin.databinding.ActivityImunisasiBinding;

import java.util.Calendar;
import java.util.Locale;

public class ImunisasiActivity extends AppCompatActivity {

    private ActivityImunisasiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImunisasiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        // Contoh data jadwal imunisasi
        String[] jadwal = {
                "BCG - Usia 1 bulan",
                "Polio 1 - Usia 2 bulan",
                "DPT 1 - Usia 2 bulan",
                "Hepatitis B - Usia 2 bulan",
                "Polio 2 - Usia 3 bulan",
                "DPT 2 - Usia 3 bulan",
                "Polio 3 - Usia 4 bulan",
                "DPT 3 - Usia 4 bulan",
                "Campak - Usia 9 bulan"
        };

        // Adapter untuk ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                jadwal
        );

        // Set hasil ke ListView
        binding.lvJadwalImunisasi.setAdapter(adapter);

        // Contoh update informasi umur & total vaksin
        binding.tvUmurAnak.setText("👶 Umur: berdasarkan " + tglLahir);
        binding.tvTotalVaksin.setText("💉 Vaksin: " + jadwal.length);

        // Bonus feedback
        Toast.makeText(this, "Jadwal imunisasi berhasil dibuat ✅", Toast.LENGTH_SHORT).show();
    }
}
