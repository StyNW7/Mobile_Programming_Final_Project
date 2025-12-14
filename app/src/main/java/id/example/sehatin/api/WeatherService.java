package id.example.sehatin.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    // Using Open-Meteo API (Free, No Key)
    // Example: https://api.open-meteo.com/v1/forecast?latitude=-6.2&longitude=106.8&current_weather=true
    @GET("v1/forecast")
    Call<WeatherResponse> getCurrentWeather(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("current_weather") boolean currentWeather
    );
}