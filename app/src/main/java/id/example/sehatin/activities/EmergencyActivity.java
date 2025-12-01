package id.example.sehatin.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import id.example.sehatin.databinding.ActivityEmergencyBinding;

public class EmergencyActivity extends AppCompatActivity {

    private static final String TAG = "EmergencyActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DocumentReference docRef = db.collection("emergencyContacts").document("0kS7VWGPf0DDpOOpgfzH");

    private static final int REQUEST_CALL_PHONE = 1;
    private String emergencyNumber = "118"; // Default emergency number

    private ActivityEmergencyBinding binding;
    private Handler handler = new Handler();
    private boolean isHolding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve emergency contact from Firestore
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String numberFromFirestore = document.getString("phoneNumber");
                        if (numberFromFirestore != null && !numberFromFirestore.isEmpty()) {
                            emergencyNumber = numberFromFirestore;
                            Log.d(TAG, "Emergency number updated to: " + emergencyNumber);
                        } else {
                            Log.d(TAG, "Emergency number not found in document, using default.");
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        binding.btnCallEmergency.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isHolding = true;
                binding.tvHoldHint.setText("Menahan...");

                handler.postDelayed(() -> {
                    if (isHolding) {
                        checkPermissionAndCall();
                    }
                }, 3000); // 3 detik

            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {

                isHolding = false;
                binding.tvHoldHint.setText("Tahan selama 3 detik...");
            }

            return true;
        });
    }

    private void checkPermissionAndCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE);
        } else {
            makeEmergencyCall();
        }
    }

    private void makeEmergencyCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + emergencyNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal melakukan panggilan darurat!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                makeEmergencyCall();
            } else {
                Toast.makeText(this,
                        "Permission ditolak! Hubungi manual: " + emergencyNumber,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
