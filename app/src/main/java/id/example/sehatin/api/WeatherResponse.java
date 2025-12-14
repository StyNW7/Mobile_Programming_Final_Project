package id.example.sehatin.api;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("current_weather")
    public CurrentWeather currentWeather;

    public static class CurrentWeather {
        @SerializedName("temperature")
        public double temperature;
        @SerializedName("windspeed")
        public double windspeed;
        @SerializedName("weathercode")
        public int weathercode;
    }
}