package com.example.jobi.Model;

public class SubGroup {
    private String id;
    private String group_id;
    private String nombre;
    private String integrantes;
    private String imagenURL;

    public SubGroup() {
    }

    public SubGroup(String id) {
        this.id = id;
    }

    public SubGroup(String id, String group_id, String nombre, String imagenURL) {
        this.id = id;
        this.group_id = group_id;
        this.nombre = nombre;
        this.imagenURL = imagenURL;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
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
