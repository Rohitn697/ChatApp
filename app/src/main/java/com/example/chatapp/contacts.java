package com.example.chatapp;

public class contacts {
    private String name,status,image;

    public contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }
    public contacts()
    {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
