package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class Enrollment {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("course_id")
    private String courseId;

    @SerializedName("enrolled_at")
    private String enrolledAt;

    @SerializedName("paid_amount")
    private double paidAmount;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("progress")
    private int progress;

    @SerializedName("total_lessons")
    private int totalLessons;

    @SerializedName("is_completed")
    private boolean isCompleted;

    @SerializedName("courses")
    private Course course;

    public Enrollment() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(String enrolledAt) { this.enrolledAt = enrolledAt; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}