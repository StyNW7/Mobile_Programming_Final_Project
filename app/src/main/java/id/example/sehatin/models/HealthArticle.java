package id.example.sehatin.models;

import java.io.Serializable;

public class HealthArticle implements Serializable {
    public String id;
    public String title;
    public String content;
    public String category; // ex: "Imunisasi", "Stunting", "Kehamilan", etc.
    public String thumbnailUrl; // optional
    public String createdAt; // ISO yyyy-MM-dd or timestamp string
    public String authorName;

    public HealthArticle() {}

    public HealthArticle(String id, String title, String content, String category,
                         String thumbnailUrl, String createdAt, String authorName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.authorName = authorName;
    }
}
