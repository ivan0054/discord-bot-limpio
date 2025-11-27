package com.ejemplo.bot.services;

import com.ejemplo.bot.ConfigManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private static final String NEWS_API_URL = "https://newsapi.org/v2/top-headlines?country=ar";
    private final ConfigManager config;
    private final OkHttpClient httpClient;

    public NewsService(ConfigManager config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public String getLatestNews() throws Exception {
        String apiKey = config.getNewsApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("API_KEY_NOTICIAS")) {
            return "❌ API key de noticias no configurada. Contacta al administrador.";
        }
        String url = NEWS_API_URL + "&pageSize=5&apiKey=" + apiKey;
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la API de noticias: " + response.code());
            }
            String body = response.body().string();
            return formatNewsResponse(body);
        } catch (IOException e) {
            logger.error("Error consultando API de noticias: {}", e.getMessage());
            throw new Exception("Error conectando con el servicio de noticias");
        }
    }

    private String formatNewsResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray articles = json.getJSONArray("articles");
            if (articles.length() == 0) return "📰 No se encontraron noticias en este momento.";
            StringBuilder result = new StringBuilder();
            result.append("📰 **Últimas Noticias de Argentina**\n\n");
            for (int i = 0; i < Math.min(5, articles.length()); i++) {
                JSONObject article = articles.getJSONObject(i);
                String title = article.optString("title", "Sin título");
                String description = article.optString("description", "");
                String source = article.optJSONObject("source") != null ? article.getJSONObject("source").optString("name", "Fuente desconocida") : "Fuente desconocida";
                String url = article.optString("url", "");
                result.append(String.format("%d. %s\n", i+1, title));
                if (!description.isEmpty() && !description.equals("null")) {
                    if (description.length() > 150) description = description.substring(0,147) + "...";
                    result.append("📝 " + description + "\n\n");
                }
                result.append("🏢 **Fuente:** " + source + "\n");
                if (!url.isEmpty() && !url.equals("null")) {
                    result.append("🔗 [Leer más](" + url + ")\n");
                }
                result.append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Error formateando respuesta de noticias: {}", e.getMessage());
            return "❌ Error procesando las noticias.";
        }
    }
}
