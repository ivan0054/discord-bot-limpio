package com.ejemplo.bot.commands;

import com.ejemplo.bot.ConfigManager;
import com.ejemplo.bot.services.CryptoService;
import com.ejemplo.bot.services.CurrencyService;
import com.ejemplo.bot.services.NewsService;
import com.ejemplo.bot.services.WeatherService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CommandHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final ConfigManager config;
    private final WeatherService weatherService;
    private final NewsService newsService;
    private final CryptoService cryptoService;
    private final CurrencyService currencyService;
    private final CommandStats commandStats;

    public CommandHandler(ConfigManager config, WeatherService weatherService, NewsService newsService, CryptoService cryptoService, CurrencyService currencyService) {
        this.config = config;
        this.weatherService = weatherService;
        this.newsService = newsService;
        this.cryptoService = cryptoService;
        this.currencyService = currencyService;
        this.commandStats = new CommandStats();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw().trim();
        String prefix = config.getBotPrefix();
        if (!message.startsWith(prefix)) return;

        String[] parts = message.substring(prefix.length()).split("\\s+");
        String command = parts[0].toLowerCase();
        commandStats.increment();
        logger.info("🎮 Comando ejecutado: {} por {}", message, event.getAuthor().getAsTag());
        event.getChannel().sendTyping().queue();
        try {
            switch (command) {
                case "ping" -> handlePingCommand(event);
                case "ayuda", "help" -> handleHelpCommand(event);
                case "info" -> handleInfoCommand(event);
                case "hora", "tiempo" -> handleTimeCommand(event);
                case "dolar", "usd" -> handleDolarCommand(event);
                case "crypto", "bitcoin" -> handleCryptoCommand(event);
                case "clima" -> handleWeatherCommand(event, parts);
                case "noticias", "news" -> handleNewsCommand(event);
                case "stats", "estadisticas", "estadísticas" -> handleStatsCommand(event);
                default -> handleUnknownCommand(event, command);
            }
        } catch (Exception e) {
            logger.error("💥 Error procesando comando {}: {}", command, e.getMessage());
            event.getChannel().sendMessage("❌ Error interno del bot. Intenta nuevamente.").queue();
        }
    }

    private void handlePingCommand(MessageReceivedEvent event) {
        event.getChannel().sendMessage("🏓 Pong! Bot funcionando correctamente.").queue();
    }

    private void handleHelpCommand(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📋 Lista de Comandos Disponibles");
        embed.setColor(Color.BLUE);
        embed.setDescription("Aquí tienes todos los comandos que puedes usar:");
        embed.addField("🏓 Básicos",
                "`!ping` - Verificar si el bot funciona\n" +
                        "`!info` - Información sobre el bot\n" +
                        "`!hora` / `!tiempo` - Fecha y hora actual", false);
        embed.addField("💰 Finanzas",
                "`!dolar` / `!usd` - Cotización del dólar\n" +
                        "`!crypto` / `!bitcoin` - Precios de criptomonedas", false);
        embed.addField("🌍 Servicios",
                "`!clima [ciudad]` - Información del clima\n" +
                        "`!noticias` / `!news` - Últimas noticias", false);
        embed.addField("ℹ️ Ayuda",
                "`!ayuda` / `!help` - Mostrar esta ayuda", false);
        embed.setFooter("Prefijo: " + config.getBotPrefix());
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleInfoCommand(MessageReceivedEvent event) {
        event.getChannel().sendMessage("TÍO BOT - proporciona clima, noticias, cotizaciones y más.").queue();
    }

    private void handleTimeCommand(MessageReceivedEvent event) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        event.getChannel().sendMessage("🕒 Hora actual: " + now).queue();
    }

    private void handleDolarCommand(MessageReceivedEvent event) {
        try {
            String res = currencyService.getDollarRate();
            event.getChannel().sendMessage(res).queue();
        } catch (Exception e) {
            logger.error("Error obteniendo dólar: {}", e.getMessage());
            event.getChannel().sendMessage("❌ Error al obtener cotización del dólar.").queue();
        }
    }

    private void handleCryptoCommand(MessageReceivedEvent event) {
        try {
            String res = cryptoService.getCryptoPrices();
            event.getChannel().sendMessage(res).queue();
        } catch (Exception e) {
            logger.error("Error obteniendo crypto: {}", e.getMessage());
            event.getChannel().sendMessage("❌ Error al obtener precios de criptomonedas.").queue();
        }
    }

    private void handleWeatherCommand(MessageReceivedEvent event, String[] parts) {
        if (parts.length < 2) {
            event.getChannel().sendMessage("❌ Debes especificar una ciudad. Ejemplo: `!clima Buenos Aires`").queue();
            return;
        }
        String city = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        try {
            String result = weatherService.getWeather(city);
            event.getChannel().sendMessage(result).queue();
        } catch (Exception e) {
            logger.error("Error obteniendo clima para {}: {}", city, e.getMessage());
            event.getChannel().sendMessage("❌ Error al obtener información del clima para: " + city).queue();
        }
    }

    private void handleNewsCommand(MessageReceivedEvent event) {
        try {
            String res = newsService.getLatestNews();
            event.getChannel().sendMessage(res).queue();
        } catch (Exception e) {
            logger.error("Error obteniendo noticias: {}", e.getMessage());
            event.getChannel().sendMessage("❌ Error al obtener las últimas noticias.").queue();
        }
    }
    private void handleStatsCommand(MessageReceivedEvent event) {
        int total = commandStats.getTotal();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Estadísticas del Bot");
        embed.setColor(Color.MAGENTA);

        embed.addField("📌 Comandos ejecutados", String.valueOf(total), false);
        embed.addField("👤 Usuario", event.getAuthor().getAsTag(), false);
        embed.addField("🕒 Hora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false);

        embed.setFooter("TÍO BOT • Estadísticas en tiempo real", event.getJDA().getSelfUser().getAvatarUrl());

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private void handleUnknownCommand(MessageReceivedEvent event, String command) {
        event.getChannel().sendMessage("❓ Comando desconocido: " + command).queue();
    }


}
