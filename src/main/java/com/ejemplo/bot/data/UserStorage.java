package com.ejemplo.bot.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {

    private static final String FILE = "usuarios.json";
    private static final Gson gson = new Gson();

    public static Map<String, UserData> load() {
        try (FileReader reader = new FileReader(FILE)) {
            Type type = new TypeToken<HashMap<String, UserData>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static void save(Map<String, UserData> data) {
        try (FileWriter writer = new FileWriter(FILE)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
