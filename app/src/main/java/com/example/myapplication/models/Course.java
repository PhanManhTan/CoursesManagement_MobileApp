package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Course implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("instructor_id")
    private String instructorId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("discount_price")
    private double discountPrice;

    @SerializedName("status")
    private String status;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("rating")
    private float rating;

    @SerializedName("student_count")
    private int studentCount;

    @SerializedName("lesson_count")
    private int lessonCount;

    @SerializedName("duration")
    private String duration;

    private int thumbnailResId;

    public Course() {
    }

    public Course(String id, String instructorId, String title, String description, String thumbnailUrl, double price, double discountPrice, String status, String categoryId, String createdAt) {
        this.id = id;
        this.instructorId = instructorId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.discountPrice = discountPrice;
        this.status = status;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
    }

    public Course(String id, String title, int lessonCount, String duration, double price, String status, int thumbnailResId) {
        this.id = id;
        this.title = title;
        this.lessonCount = lessonCount;
        this.duration = duration;
        this.price = price;
        this.status = status;
        this.thumbnailResId = thumbnailResId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public void setThumbnailResId(int thumbnailResId) {
        this.thumbnailResId = thumbnailResId;
    }
}