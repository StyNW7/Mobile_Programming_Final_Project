package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityInfoRubrikBinding;
import id.example.sehatin.models.HealthArticle;
import id.example.sehatin.utils.SessionManager;

public class InfoRubrikActivity extends AppCompatActivity {

    private ActivityInfoRubrikBinding binding;
    private List<HealthArticle> articlesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoRubrikBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        setupTopBar();
        setupBottomNavigation();
        setupArticleList();
    }

    private void setupTopBar() {
        // Find views in the included layout
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        ImageView btnSignOut = findViewById(R.id.btnSignOut);

        // Customize Title
        if (tvTitle != null) tvTitle.setText("Artikel Sehatin");

        // Handle Logout
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNavigationView;
        FloatingActionButton fab = binding.fabEmergency;

        // 1. Highlight the "Article" item
        bottomNav.setSelectedItemId(R.id.nav_article);

        // 2. Disable placeholder click
        bottomNav.getMenu().findItem(R.id.nav_placeholder).setEnabled(false);

        // 3. Handle Navigation Clicks
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Go back to Main Activity
                startActivity(new Intent(this, MainActivity.class));
                finish(); // Close this activity so back button works logically
                return true;
            } else if (id == R.id.nav_chatbot) {
                // Navigate to Chatbot (Implement later)
                return true;
            } else if (id == R.id.nav_article) {
                // Already here
                return true;
            } else if (id == R.id.nav_profile) {
                // Navigate to Profile (Implement later)
                return true;
            }
            return false;
        });

        // 4. Handle FAB Click
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyActivity.class));
        });
    }

    private void setupArticleList() {
        // Initialize adapter
        List<String> topicTitles = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                topicTitles
        );

        binding.lvRubrikInfo.setAdapter(adapter);

        // Fetch Data
        fetchArticlesFromFirestore();

        // Click Listener
        binding.lvRubrikInfo.setOnItemClickListener((parent, view, position, id) -> {
            if (position < articlesList.size()) {
                HealthArticle selectedArticle = articlesList.get(position);
                Intent intent = new Intent(InfoRubrikActivity.this, DetailArticleActivity.class);
                intent.putExtra("ARTICLE_DATA", selectedArticle);
                startActivity(intent);
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
                            article.id = document.getId();
                            articlesList.add(article);
                            titles.add(article.title);
                        }

                        adapter.clear();
                        adapter.addAll(titles);
                        adapter.notifyDataSetChanged();
                    } else {
                        loadFallbackData();
                    }
                });
    }

    private void loadFallbackData() {
        // Your existing fallback logic here...
        // (Kept shorter for brevity, but insert your full fallback code here if needed)
        String[] sampleTitles = {"Pentingnya Imunisasi", "Nutrisi Ibu Hamil"};
        for (String title : sampleTitles) {
            adapter.add(title);
            HealthArticle dummy = new HealthArticle();
            dummy.title = title;
            articlesList.add(dummy);
        }
    }
}