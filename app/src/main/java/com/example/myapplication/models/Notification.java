package com.example.myapplication.models;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;

    public Notification(String title, String message, boolean isRead) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
    }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
}