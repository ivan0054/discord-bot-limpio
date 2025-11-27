package com.ejemplo.bot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TypeCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();

        if (!msg.startsWith("!tipo")) return;

        String[] parts = msg.split(" ", 2);
        if (parts.length < 2) {
            event.getChannel().sendMessage("Uso: `!tipo <valor>`").queue();
            return;
        }

        String value = parts[1];

        Object parsedValue = parseValue(value);

        String result = switch (parsedValue) {
            case Integer i -> "Es un número entero: " + i;
            case Double d -> "Es un número decimal: " + d;
            case Boolean b -> "Es un valor booleano: " + b;
            case String s -> "Es un texto: " + s;
            default -> "Tipo desconocido.";
        };

        event.getChannel().sendMessage(result).queue();
    }

    private Object parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {}

        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {}

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(value);

        return value;
    }
}