package id.example.sehatin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Random;

import id.example.sehatin.R;
import id.example.sehatin.activities.*;
import id.example.sehatin.api.RetrofitClient;
import id.example.sehatin.api.WeatherResponse;
import id.example.sehatin.models.User;
import id.example.sehatin.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private SessionManager sessionManager;
    private User currentUser;
    private TextView tvTemp, tvLocation, tvQuoteContent;
    private ImageView imgWeather;

    // --- STATIC QUOTES ---
    private final String[] HEALTH_QUOTES = {
            "“Kesehatan adalah harta yang paling berharga bagi setiap keluarga.”",
            "“Anak yang sehat adalah awal dari masa depan yang cerah.”",
            "“Imunisasi adalah payung pelindung bagi si kecil di tengah hujan penyakit.”",
            "“Cinta orang tua terlihat dari seberapa peduli mereka pada kesehatan anaknya.”",
            "“Mencegah lebih baik daripada mengobati. Yuk, ke Posyandu rutin!”",
            "“Makanan bergizi hari ini adalah kecerdasan anak di masa depan.”",
            "“Senyum anak sehat adalah kebahagiaan terbesar orang tua.”"
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        currentUser = sessionManager.getUserDetail();

        // 1. Setup Header
        TextView tvWelcome = view.findViewById(R.id.tvWelcomeName);
        if (currentUser != null && currentUser.name != null) {
            tvWelcome.setText(currentUser.name);
        }

        // 2. Setup Weather Widgets
        tvTemp = view.findViewById(R.id.tvTemp);
        tvLocation = view.findViewById(R.id.tvLocation);
        imgWeather = view.findViewById(R.id.imgWeather);

        // Fetch weather for Jakarta (Default)
        // You can change coordinates: -6.2088 (Lat), 106.8456 (Lon)
        fetchWeatherData(-6.2088, 106.8456);

        // 3. Setup Quote Section
        tvQuoteContent = view.findViewById(R.id.tvQuoteContent);
        ImageView btnRefreshQuote = view.findViewById(R.id.btnRefreshQuote);

        randomizeQuote(); // Show random one on load
        btnRefreshQuote.setOnClickListener(v -> randomizeQuote());

        // 4. Setup Feature Buttons (Existing Logic)
        LinearLayout btnJadwal = view.findViewById(R.id.btnJadwal);
        LinearLayout btnInfo = view.findViewById(R.id.btnInfo);
        LinearLayout btnRiwayat = view.findViewById(R.id.btnRiwayat);
        LinearLayout btnChat = view.findViewById(R.id.btnChat);

        btnJadwal.setOnClickListener(v -> startActivity(new Intent(getActivity(), ImunisasiActivity.class)));
        btnInfo.setOnClickListener(v -> startActivity(new Intent(getActivity(), InfoRubrikActivity.class)));
        btnRiwayat.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiwayatActivity.class)));
        btnChat.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddChildActivity.class)));

        // 5. Test Button
        Button btnTest = view.findViewById(R.id.testButton);
        btnTest.setOnClickListener(v -> Toast.makeText(getContext(), "Worker Test Clicked", Toast.LENGTH_SHORT).show());
    }

    private void fetchWeatherData(double lat, double lon) {
        RetrofitClient.getService().getCurrentWeather(lat, lon, true).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().currentWeather != null) {
                    double temp = response.body().currentWeather.temperature;
                    int code = response.body().currentWeather.weathercode;

                    // Update UI
                    tvTemp.setText(String.valueOf(temp));

                    // Simple logic to change icon based on code (WMO Weather interpretation)
                    // 0 = Clear, 1-3 = Cloudy, 45+ = Fog, 51+ = Drizzle, 61+ = Rain
                    if (code <= 3) {
                        // Sunny / Cloudy
                        // You can fetch a real sun icon later, for now using default
                        tvLocation.setText("Jakarta (Cerah/Berawan)");
                    } else if (code >= 51) {
                        // Rain
                        tvLocation.setText("Jakarta (Hujan)");
                    } else {
                        tvLocation.setText("Jakarta");
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WEATHER", "Error fetching weather", t);
                tvTemp.setText("--");
            }
        });
    }

    private void randomizeQuote() {
        int index = new Random().nextInt(HEALTH_QUOTES.length);
        tvQuoteContent.setText(HEALTH_QUOTES[index]);
        // Simple animation
        tvQuoteContent.setAlpha(0f);
        tvQuoteContent.animate().alpha(1f).setDuration(500).start();
    }
}