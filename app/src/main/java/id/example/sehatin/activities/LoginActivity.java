package id.example.sehatin.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import id.example.sehatin.databinding.ActivityLoginBinding;
import id.example.sehatin.firebase.DatabaseHelper;
import id.example.sehatin.models.User;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper();

        setupListeners();
    }

    private void setupListeners() {

        // LOGIN BUTTON
        binding.btnLogin.setOnClickListener(view -> {

            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Field empty check
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Email validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Firebase Authentication: Sign In
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // AUTH SUCCESS
                            String userId = mAuth.getCurrentUser().getUid();

                            // 2. Retrieve User Data from Firestore
                            fetchUserDataFromFirestore(userId);

                        } else {
                            // AUTH FAILURE
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login Gagal. Cek email dan password Anda.";
                            Toast.makeText(this, "Login Gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // GO TO REGISTER
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void fetchUserDataFromFirestore(String userId) {
        dbHelper.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Convert Firestore Document to your User Model
                    User user = document.toObject(User.class);

                    // 3. Login Success & Data Retrieved
                    Toast.makeText(this, "Selamat datang, " + user.getName(), Toast.LENGTH_SHORT).show();

                    // Navigate to the Main Activity
                    // You should pass the User object or its ID to the main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // Consider adding the User object as a serializable extra here
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(this, "Profil tidak ditemukan di database.", Toast.LENGTH_LONG).show();
                    // Optional: Sign out the user if their data is missing in Firestore
                    mAuth.signOut();
                }
            } else {
                Toast.makeText(this, "Gagal mengambil data profil.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        });
    }
}
