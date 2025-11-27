package com.ejemplo.bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class CityCommand extends ListenerAdapter {

    record Ciudad(String nombre, int poblacion) {}

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().equals("!topciudades")) return;

        List<Ciudad> ciudades = List.of(
                new Ciudad("Buenos Aires", 15000000),
                new Ciudad("Córdoba", 1400000),
                new Ciudad("Rosario", 1200000),
                new Ciudad("Mendoza", 1100000),
                new Ciudad("La Plata", 900000)
        );

        String resultado = ciudades.stream()
                .sorted((a, b) -> b.poblacion() - a.poblacion())
                .limit(3)
                .map(c -> c.nombre() + " → " + c.poblacion() + " habitantes")
                .reduce("", (a, b) -> a + b + "\n");

        event.getChannel().sendMessage("🏙 **Top 3 ciudades por población:**\n" + resultado).queue();
    }
}

