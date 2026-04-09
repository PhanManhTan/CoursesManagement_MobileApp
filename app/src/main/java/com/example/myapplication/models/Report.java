package com.example.myapplication.models;

import java.io.Serializable;

public class Report implements Serializable {
    private String id;
    private String contentId; // ID of the course or lecture being reported
    private String contentType; // Course, Lecture, etc.
    private String reporterId;
    private String reason;
    private long timestamp;
    private String status; // Pending, Resolved

    public Report() {
        // Default constructor for Firebase
    }

    public Report(String id, String contentId, String contentType, String reporterId, String reason) {
        this.id = id;
        this.contentId = contentId;
        this.contentType = contentType;
        this.reporterId = reporterId;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.status = "Pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
