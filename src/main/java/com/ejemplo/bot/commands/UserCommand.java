package com.ejemplo.bot.commands;

import com.ejemplo.bot.data.UserData;
import com.ejemplo.bot.data.UserStorage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

public class UserCommand extends ListenerAdapter {
    private final Map<String, UserData> usuarios = UserStorage.load();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();

        if (msg.startsWith("!guardar")) {
            try {
                String[] partes = msg.replace("!guardar ", "").split(" ");
                String nombre = partes[0].split("=")[1];
                int edad = Integer.parseInt(partes[1].split("=")[1]);

                usuarios.put(nombre, new UserData(nombre, edad));
                UserStorage.save(usuarios);

                event.getChannel().sendMessage("Datos guardados correctamente.").queue();

            } catch (Exception e) {
                event.getChannel().sendMessage("Formato incorrecto. Ejemplo:\n`!guardar nombre=Ivan edad=20`").queue();
            }
        }

        if (msg.startsWith("!cargar")) {
            String nombre = msg.replace("!cargar ", "").trim();

            UserData data = usuarios.get(nombre);

            if (data == null) {
                event.getChannel().sendMessage("No hay datos guardados para ese usuario.").queue();
            } else {
                event.getChannel().sendMessage("Usuario: " + data.getNombre() + "\nEdad: " + data.getEdad()).queue();
            }
        }
    }
}

