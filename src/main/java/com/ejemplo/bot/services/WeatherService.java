package com.ejemplo.bot.services;

import com.ejemplo.bot.ConfigManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather";
    private final ConfigManager config;
    private final OkHttpClient httpClient;

    public WeatherService(ConfigManager config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public String getWeather(String city) throws Exception {
        String apiKey = config.getWeatherApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("API_KEY_CLIMA")) {
            return "❌ API key del clima no configurada. Contacta al administrador.";
        }
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=es", WEATHER_API_URL, city, apiKey);
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) return "❌ Ciudad no encontrada: " + city;
                throw new IOException("Error en la API: " + response.code());
            }
            String body = response.body().string();
            return formatWeatherResponse(body, city);
        } catch (IOException e) {
            logger.error("Error consultando API del clima: {}", e.getMessage());
            throw new Exception("Error conectando con el servicio del clima");
        }
    }

    private String formatWeatherResponse(String jsonResponse, String city) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject main = json.getJSONObject("main");
            JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
            JSONObject wind = json.optJSONObject("wind");
            JSONObject sys = json.optJSONObject("sys");

            double temp = main.getDouble("temp");
            double feelsLike = main.optDouble("feels_like", temp);
            int humidity = main.optInt("humidity", 0);
            double pressure = main.optDouble("pressure", 0);
            String description = weather.optString("description", "");
            double windSpeed = wind != null ? wind.optDouble("speed", 0) : 0;
            String country = sys != null ? sys.optString("country", "") : "";

            StringBuilder result = new StringBuilder();
            result.append(String.format("🌤️ **Clima en %s, %s**\n\n", city, country));
            result.append(String.format("🌡️ **Temperatura:** %d°C\n", Math.round(temp)));
            result.append(String.format("🤲 **Sensación térmica:** %d°C\n", Math.round(feelsLike)));
            result.append(String.format("📝 **Descripción:** %s\n\n", capitalize(description)));
            result.append(String.format("💧 **Humedad:** %d%%\n", humidity));
            result.append(String.format("🏔️ **Presión:** %d hPa\n", Math.round(pressure)));
            result.append(String.format("💨 **Viento:** %.1f m/s", windSpeed));
            return result.toString();
        } catch (Exception e) {
            logger.error("Error formateando respuesta del clima: {}", e.getMessage());
            return "❌ Error procesando información del clima.";
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
