package com.example.jobi.Model;

public class putPDF {

    private String name;
    private String url;

    public putPDF() {

    }
    public putPDF(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
