package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @SerializedName("duration_seconds")
    private int durationSeconds;

    @SerializedName("content")
    private Object content;

    private transient String localVideoName;
    private transient String localVideoUri;
    private transient String localThumbnailName;
    private transient String localThumbnailUri;
    private transient List<String> localFileNames = new ArrayList<>();
    private transient List<String> localFileUris = new ArrayList<>();

    public Lesson() {
    }

    public Lesson(String id, String chapterId, String title, String contentType, String videoUrl, String documentUrl, int orderIndex, String createdAt, int durationSeconds) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.contentType = contentType;
        this.videoUrl = videoUrl;
        this.documentUrl = documentUrl;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
        this.durationSeconds = durationSeconds;
    }

    public Lesson(String id, String title, int durationSeconds) {
        this.id = id;
        this.title = title;
        this.durationSeconds = durationSeconds;
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

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getLocalVideoName() {
        return localVideoName;
    }

    public String getLocalVideoUri() {
        return localVideoUri;
    }

    public void setLocalVideoFile(String name, String uri) {
        this.localVideoName = name;
        this.localVideoUri = uri;
    }

    public void clearLocalVideoFile() {
        this.localVideoName = null;
        this.localVideoUri = null;
    }

    public String getLocalThumbnailName() {
        return localThumbnailName;
    }

    public String getLocalThumbnailUri() {
        return localThumbnailUri;
    }

    public void setLocalThumbnailFile(String name, String uri) {
        this.localThumbnailName = name;
        this.localThumbnailUri = uri;
    }

    public void clearLocalThumbnailFile() {
        this.localThumbnailName = null;
        this.localThumbnailUri = null;
    }

    public List<String> getLocalFileNames() {
        if (localFileNames == null) {
            localFileNames = new ArrayList<>();
        }
        return localFileNames;
    }

    public List<String> getLocalFileUris() {
        if (localFileUris == null) {
            localFileUris = new ArrayList<>();
        }
        return localFileUris;
    }

    public void addLocalFile(String name, String uri) {
        getLocalFileNames().add(name);
        getLocalFileUris().add(uri);
    }

    public void setLocalFile(int index, String name, String uri) {
        if (index < 0 || index >= getLocalFileNames().size()) return;

        getLocalFileNames().set(index, name);
        if (index < getLocalFileUris().size()) {
            getLocalFileUris().set(index, uri);
        }
    }

    public void removeLocalFile(int index) {
        if (index < 0 || index >= getLocalFileNames().size()) return;

        getLocalFileNames().remove(index);
        if (index < getLocalFileUris().size()) {
            getLocalFileUris().remove(index);
        }
    }

    public Lesson deepCopy() {
        Lesson copy = new Lesson();
        copy.setId(id);
        copy.setChapterId(chapterId);
        copy.setTitle(title);
        copy.setContentType(contentType);
        copy.setVideoUrl(videoUrl);
        copy.setDocumentUrl(documentUrl);
        copy.setOrderIndex(orderIndex);
        copy.setCreatedAt(createdAt);
        copy.setDurationSeconds(durationSeconds);
        copy.setContent(deepCopyValue(content));
        copy.setLocalVideoFile(localVideoName, localVideoUri);
        copy.setLocalThumbnailFile(localThumbnailName, localThumbnailUri);
        copy.getLocalFileNames().addAll(getLocalFileNames());
        copy.getLocalFileUris().addAll(getLocalFileUris());
        return copy;
    }

    private Object deepCopyValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> copiedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object key = entry.getKey();
                if (key != null) {
                    copiedMap.put(String.valueOf(key), deepCopyValue(entry.getValue()));
                }
            }
            return copiedMap;
        }
        if (value instanceof List) {
            List<Object> copiedList = new ArrayList<>();
            for (Object item : (List<?>) value) {
                copiedList.add(deepCopyValue(item));
            }
            return copiedList;
        }
        return value;
    }
}