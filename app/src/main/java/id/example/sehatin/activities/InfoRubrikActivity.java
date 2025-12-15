package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import id.example.sehatin.databinding.ActivityInfoRubrikBinding;
import id.example.sehatin.models.HealthArticle;

public class InfoRubrikActivity extends AppCompatActivity {

    private ActivityInfoRubrikBinding binding;
    private List<HealthArticle> articlesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoRubrikBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Initialize adapter with empty list
        List<String> topicTitles = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                topicTitles
        );

        binding.lvRubrikInfo.setAdapter(adapter);

        // Fetch articles from Firebase
        fetchArticlesFromFirestore();

        // Set click listener
        binding.lvRubrikInfo.setOnItemClickListener((parent, view, position, id) -> {
            if (position < articlesList.size()) {
                HealthArticle selectedArticle = articlesList.get(position);

                Intent intent = new Intent(InfoRubrikActivity.this, DetailArticleActivity.class);
                intent.putExtra("ARTICLE_DATA", selectedArticle);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Artikel tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchArticlesFromFirestore() {
        db.collection("healthArticles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        articlesList.clear();
                        List<String> titles = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HealthArticle article = document.toObject(HealthArticle.class);
                            article.id = document.getId(); // Set the document ID
                            articlesList.add(article);
                            titles.add(article.title);
                        }

                        // Update adapter with titles
                        adapter.clear();
                        adapter.addAll(titles);
                        adapter.notifyDataSetChanged();

                        if (articlesList.isEmpty()) {
                            Toast.makeText(this, "Tidak ada artikel tersedia", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Gagal memuat artikel: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadFallbackData();
                    }
                });
    }

    private void loadFallbackData() {
        // Fallback data if Firebase fails
        articlesList.clear();
        List<String> titles = new ArrayList<>();

        // Add some sample articles
        String[] sampleTitles = {
                "Pentingnya Imunisasi Dasar Lengkap untuk Bayi",
                "Nutrisi yang Tepat untuk Ibu Hamil",
                "Cegah Stunting dengan ASI Eksklusif",
                "Perkembangan Motorik Anak 0-12 Bulan",
                "Tips Menjaga Kesehatan Mental Ibu Pasca Melahirkan"
        };

        String sampleContent = "Ini adalah contoh konten artikel tentang kesehatan ibu dan anak. " +
                "Artikel ini berisi informasi penting yang dapat membantu orang tua dalam menjaga " +
                "kesehatan keluarga. **Imunisasi** adalah salah satu cara terpenting untuk mencegah " +
                "penyakit berbahaya pada anak. Pastikan anak mendapatkan vaksinasi sesuai jadwal " +
                "yang telah ditentukan.\n\n" +
                "Selain imunisasi, **nutrisi** yang tepat juga sangat penting. Berikan makanan " +
                "bergizi seimbang untuk mendukung tumbuh kembang anak. ASI eksklusif selama 6 bulan " +
                "pertama dapat membantu mencegah stunting dan meningkatkan imunitas anak.";

        for (int i = 0; i < sampleTitles.length; i++) {
            HealthArticle article = new HealthArticle(
                    "sample_" + i,
                    sampleTitles[i],
                    sampleContent,
                    getCategoryFromTitle(sampleTitles[i]),
                    "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800",
                    "2025-12-04",
                    "Tim Sehatin"
            );
            articlesList.add(article);
            titles.add(sampleTitles[i]);
        }

        adapter.clear();
        adapter.addAll(titles);
        adapter.notifyDataSetChanged();
    }

    private String getCategoryFromTitle(String title) {
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("imunisasi") || lowerTitle.contains("vaksin")) {
            return "Imunisasi";
        } else if (lowerTitle.contains("hamil") || lowerTitle.contains("ibu")) {
            return "Kehamilan";
        } else if (lowerTitle.contains("stunting")) {
            return "Stunting";
        } else if (lowerTitle.contains("nutrisi") || lowerTitle.contains("makan")) {
            return "Nutrisi";
        } else if (lowerTitle.contains("mental")) {
            return "Kesehatan Mental";
        } else {
            return "Perkembangan Anak";
        }
    }
}