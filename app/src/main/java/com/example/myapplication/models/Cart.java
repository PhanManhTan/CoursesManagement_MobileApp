package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class Cart {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("course_id")
    private String courseId;

    // Supabase Join: select=*,courses(*)
    @SerializedName("courses")
    private Course course;

    public Cart() {
    }

    public Cart(String id, String userId, String courseId) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}