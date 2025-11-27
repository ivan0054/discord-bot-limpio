package com.ejemplo.bot;

import com.ejemplo.bot.commands.CityCommand;
import com.ejemplo.bot.commands.CommandHandler;
import com.ejemplo.bot.commands.TypeCommand;
import com.ejemplo.bot.commands.UserCommand;
import com.ejemplo.bot.services.CryptoService;
import com.ejemplo.bot.services.CurrencyService;
import com.ejemplo.bot.services.NewsService;
import com.ejemplo.bot.services.WeatherService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) {

        ConfigManager config = new ConfigManager();
        config.validateConfiguration();

        try {

            JDA jda = JDABuilder.createDefault(config.getBotToken())
                    .enableIntents(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .build();

            WeatherService weatherService = new WeatherService(config);
            NewsService newsService = new NewsService(config);
            CryptoService cryptoService = new CryptoService();
            CurrencyService currencyService = new CurrencyService();
            CommandHandler handler = new CommandHandler(
                    config,
                    weatherService,
                    newsService,
                    cryptoService,
                    currencyService
            );

            jda.addEventListener(handler);
            jda.addEventListener(new TypeCommand());
            jda.addEventListener(new UserCommand());
            jda.addEventListener(new CityCommand());

            System.out.println("Bot started and ready.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al iniciar el bot.");
        }
    }
}