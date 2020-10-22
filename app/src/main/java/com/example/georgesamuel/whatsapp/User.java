package com.example.georgesamuel.whatsapp;

import java.io.Serializable;

public class User implements Serializable {

    private String name = "";
    private String status = "";
    private String imagePath = "";
    private String uid =  "";

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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getUserId() {
        return uid;
    }

    public void setUserId(String userId) {
        this.uid = userId;
    }
}
