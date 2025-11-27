package com.ejemplo.bot.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CryptoService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private static final String CRYPTO_API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,cardano,polkadot&vs_currencies=usd&include_24hr_change=true";
    private final OkHttpClient httpClient;
    private final NumberFormat currencyFormatter;

    public CryptoService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public String getCryptoPrices() throws Exception {
        Request request = new Request.Builder().url(CRYPTO_API_URL).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la API de CoinGecko: " + response.code());
            }
            String body = response.body().string();
            return formatCryptoResponse(body);
        } catch (IOException e) {
            logger.error("Error consultando API de CoinGecko: {}", e.getMessage());
            throw new Exception("Error conectando con el servicio de criptomonedas");
        }
    }

    private String formatCryptoResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder();
            result.append("₿ **Precios de Criptomonedas**\n\n");
            if (json.has("bitcoin")) {
                JSONObject btc = json.getJSONObject("bitcoin");
                double price = btc.getDouble("usd");
                double change24h = btc.optDouble("usd_24h_change", 0.0);
                String changeEmoji = change24h >= 0 ? "📈" : "📉";
                String changeSign = change24h >= 0 ? "+" : "";
                result.append("**🟠 Bitcoin (BTC)**\n");
                result.append(String.format("💰 Precio: %s\n\n", currencyFormatter.format(price)));
                result.append(String.format("%s 24h: %s%.2f%%\n\n", changeEmoji, changeSign, change24h));
            }
            if (json.has("ethereum")) {
                JSONObject eth = json.getJSONObject("ethereum");
                double price = eth.getDouble("usd");
                double change24h = eth.optDouble("usd_24h_change", 0.0);
                String changeEmoji = change24h >= 0 ? "📈" : "📉";
                String changeSign = change24h >= 0 ? "+" : "";
                result.append("**🔵 Ethereum (ETH)**\n");
                result.append(String.format("💰 Precio: %s\n\n", currencyFormatter.format(price)));
                result.append(String.format("%s 24h: %s%.2f%%\n\n", changeEmoji, changeSign, change24h));
            }
            // cardano and polkadot similar...
            result.append("📊 *Datos proporcionados por CoinGecko*");
            return result.toString();
        } catch (Exception e) {
            logger.error("Error formateando respuesta de criptomonedas: {}", e.getMessage());
            return "❌ Error procesando precios de criptomonedas.";
        }
    }
}
