package com.ejemplo.bot.data;

public class UserData {
    private String nombre;
    private int edad;

    public UserData(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    public String getNombre() { return nombre; }
    public int getEdad() { return edad; }
}

