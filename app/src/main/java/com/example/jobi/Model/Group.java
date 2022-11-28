package com.example.jobi.Model;

public class Group {
    private String id;
    private String nombre;
    private String integrantes;
    private String imagenURL;

    public Group() {
    }

    public Group(String id) {
        this.id = id;
    }

    public Group(String id, String nombre, String imagenURL) {
        this.id = id;
        this.nombre = nombre;
        this.imagenURL = imagenURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(String integrantes) {
        this.integrantes = integrantes;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }
}
