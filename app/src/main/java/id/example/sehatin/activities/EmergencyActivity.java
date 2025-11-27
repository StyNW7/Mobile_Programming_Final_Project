package id.example.sehatin.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import id.example.sehatin.databinding.ActivityEmergencyBinding;

public class EmergencyActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PHONE = 1;
    private static final String EMERGENCY_NUMBER = "118";
    private ActivityEmergencyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCallEmergency.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(EmergencyActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EmergencyActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            } else {
                makeEmergencyCall();
            }
        });
    }

    private void makeEmergencyCall() {
        String dialUri = "tel:" + EMERGENCY_NUMBER;
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(dialUri));

        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Izin telepon belum diberikan!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall();
            } else {
                Toast.makeText(this, "Izin ditolak. Panggil manual: " + EMERGENCY_NUMBER, Toast.LENGTH_LONG).show();
            }
        }
    }
}