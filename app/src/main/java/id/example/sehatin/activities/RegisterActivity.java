package id.example.sehatin.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import id.example.sehatin.databinding.ActivityRegisterBinding;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.User;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper();

        setupListeners();
    }

    private void setupListeners() {

        // REGISTER BUTTON
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String phoneNumber = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            // 1. Validation Checks
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                Toast.makeText(this, "Format nomor telepon tidak valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // AUTH SUCCESS
                            String userId = mAuth.getCurrentUser().getUid();

                            // 3. Prepare User Data for Firestore
                            // Assuming User model has 7 parameters: (id, fcmToken, name, email, role, phoneNumber, address)
                            User user = new User(
                                    userId,
                                    "",          // fcmToken (empty initially)
                                    name,
                                    email,
                                    phoneNumber
                            );

                            // 4. Save User Details to Firestore Database
                            dbHelper.addUser(user, dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    // FIRESTORE SUCCESS
                                    Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();

                                    // Pindah ke login HANYA jika Auth dan DB berhasil
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    // FIRESTORE FAILURE
                                    String dbErrorMessage = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown DB Error";
                                    Toast.makeText(this, "Registrasi gagal disimpan: " + dbErrorMessage, Toast.LENGTH_LONG).show();

                                    // Delete Auth user to avoid orphaned account
                                    if (mAuth.getCurrentUser() != null) {
                                        mAuth.getCurrentUser().delete();
                                    }
                                }
                            });

                        } else {
                            // AUTH FAILURE
                            String authErrorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown Auth Error";
                            Toast.makeText(this, "Registrasi Gagal: " + authErrorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        });

        // GO TO LOGIN
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
