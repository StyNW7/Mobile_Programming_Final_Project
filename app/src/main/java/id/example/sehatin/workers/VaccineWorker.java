package id.example.sehatin.workers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import id.example.sehatin.R;

public class VaccineWorker extends Worker {

    public VaccineWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Logika ini berjalan di background thread
        Log.d("VaccineWorker", "Sedang mengecek jadwal imunisasi...");

        // cek apakah user sudah login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return Result.success(); // kl ga ada user, stop kerja.
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Tambah 1 hari = Besok
        String tomorrowDate = sdf.format(calendar.getTime());

        Log.d("VaccineWorker", "Mencari jadwal untuk tanggal: " + tomorrowDate);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            QuerySnapshot querySnapshot = Tasks.await(
                    db.collection("vaccineSchedules")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("scheduledDate", tomorrowDate)
                            .whereEqualTo("isCompleted", false)
                            .get()
            );

            if (!querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    String vaccineName = doc.getString("vaccineName");
                    String childId = doc.getString("childId");

                    showNotification("Jadwal Imunisasi Besok!", "Jangan lupa vaksin " + vaccineName + " untuk si Kecil.");
                }
            } else {
                Log.d("VaccineWorker", "Tidak ada jadwal untuk besok.");
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.e("VaccineWorker", "Gagal ambil data", e);
            return Result.retry();
        }

        return Result.success();
    }

    // helper utk munculin notif
    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "vaccine_channel_id";

        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationChannel channel = new NotificationChannel(channelId, "Pengingat Vaksin", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_logo_sehatin) // Ganti icon app kamu
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // cek hp user Android 13 ke atas ato bkn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("VaccineWorker", "Gagal kirim notif: Izin tidak diberikan user.");
                return;
            }
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
