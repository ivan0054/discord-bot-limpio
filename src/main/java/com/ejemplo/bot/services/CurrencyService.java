package com.ejemplo.bot.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private static final String DOLLAR_API_URL = "https://api.bluelytics.com.ar/v2/latest";
    private final OkHttpClient httpClient;

    public CurrencyService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public String getDollarRate() throws Exception {
        Request request = new Request.Builder().url(DOLLAR_API_URL).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la API de BluelyTics: " + response.code());
            }
            String body = response.body().string();
            return formatDollarResponse(body);
        } catch (IOException e) {
            logger.error("Error consultando API de BluelyTics: {}", e.getMessage());
            throw new Exception("Error conectando con el servicio de cotizaciones");
        }
    }

    private String formatDollarResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject oficial = json.getJSONObject("oficial");
            double oficialCompra = oficial.getDouble("value_buy");
            double oficialVenta = oficial.getDouble("value_sell");
            JSONObject blue = json.getJSONObject("blue");
            double blueCompra = blue.getDouble("value_buy");
            double blueVenta = blue.getDouble("value_sell");
            double brecha = ((blueVenta - oficialVenta) / oficialVenta) * 100.0;
            String lastUpdate = json.optString("last_update", "");
            Date updateDate = new Date();
            String formattedDate = lastUpdate.isEmpty() ? "" : lastUpdate;
            StringBuilder result = new StringBuilder();
            result.append("💵 **Cotización del Dólar en Argentina**\n\n");
            result.append(String.format("🏛️ **Dólar Oficial**\n💰 Compra: $%.2f\n💸 Venta: $%.2f\n\n", oficialCompra, oficialVenta));
            result.append(String.format("🔵 **Dólar Blue**\n💰 Compra: $%.2f\n💸 Venta: $%.2f\n\n", blueCompra, blueVenta));
            result.append(String.format("📊 **Brecha:** %.1f%%\n\n", brecha));
            result.append(String.format("🕐 *Última actualización: %s*", formattedDate));
            return result.toString();
        } catch (Exception e) {
            logger.error("Error formateando respuesta del dólar: {}", e.getMessage());
            return "❌ Error procesando cotización del dólar.";
        }
    }
}
