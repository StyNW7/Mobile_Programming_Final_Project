package id.example.sehatin.activities;

import id.example.sehatin.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout btnPanic, btnJadwal, btnInfo, btnRiwayat, btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnPanic = findViewById(R.id.btnPanic);
        btnJadwal = findViewById(R.id.btnJadwal);
        btnInfo = findViewById(R.id.btnInfo);
        btnRiwayat = findViewById(R.id.btnRiwayat);
        btnChat = findViewById(R.id.btnChat);

        btnPanic.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
        });

        btnJadwal.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ImunisasiActivity.class));
        });

        btnInfo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, InfoRubrikActivity.class));
        });

        btnRiwayat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RiwayatActivity.class));
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
        });

    }
}
