package com.example.myapplication.models;

import java.io.Serializable;

public class Course implements Serializable {
    private String id;
    private String title;
    private String instructorId;
    private String description;
    private double price;
    private String thumbnailUrl;
    private String status; // Pending, Approved, Rejected
    private float rating;
    private int studentCount;

    public Course() {
        // Default constructor for Firebase
    }

    public Course(String id, String title, String instructorId, String description, double price) {
        this.id = id;
        this.title = title;
        this.instructorId = instructorId;
        this.description = description;
        this.price = price;
        this.status = "Pending";
        this.rating = 0.0f;
        this.studentCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
