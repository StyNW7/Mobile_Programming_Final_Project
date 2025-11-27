package id.example.sehatin.activities;

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

        // Mengambil data dari resources/arrays.xml
        String[] topics = getResources().getStringArray(R.array.info_topics);

        // Adaptor untuk menampilkan data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, topics);
        binding.lvRubrikInfo.setAdapter(adapter);

        binding.lvRubrikInfo.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            Toast.makeText(this, "Membuka detail artikel: " + selectedItem, Toast.LENGTH_SHORT).show();
            // TODO: Tambahkan intent ke DetailArtikelActivity
        });
    }
}