package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityInfoRubrikBinding;

public class InfoRubrikActivity extends AppCompatActivity {

    private ActivityInfoRubrikBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoRubrikBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil data dari strings.xml / arrays.xml
        String[] topics = getResources().getStringArray(R.array.info_topics);

        // Adaptor untuk ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                topics
        );

        binding.lvRubrikInfo.setAdapter(adapter);

        // Klik setiap topik
        binding.lvRubrikInfo.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = topics[position];

            // Nanti bisa diarahkan ke activity detail
            Toast.makeText(
                    this,
                    "Membuka artikel: " + selectedItem,
                    Toast.LENGTH_SHORT
            ).show();

            // Contoh kalau mau pake Intent ke halaman detail (opsional)
            /*
            Intent intent = new Intent(InfoRubrikActivity.this, DetailArtikelActivity.class);
            intent.putExtra("JUDUL_ARTIKEL", selectedItem);
            startActivity(intent);
            */
        });
    }
}
