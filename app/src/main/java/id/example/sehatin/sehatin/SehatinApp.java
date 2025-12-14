package id.example.sehatin.sehatin;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class SehatinApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Force Light Mode for the whole app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}