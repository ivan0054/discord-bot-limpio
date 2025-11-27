package com.ejemplo.bot.commands;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandStats {

    private final AtomicInteger totalCommands = new AtomicInteger(0);
    private final File statsFile = new File("stats.json");

    public CommandStats() {
        loadStats();
    }

    public void increment() {
        totalCommands.incrementAndGet();
        saveStats();
    }

    public int getTotal() {
        return totalCommands.get();
    }

    private void saveStats() {
        try (FileWriter writer = new FileWriter(statsFile)) {
            JSONObject json = new JSONObject();
            json.put("totalCommands", totalCommands.get());
            writer.write(json.toString(4));
        } catch (IOException e) {
            System.err.println("Error guardando estadísticas: " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            if (!statsFile.exists()) {
                saveStats();
                return;
            }

            String content = new String(Files.readAllBytes(statsFile.toPath()));
            JSONObject json = new JSONObject(content);

            int savedTotal = json.getInt("totalCommands");
            totalCommands.set(savedTotal);

        } catch (Exception e) {
            System.err.println("Error cargando estadísticas: " + e.getMessage());
        }
    }
}