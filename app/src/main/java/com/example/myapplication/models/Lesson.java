package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Lesson implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("chapter_id")
    private String chapterId;

    @SerializedName("title")
    private String title;

    @SerializedName("content_type")
    private String contentType;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("document_url")
    private String documentUrl;

    @SerializedName("order_index")
    private int orderIndex;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("duration")
    private String duration;

    @SerializedName("content")
    private String content;

    public Lesson() {
    }

    public Lesson(String id, String chapterId, String title, String contentType, String videoUrl, String documentUrl, int orderIndex, String createdAt) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.contentType = contentType;
        this.videoUrl = videoUrl;
        this.documentUrl = documentUrl;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
    }

    public Lesson(String id, String title, String duration) {
        this.id = id;
        this.title = title;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}