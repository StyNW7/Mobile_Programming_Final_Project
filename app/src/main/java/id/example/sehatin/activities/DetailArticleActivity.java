package id.example.sehatin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.example.sehatin.R;
import id.example.sehatin.databinding.ActivityDetailArticleBinding;
import id.example.sehatin.models.HealthArticle;

public class DetailArticleActivity extends AppCompatActivity {

    private ActivityDetailArticleBinding binding;
    private HealthArticle article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailArticleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get article data from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ARTICLE_DATA")) {
            try {
                article = (HealthArticle) intent.getSerializableExtra("ARTICLE_DATA");
                if (article != null) {
                    displayArticleDetails();
                } else {
                    showErrorAndFinish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAndFinish();
            }
        } else {
            showErrorAndFinish();
        }

        // Setup back button
        binding.ivBack.setOnClickListener(v -> finish());

        // Setup share button
        binding.btnShare.setOnClickListener(v -> shareArticle());
    }

    private void displayArticleDetails() {
        try {
            // Set category
            if (article.category != null && !article.category.isEmpty()) {
                binding.tvCategory.setText(article.category);
                binding.tvCategory.setVisibility(View.VISIBLE);
            } else {
                binding.tvCategory.setVisibility(View.GONE);
            }

            // Set title
            if (article.title != null) {
                binding.tvArticleTitle.setText(article.title);
            }

            // Set author and formatted date
            String authorText = article.authorName != null ? article.authorName : "Penulis";
            String dateText = formatDate(article.createdAt);
            binding.tvAuthorDate.setText(String.format("%s • %s", authorText, dateText));

            // Load thumbnail image with Glide
            if (article.thumbnailUrl != null && !article.thumbnailUrl.isEmpty()) {
                Glide.with(this)
                        .load(article.thumbnailUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(binding.ivArticleThumbnail);
            } else {
                binding.ivArticleThumbnail.setImageResource(R.drawable.placeholder_image);
            }

            // Set content with HTML support
            if (article.content != null) {
                // Process content to handle **bold** text
                String processedContent = processContentForHtml(article.content);

                Spanned content;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    content = Html.fromHtml(processedContent, Html.FROM_HTML_MODE_COMPACT);
                } else {
                    content = Html.fromHtml(processedContent);
                }
                binding.tvArticleContent.setText(content);
                binding.tvArticleContent.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                binding.tvArticleContent.setText("Konten tidak tersedia");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menampilkan artikel", Toast.LENGTH_SHORT).show();
        }
    }

    private String processContentForHtml(String content) {
        if (content == null) return "";

        // Replace **bold** with <b>bold</b>
        String processed = content.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        // Replace newlines with <br> tags
        processed = processed.replace("\n\n", "<br><br>");
        processed = processed.replace("\n", "<br>");

        return processed;
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Tanggal tidak tersedia";
        }

        try {
            SimpleDateFormat inputFormat;

            // Try different date formats
            if (dateString.contains("-")) {
                // Check if it has time component
                if (dateString.contains("T")) {
                    // ISO format with time: 2025-12-04T10:30:00Z
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                } else {
                    // Simple date format: 2025-12-04
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
            } else {
                // If it's already in a different format, try to parse as is
                return dateString;
            }

            Date date = inputFormat.parse(dateString);

            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }

    private void shareArticle() {
        if (article == null) return;

        String shareText = String.format(
                "%s\n\n%s\n\nBaca selengkapnya di aplikasi Sehatin.",
                article.title,
                article.content != null && article.content.length() > 150
                        ? article.content.substring(0, 150) + "..."
                        : (article.content != null ? article.content : "")
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Bagikan Artikel"));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk berbagi", Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, "Artikel tidak ditemukan", Toast.LENGTH_SHORT).show();
        finish();
    }
}