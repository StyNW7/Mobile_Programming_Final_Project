package id.example.sehatin.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

        binding.etTglLahir.setOnClickListener(v -> showDatePickerDialog());

        binding.btnHitungJadwal.setOnClickListener(v -> {
            if (!binding.etTglLahir.getText().toString().isEmpty()) {
                calculateAndDisplaySchedule(binding.etTglLahir.getText().toString());
            } else {
                Toast.makeText(this, "Mohon masukkan Tanggal Lahir anak.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y);
                    binding.etTglLahir.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateAndDisplaySchedule(String tglLahir) {
        // Logika detail perhitungan jadwal imunisasi berdasarkan tgl lahir
        Toast.makeText(this, "Jadwal imunisasi otomatis dihitung dari tgl lahir: " + tglLahir, Toast.LENGTH_LONG).show();
    }
}