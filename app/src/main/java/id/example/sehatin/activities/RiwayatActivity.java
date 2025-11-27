package id.example.sehatin.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import id.example.sehatin.databinding.ActivityRiwayatBinding;

public class RiwayatActivity extends AppCompatActivity {

    private ActivityRiwayatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiwayatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSimpanRiwayat.setOnClickListener(v -> {
            String bb = binding.etBeratBadan.getText().toString();
            String tb = binding.etTinggiBadan.getText().toString();
            String catatan = binding.etCatatan.getText().toString();

            if (bb.isEmpty() || tb.isEmpty()) {
                Toast.makeText(this, "Berat dan Tinggi Badan harus diisi.", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "Catatan Kesehatan disimpan! BB: " + bb + " kg, TB: " + tb + " cm.";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            binding.etBeratBadan.setText("");
            binding.etTinggiBadan.setText("");
            binding.etCatatan.setText("");
        });
    }
}