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
    private static final String COLL_DOCTORS = "doctors";
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

    public void getDoctors(OnCompleteListener<QuerySnapshot> onComplete) {
        db.collection(COLL_DOCTORS)
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

        // Add after your first article
        addHealthArticle(
                new HealthArticle(
                        null,
                        "Panduan Lengkap Imunisasi Anak 0-24 Bulan",
                        "Imunisasi adalah investasi kesehatan terbaik untuk anak. Berikut panduan lengkap imunisasi dari lahir hingga 2 tahun:\n\n" +

                                "**0 Bulan (Saat Lahir):**\n" +
                                "• Hepatitis B-0: Mencegah hepatitis B\n" +
                                "• Polio-0: Mencegah polio\n\n" +

                                "**1 Bulan:**\n" +
                                "• BCG: Mencegah TBC\n" +
                                "• Hepatitis B-1\n\n" +

                                "**2 Bulan:**\n" +
                                "• DPT-HB-Hib 1: Mencegah difteri, pertusis, tetanus, hepatitis B, dan HiB\n" +
                                "• Polio 1\n" +
                                "• Rotavirus 1 (opsional)\n\n" +

                                "**3 Bulan:**\n" +
                                "• DPT-HB-Hib 2\n" +
                                "• Polio 2\n\n" +

                                "**4 Bulan:**\n" +
                                "• DPT-HB-Hib 3\n" +
                                "• Polio 3\n" +
                                "• Rotavirus 2 (opsional)\n\n" +

                                "**9 Bulan:**\n" +
                                "• Campak/MR: Mencegah campak dan rubella\n\n" +

                                "**12 Bulan:**\n" +
                                "• PCV 4: Mencegah pneumonia\n" +
                                "• JE (di daerah endemis)\n\n" +

                                "**18-24 Bulan:**\n" +
                                "• DPT-HB-Hib 4\n" +
                                "• Campak/MR 2\n\n" +

                                "**Catatan Penting:**\n" +
                                "• Bawa buku KIA ke setiap kunjungan\n" +
                                "• Jangan tunda jadwal imunisasi\n" +
                                "• Pantau reaksi pasca imunisasi\n" +
                                "• Konsultasi dokter jika anak sakit saat jadwal imunisasi",

                        "Imunisasi",
                        "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=800",
                        java.time.LocalDate.now().minusDays(1).toString(),
                        "Dr. Ahmad Fauzi, Sp.A"
                ),
                t -> Log.d(TAG, "Dummy article 2 added")
        );

        addHealthArticle(
                new HealthArticle(
                        null,
                        "Makanan Pendamping ASI (MPASI) yang Tepat untuk Bayi 6-12 Bulan",
                        "MPASI mulai diberikan saat bayi berusia 6 bulan. Berikut panduan lengkap MPASI:\n\n" +

                                "**Prinsip MPASI:**\n" +
                                "1. **Tepat Waktu:** Mulai usia 6 bulan\n" +
                                "2. **Adekuat:** Cukup energi, protein, dan mikronutrien\n" +
                                "3. **Aman:** Higienis dan bebas kontaminasi\n" +
                                "4. **Diberikan dengan cara yang benar:** Responsif feeding\n\n" +

                                "**Tekstur MPASI Berdasarkan Usia:**\n" +
                                "• **6-8 bulan:** Bubur halus/saring\n" +
                                "• **9-11 bulan:** Makanan lumat/lembut\n" +
                                "• **12-24 bulan:** Makanan keluarga\n\n" +

                                "**Menu Harian Contoh (8 bulan):**\n" +
                                "• **Pagi:** Bubur beras + hati ayam + wortel\n" +
                                "• **Siang:** Puree kentang + ikan + bayam\n" +
                                "• **Sore:** Bubur buah pisang + alpukat\n" +
                                "• **Makan selingan:** Yoghurt atau buah\n\n" +

                                "**Makanan yang Harus Dihindari:**\n" +
                                "• Madu (risiko botulisme)\n" +
                                "• Garam dan gula berlebihan\n" +
                                "• Makanan keras yang bisa menyebabkan tersedak\n" +
                                "• Makanan instan/cepat saji\n\n" +

                                "**Tips Sukses MPASI:**\n" +
                                "• Buat jadwal makan teratur\n" +
                                "• Variasikan menu setiap hari\n" +
                                "• Perkenalkan satu per satu jenis makanan\n" +
                                "• Sabar saat bayi menolak makanan baru\n" +
                                "• Jangan memaksa bayi makan",

                        "Nutrisi",
                        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
                        java.time.LocalDate.now().minusDays(2).toString(),
                        "Dr. Sari Indah, Sp.GK"
                ),
                t -> Log.d(TAG, "Dummy article 3 added")
        );

        addHealthArticle(
                new HealthArticle(
                        null,
                        "Cegah Stunting Sejak Dini: Panduan untuk Orang Tua",
                        "Stunting adalah kondisi gagal tumbuh pada anak akibat kekurangan gizi kronis. Berikut cara mencegahnya:\n\n" +

                                "**Tanda-tanda Stunting:**\n" +
                                "• Tinggi badan di bawah standar\n" +
                                "• Perkembangan motorik terlambat\n" +
                                "• Mudah sakit\n" +
                                "• Performa kognitif kurang optimal\n\n" +

                                "**Penyebab Stunting:**\n" +
                                "1. **Faktor Ibu:**\n" +
                                "   - Kekurangan gizi selama hamil\n" +
                                "   - Usia terlalu muda atau tua saat hamil\n" +
                                "   - Penyakit kronis\n" +
                                "   - Jarak kehamilan terlalu dekat\n\n" +

                                "2. **Faktor Bayi:**\n" +
                                "   - Tidak mendapat ASI eksklusif\n" +
                                "   - MPASI tidak tepat\n" +
                                "   - Infeksi berulang\n" +
                                "   - Lingkungan tidak bersih\n\n" +

                                "**Langkah Pencegahan:**\n" +
                                "**Sebelum Hamil:**\n" +
                                "• Konsumsi makanan bergizi\n" +
                                "• Konsumsi asam folat\n" +
                                "• Periksa kesehatan secara rutin\n\n" +

                                "**Selama Hamil:**\n" +
                                "• Kontrol kehamilan minimal 4 kali\n" +
                                "• Konsumsi tablet tambah darah\n" +
                                "• Makan makanan bergizi seimbang\n" +
                                "• Istirahat cukup\n\n" +

                                "**Setelah Lahir:**\n" +
                                "• Beri ASI eksklusif 6 bulan\n" +
                                "• MPASI tepat waktu dan bergizi\n" +
                                "• Imunisasi lengkap\n" +
                                "• Pantau tumbuh kembang di posyandu\n" +
                                "• Jaga kebersihan lingkungan\n\n" +

                                "**Peran Posyandu:**\n" +
                                "• Pemantauan berat dan tinggi badan bulanan\n" +
                                "• Edukasi gizi\n" +
                                "• Distribusi vitamin A\n" +
                                "• Imunisasi\n\n" +

                                "Stunting dapat dicegah dengan intervensi yang tepat sejak dini!",

                        "Stunting",
                        "https://images.unsplash.com/photo-1582750433449-648ed127bb54?w=800",
                        java.time.LocalDate.now().minusDays(3).toString(),
                        "Dr. Bambang Wijaya, Sp.A(K)"
                ),
                t -> Log.d(TAG, "Dummy article 4 added")
        );

}

    // Additional access if needed
    public FirebaseFirestore getFirestore() {
        return db;
    }

}
