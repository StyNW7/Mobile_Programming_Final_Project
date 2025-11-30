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
        Log.d("VaccineWorker", "Sedang mengecek jadwal imunisasi...");

        // cek apakah user sudah login -> kl udah ada logic login baru uncomment ini
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            return Result.success(); // kl ga ada user, stop kerja.
//        }
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userId = "user_tes_124";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // cek untuk hari ini (hari-h)
        String todayDate = sdf.format(calendar.getTime());
        checkAndNotify(userId, todayDate, "HARI INI!", "Segera kunjungi Puskesmas/Posyandu sekarang.");

        // cek untuk besok
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrowDate = sdf.format(calendar.getTime());
        checkAndNotify(userId, tomorrowDate, "BESOK!", "Siapkan berkas imunisasi untuk besok.");

        return Result.success();
    }

    private void checkAndNotify(String userId, String dateToCheck, String titleSuffix, String messageBody) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            Log.d("VaccineWorker", "Cek jadwal tgl: " + dateToCheck);

            QuerySnapshot querySnapshot = Tasks.await(
                    db.collection("vaccineSchedules")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("scheduledDate", dateToCheck) // Cek tanggal sesuai parameter
                            .whereEqualTo("isCompleted", false)
                            .get()
            );

            if (!querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    String vaccineName = doc.getString("vaccineName");

                    // Munculkan notifikasi
                    showNotification(
                            "Jadwal Imunisasi " + titleSuffix,
                            "Vaksin " + vaccineName + ". " + messageBody
                    );
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.e("VaccineWorker", "Gagal ambil data tgl " + dateToCheck, e);
        }
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
