package com.example.jobi.Model;

public class User {

    private String id;
    private String usuario;
    private String password;
    private String email;
    private String imagenURL;
    private String status;

    public User(String id, String usuario, String password, String email, String imagenURL,String status){
        this.id = id;
        this.usuario = usuario;
        this.password = password;
        this.email = email;
        this.imagenURL = imagenURL;
        this.status = status;
    }
    public User(String id, String usuario, String password, String email, String imagenURL){
        this.id = id;
        this.usuario = usuario;
        this.password = password;
        this.email = email;
        this.imagenURL = imagenURL;

    }
    public User(String email, String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
        this.email = email;
    }

    public User() {
    }

    public User(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return usuario;
    }

    public void setUsername(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagenURL() {
        return imagenURL;
    }

    public void setImagenURL(String imagenURL) {
        this.imagenURL = imagenURL;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
