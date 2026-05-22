package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

public class Chapter {
    @SerializedName("id")
    private String id;

    @SerializedName("course_id")
    private String courseId;

    @SerializedName("title")
    private String title;

    @SerializedName("order_index")
    private int orderIndex;

    public Chapter() {
    }

    public Chapter(String id, String courseId, String title, int orderIndex) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Chapter deepCopy() {
        return new Chapter(id, courseId, title, orderIndex);
    }
}
