package id.example.sehatin.firebase;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;

import java.text.SimpleDateFormat;
import java.util.*;

import id.example.sehatin.models.*;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private final FirebaseFirestore db;

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // --------------------------------------------------
    // COLLECTION NAMES
    // --------------------------------------------------
    private static final String COLL_USERS = "users";
    private static final String COLL_CHILDREN = "children";
    private static final String COLL_VACCINE_SCHEDULES = "vaccineSchedules";
    private static final String COLL_HEALTH_RECORDS = "childHealthRecords";
    private static final String COLL_EMERGENCY = "emergencyContacts";
    private static final String COLL_ARTICLES = "healthArticles";

    // --------------------------------------------------
    // CREATE / INSERT
    // --------------------------------------------------
    public void addUser(User user, OnCompleteListener<Void> onComplete) {
        if (user.id == null)
            user.id = db.collection(COLL_USERS).document().getId();
        db.collection(COLL_USERS).document(user.id)
                .set(user).addOnCompleteListener(onComplete);
    }

    public void updateUserToken(String userId, String token) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token); // Pastikan key string-nya sama dengan di User.java

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating token", e));
    }

    public void addChild(Child child, OnCompleteListener<Void> onComplete) {
        if (child.id == null)
            child.id = db.collection(COLL_CHILDREN).document().getId();
        db.collection(COLL_CHILDREN).document(child.id)
                .set(child).addOnCompleteListener(onComplete);
    }

    public void addVaccineSchedule(VaccineSchedule schedule, OnCompleteListener<Void> onComplete) {
        if (schedule.id == null)
            schedule.id = db.collection(COLL_VACCINE_SCHEDULES).document().getId();
        db.collection(COLL_VACCINE_SCHEDULES).document(schedule.id)
                .set(schedule).addOnCompleteListener(onComplete);

    }

    public void addHealthRecord(ChildHealthRecord record, OnCompleteListener<Void> onComplete) {
        if (record.id == null)
            record.id = db.collection(COLL_HEALTH_RECORDS).document().getId();
        db.collection(COLL_HEALTH_RECORDS).document(record.id)
                .set(record).addOnCompleteListener(onComplete);
    }

    public void addEmergencyContact(EmergencyContact contact, OnCompleteListener<Void> onComplete) {
        if (contact.id == null)
            contact.id = db.collection(COLL_EMERGENCY).document().getId();
        db.collection(COLL_EMERGENCY).document(contact.id)
                .set(contact).addOnCompleteListener(onComplete);
    }

    public void addHealthArticle(HealthArticle article, OnCompleteListener<Void> onComplete) {
        if (article.id == null)
            article.id = db.collection(COLL_ARTICLES).document().getId();
        db.collection(COLL_ARTICLES).document(article.id)
                .set(article)
                .addOnCompleteListener(onComplete);
    }

    // --------------------------------------------------
    // READ / QUERY
    // --------------------------------------------------

    public void getUser(String userId, OnCompleteListener<DocumentSnapshot> onComplete) {
        db.collection(COLL_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(onComplete);
    }

    // --------------------------------------------------
    // DUMMY SEEDER
    // --------------------------------------------------
    public void seedDummyData() {

        addEmergencyContact(
                new EmergencyContact(
                        null, "RS Hermina", "Hospital", "021777888", "24 Hours"
                ),
                t -> Log.d(TAG, "Dummy emergency added")
        );

        addHealthArticle(
                new HealthArticle(
                        null,
                        "5 Cara Menjaga Kesehatan Anak di Usia Dini",
                        "Menjaga kesehatan anak adalah prioritas setiap orang tua. "
                                + "Pada usia dini, anak sangat rentan terhadap penyakit karena sistem imun mereka "
                                + "belum berkembang sempurna. Berikut adalah lima langkah mudah yang dapat dilakukan "
                                + "orang tua untuk memastikan anak tetap sehat setiap hari:\n\n"

                                + "1. **Vaksinasi Tepat Waktu**\n"
                                + "   Vaksinasi adalah perlindungan pertama untuk mencegah penyakit berbahaya seperti "
                                + "   polio, campak, dan hepatitis B. Pastikan jadwal imunisasi anak tidak terlewat.\n\n"

                                + "2. **Pola Makan Seimbang**\n"
                                + "   Berikan makanan kaya nutrisi seperti sayur, buah, protein, dan susu. "
                                + "   Hindari jajanan tinggi gula dan makanan cepat saji agar imunitas anak tetap kuat.\n\n"

                                + "3. **Cukup Tidur**\n"
                                + "   Anak membutuhkan 10–13 jam tidur sehari untuk mendukung pertumbuhan otak "
                                + "   dan tubuh. Buat rutinitas tidur yang konsisten.\n\n"

                                + "4. **Kebersihan Diri**\n"
                                + "   Ajarkan anak mencuci tangan sebelum makan, setelah bermain, dan setelah dari toilet. "
                                + "   Kebiasaan ini mencegah infeksi bakteri dan virus.\n\n"

                                + "5. **Aktivitas Fisik**\n"
                                + "   Ajak anak bermain di luar rumah setidaknya 1 jam sehari. Aktivitas fisik membantu "
                                + "   perkembangan motorik serta membangun kekuatan tulang dan otot.\n\n"

                                + "Kesehatan anak bukan hanya tanggung jawab dokter, tetapi juga peran penting orang tua "
                                + "dalam membentuk kebiasaan sehat sejak dini.",

                        "Parenting",
                        "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800",
                        java.time.LocalDate.now().toString(),
                        "Dr. Maya Putri, Sp.A"
                ),
                t -> Log.d(TAG, "Dummy health article added")
        );

}

    // Additional access if needed
    public FirebaseFirestore getFirestore() {
        return db;
    }
}
