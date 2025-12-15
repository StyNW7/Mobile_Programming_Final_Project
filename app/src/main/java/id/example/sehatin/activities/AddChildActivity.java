package id.example.sehatin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import id.example.sehatin.R;
import id.example.sehatin.models.Child;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddChildActivity extends AppCompatActivity {

    private EditText etChildName, etBirthDate, etBirthWeight, etBirthHeight;
    private RadioGroup rgChildGender;
    private Button btnSaveChild;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Calendar calendar = Calendar.getInstance();
    private String selectedBirthDateIso; // Untuk menyimpan format ISO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi Views
        etChildName = findViewById(R.id.etChildName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etBirthWeight = findViewById(R.id.etBirthWeight);
        etBirthHeight = findViewById(R.id.etBirthHeight);
        rgChildGender = findViewById(R.id.rgChildGender);
        btnSaveChild = findViewById(R.id.btnSaveChild);
        progressBar = findViewById(R.id.progressBar);

        // Setup Listener untuk Tanggal Lahir (Date Picker)
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Setup Listener untuk Tombol Simpan
        btnSaveChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChildData();
            }
        });

        // Setup Toolbar
        // Bagian ini sekarang akan berfungsi karena AddChildActivity menggunakan tema DarkActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tambah Data Anak");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateField();
            }
        };

        new DatePickerDialog(
                AddChildActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateField() {
        // Format tampilan di EditText (e.g., 14 November 2024)
        String viewFormat = "dd MMMM yyyy";
        SimpleDateFormat viewSdf = new SimpleDateFormat(viewFormat, new Locale("id", "ID"));
        etBirthDate.setText(viewSdf.format(calendar.getTime()));

        // Format ISO String "yyyy-MM-dd" untuk disimpan ke database
        String isoFormat = "yyyy-MM-dd";
        SimpleDateFormat isoSdf = new SimpleDateFormat(isoFormat, Locale.US);
        selectedBirthDateIso = isoSdf.format(calendar.getTime());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void saveChildData() {
        String name = etChildName.getText().toString().trim();
        String birthDateStr = etBirthDate.getText().toString().trim();
        String birthWeightStr = etBirthWeight.getText().toString().trim();
        String birthHeightStr = etBirthHeight.getText().toString().trim();

        // 1. Validasi Input Dasar
        if (TextUtils.isEmpty(name)) {
            etChildName.setError("Nama harus diisi.");
            return;
        }
        if (TextUtils.isEmpty(birthDateStr)) {
            etBirthDate.setError("Tanggal Lahir harus diisi.");
            return;
        }

        int selectedGenderId = rgChildGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Pilih jenis kelamin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Dapatkan Nilai
        RadioButton selectedRadioButton = findViewById(selectedGenderId);
        // Konversi ke format model: "male" atau "female"
        String genderText = selectedRadioButton.getText().toString().toLowerCase(Locale.ROOT);
        String gender = genderText.contains("laki") ? "male" : "female";

        // Konversi Double (Boleh Kosong/Nol)
        Double birthWeight = TextUtils.isEmpty(birthWeightStr) ? 0.0 : Double.parseDouble(birthWeightStr);
        Double birthHeight = TextUtils.isEmpty(birthHeightStr) ? 0.0 : Double.parseDouble(birthHeightStr);

        // 3. Ambil ID Pengguna
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveChild.setEnabled(false);

        // ID Child akan diisi secara otomatis oleh Firestore
        String childId = null;

        // Buat objek Child
        Child newChild = new Child(childId, userId, name, selectedBirthDateIso, gender, birthWeight, birthHeight);

        // 4. Simpan data ke Firestore
        db.collection("children")
                .add(newChild)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        progressBar.setVisibility(View.GONE);
                        btnSaveChild.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Opsional: Perbarui Child object dengan ID Firestore yang baru dibuat
                            String newId = task.getResult().getId();
                            task.getResult().update("id", newId); // Set ID dokumen ke properti id

                            Toast.makeText(AddChildActivity.this, "Data anak berhasil ditambahkan!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(AddChildActivity.this, "Gagal menyimpan data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}