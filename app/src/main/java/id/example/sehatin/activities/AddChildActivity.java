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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

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
    // private RecyclerView rvSavedChildren; // Local variable sufficient

    private FirestoreRecyclerAdapter<Child, ChildViewHolder> adapter;

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
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        // Setup Listener untuk Tombol Simpan
        btnSaveChild.setOnClickListener(v -> saveChildData());

        // Setup Toolbar
        // Bagian ini sekarang akan berfungsi karena AddChildActivity menggunakan tema DarkActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tambah Data Anak");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView rvSavedChildren = findViewById(R.id.rvSavedChildren);
        rvSavedChildren.setLayoutManager(new LinearLayoutManager(this));

        // Get current user ID
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        if (!userId.isEmpty()) {
            Query query = db.collection("children")
                    .whereEqualTo("userId", userId);
            // .orderBy("name", Query.Direction.ASCENDING); // Requires index if mixed with whereEqualTo

            FirestoreRecyclerOptions<Child> options = new FirestoreRecyclerOptions.Builder<Child>()
                    .setQuery(query, Child.class)
                    .build();

            adapter = new FirestoreRecyclerAdapter<>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ChildViewHolder holder, int position, @NonNull Child model) {
                    holder.tvChildName.setText(model.getName());
                    String gender = "Laki-laki";
                    if ("female".equalsIgnoreCase(model.getGender())) {
                        gender = "Perempuan";
                        // Optionally use a pink version or same icon with different tint if desired
                        // For now using the new consistent default icon
                        holder.imgChildIcon.setImageResource(R.drawable.ic_child_default);
                    } else {
                         holder.imgChildIcon.setImageResource(R.drawable.ic_child_default);
                    }
                    holder.tvChildInfo.setText(String.format("%s • %s", gender, model.getBirthDate()));

                    holder.btnDeleteChild.setOnClickListener(v -> {
                        getSnapshots().getSnapshot(holder.getBindingAdapterPosition()).getReference().delete();
                        Toast.makeText(AddChildActivity.this, "Data anak dihapus.", Toast.LENGTH_SHORT).show();
                    });
                }

                @NonNull
                @Override
                public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                    View view = LayoutInflater.from(group.getContext())
                            .inflate(R.layout.item_saved_child, group, false);
                    return new ChildViewHolder(view);
                }
            };
            rvSavedChildren.setAdapter(adapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    // ViewHolder Class
    private static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildName, tvChildInfo;
        ImageView imgChildIcon;
        android.widget.ImageButton btnDeleteChild;

        public ChildViewHolder(View itemView) {
            super(itemView);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvChildInfo = itemView.findViewById(R.id.tvChildInfo);
            imgChildIcon = itemView.findViewById(R.id.imgChildIcon);
            btnDeleteChild = itemView.findViewById(R.id.btnDeleteChild);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField();
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
        getOnBackPressedDispatcher().onBackPressed();
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
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveChild.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Opsional: Perbarui Child object dengan ID Firestore yang baru dibuat
                        String newId = task.getResult().getId();
                        task.getResult().update("id", newId); // Set ID dokumen ke properti id

                        Toast.makeText(AddChildActivity.this, "Data anak berhasil ditambahkan!", Toast.LENGTH_LONG).show();

                        // Reset form instead of finish()
                        etChildName.setText("");
                        etBirthDate.setText("");
                        etBirthWeight.setText("");
                        etBirthHeight.setText("");
                        rgChildGender.check(R.id.rbMale);
                        selectedBirthDateIso = null;
                    } else {
                        String msg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(AddChildActivity.this, "Gagal menyimpan data: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}