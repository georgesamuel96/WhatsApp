package com.example.georgesamuel.whatsapp;

public class Message {

    private String message, from, type, messageId, fileName;

    public Message() {

    }

    public Message(String message, String from, String type, String messageId, String fileName) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.messageId = messageId;
        this.fileName = fileName;
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
