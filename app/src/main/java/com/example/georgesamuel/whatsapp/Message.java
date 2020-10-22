package com.example.georgesamuel.whatsapp;

public class Message {

    private String message, from, type;

    public Message() {

    }
    
    public Message(String message, String from, String type) {
        this.message = message;
        this.from = from;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
