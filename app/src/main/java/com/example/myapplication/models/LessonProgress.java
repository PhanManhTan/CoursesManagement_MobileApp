package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LessonProgress implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("lesson_id")
    private String lessonId;

    @SerializedName("is_completed")
    private boolean isCompleted;

    @SerializedName("watch_time_seconds")
    private int watchTimeSeconds;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public LessonProgress() {
    }

    public LessonProgress(String id, String userId, String lessonId, boolean isCompleted, int watchTimeSeconds, String completedAt, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.lessonId = lessonId;
        this.isCompleted = isCompleted;
        this.watchTimeSeconds = watchTimeSeconds;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getWatchTimeSeconds() {
        return watchTimeSeconds;
    }

    public void setWatchTimeSeconds(int watchTimeSeconds) {
        this.watchTimeSeconds = watchTimeSeconds;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}