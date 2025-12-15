package id.example.sehatin.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import id.example.sehatin.databinding.ActivityLoginBinding;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.User;
import id.example.sehatin.utils.SessionManager; // IMPORT SESSION MANAGER

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager; // 1. Define SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper();
        sessionManager = new SessionManager(this); // 2. Initialize SessionManager

        // Check if already logged in (Optional optimization)
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Clear previous errors
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);

            boolean isValid = true;

            if (email.isEmpty()) {
                binding.tilEmail.setError("Email tidak boleh kosong");
                isValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Format email tidak valid");
                isValid = false;
            }

            if (password.isEmpty()) {
                binding.tilPassword.setError("Password tidak boleh kosong");
                isValid = false;
            }

            if (!isValid) return;

            showLoading(true);

            // Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            fetchUserDataFromFirestore(userId);
                        } else {
                            showLoading(false);
                            String error = task.getException() != null ? task.getException().getMessage() : "Login Gagal";
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void fetchUserDataFromFirestore(String userId) {
        dbHelper.getUser(userId, task -> {
            showLoading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);

                    // 3. SAVE SESSION
                    sessionManager.createLoginSession(user);

                    Toast.makeText(this, "Selamat datang, " + user.getName(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Profil tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }
}