package com.example.jobi.Model;

public class Chat {

    private  String sender;
    private  String reciver;
    private  String message;
    private  String type;
    private  String latitud;
    private  String longitud;
    private  String isseen;

    public Chat(String sender, String reciver, String message) {
        this.sender = sender;
        this.reciver = reciver;
        this.message = message;
    }

    public Chat(String sender, String reciver, String message, String type,String isseen) {
        this.sender = sender;
        this.reciver = reciver;
        this.message = message;
        this.type = type;
        this.isseen = isseen;
    }

    public Chat() {
    }

    public Chat(String sender, String reciver, String message, String type, String latitud, String longitud,String isseen) {
        this.sender = sender;
        this.reciver = reciver;
        this.message = message;
        this.type = type;
        this.latitud = latitud;
        this.longitud = longitud;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciver() {
        return reciver;
    }

    public void setReciver(String reciver) {
        this.reciver = reciver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getIsseen() {
        return isseen;
    }

    public void setIsseen(String isseen) {
        this.isseen = isseen;
    }
}
