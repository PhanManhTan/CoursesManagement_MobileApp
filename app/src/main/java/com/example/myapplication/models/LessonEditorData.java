package com.example.myapplication.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonEditorData implements Serializable {
    private String id;
    private String courseId;
    private String chapterId;
    private String title;
    private String description;
    private String videoUrl;
    private String documentUrl;
    private String contentType;
    private int orderIndex;
    private String localVideoName;
    private String localVideoUri;
    private ArrayList<String> localFileNames = new ArrayList<>();
    private ArrayList<String> localFileUris = new ArrayList<>();
    private ArrayList<String> localFileSizes = new ArrayList<>();
    private ArrayList<Quiz> quizzes = new ArrayList<>();

    public static LessonEditorData fromLesson(Lesson lesson) {
        LessonEditorData data = new LessonEditorData();
        if (lesson == null) {
            return data;
        }

        data.id = lesson.getId();
        data.chapterId = lesson.getChapterId();
        data.title = lesson.getTitle();
        data.videoUrl = lesson.getVideoUrl();
        data.documentUrl = lesson.getDocumentUrl();
        data.contentType = lesson.getContentType();
        data.orderIndex = lesson.getOrderIndex();

        Map<String, Object> content = getContentMap(lesson.getContent());
        data.description = getMapString(content, "description");

        String videoName = lesson.getLocalVideoName();
        String videoUri = lesson.getLocalVideoUri();
        if (!hasValue(videoUri)) {
            videoUri = getMapString(content, "video_url");
        }
        if (!hasValue(videoName)) {
            videoName = getMapString(content, "video_name");
        }
        if (!hasValue(videoUri)) {
            videoUri = lesson.getVideoUrl();
        }
        if (hasValue(videoUri) || hasValue(videoName)) {
            data.localVideoUri = videoUri;
            data.localVideoName = hasValue(videoName) ? videoName : extractFileName(videoUri);
            if (!hasValue(data.videoUrl)) {
                data.videoUrl = videoUri;
            }
        }

        copyFilesFromLesson(data, lesson);
        if (data.localFileUris.isEmpty()) {
            copyFilesFromContent(data, content);
        }
        if (data.localFileUris.isEmpty() && hasValue(lesson.getDocumentUrl())) {
            String[] uris = lesson.getDocumentUrl().split("\\n");
            for (String uri : uris) {
                if (hasValue(uri)) {
                    data.addFile(extractFileName(uri), uri, "");
                }
            }
        }
        copyQuizzesFromContent(data, content);

        return data;
    }

    public void applyToLesson(Lesson lesson) {
        if (lesson == null) {
            return;
        }

        lesson.setId(id);
        lesson.setChapterId(chapterId);
        lesson.setTitle(title);
        lesson.setVideoUrl(videoUrl);
        lesson.setDocumentUrl(documentUrl);
        lesson.setContentType(resolveContentType());
        lesson.setOrderIndex(orderIndex);

        lesson.clearLocalVideoFile();
        if (hasValue(localVideoUri) || hasValue(videoUrl)) {
            String uri = hasValue(localVideoUri) ? localVideoUri : videoUrl;
            lesson.setLocalVideoFile(hasValue(localVideoName) ? localVideoName : extractFileName(uri), uri);
        }

        lesson.getLocalFileNames().clear();
        lesson.getLocalFileUris().clear();
        for (int i = 0; i < localFileUris.size(); i++) {
            String uri = localFileUris.get(i);
            if (!hasValue(uri)) continue;

            String name = i < localFileNames.size() ? localFileNames.get(i) : extractFileName(uri);
            lesson.addLocalFile(hasValue(name) ? name : extractFileName(uri), uri);
        }

        lesson.setContent(buildContent());
    }

    public Map<String, Object> buildContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("managed_by_instructor_editor", true);
        content.put("description", valueOrEmpty(description));
        content.put("video_name", valueOrEmpty(localVideoName));
        content.put("video_url", valueOrEmpty(hasValue(localVideoUri) ? localVideoUri : videoUrl));

        List<Map<String, String>> files = new ArrayList<>();
        for (int i = 0; i < localFileUris.size(); i++) {
            String uri = localFileUris.get(i);
            if (!hasValue(uri)) continue;

            Map<String, String> file = new HashMap<>();
            file.put("name", i < localFileNames.size() ? valueOrEmpty(localFileNames.get(i)) : extractFileName(uri));
            file.put("url", uri);
            file.put("size", i < localFileSizes.size() ? valueOrEmpty(localFileSizes.get(i)) : "");
            files.add(file);
        }
        content.put("files", files);
        content.put("quizzes", buildQuizPayload());
        return content;
    }

    private List<Map<String, Object>> buildQuizPayload() {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (Quiz quiz : getQuizzes()) {
            if (quiz == null || !hasValue(quiz.getQuestion())) {
                continue;
            }

            String resolvedLessonId = hasValue(id) ? id : quiz.getLessonId();
            quiz.setLessonId(resolvedLessonId);

            Map<String, Object> item = new HashMap<>();
            if (hasValue(quiz.getId())) {
                item.put("id", quiz.getId());
            }
            if (hasValue(resolvedLessonId)) {
                item.put("lesson_id", resolvedLessonId);
            }
            item.put("question", valueOrEmpty(quiz.getQuestion()));
            item.put("options", normalizeOptions(quiz.getOptions()));
            item.put("correct_answer", valueOrEmpty(quiz.getCorrectAnswer()));
            payload.add(item);
        }
        return payload;
    }

    public void addFile(String name, String uri, String size) {
        localFileNames.add(valueOrEmpty(name));
        localFileUris.add(valueOrEmpty(uri));
        localFileSizes.add(valueOrEmpty(size));
    }

    public void setFile(int index, String name, String uri, String size) {
        if (index < 0 || index >= localFileUris.size()) return;

        localFileUris.set(index, valueOrEmpty(uri));
        if (index < localFileNames.size()) {
            localFileNames.set(index, valueOrEmpty(name));
        }
        if (index < localFileSizes.size()) {
            localFileSizes.set(index, valueOrEmpty(size));
        }
    }

    public void removeFile(int index) {
        if (index < 0 || index >= localFileUris.size()) return;

        localFileUris.remove(index);
        if (index < localFileNames.size()) {
            localFileNames.remove(index);
        }
        if (index < localFileSizes.size()) {
            localFileSizes.remove(index);
        }
    }

    private String resolveContentType() {
        if (hasValue(localVideoUri) || hasValue(videoUrl)) {
            return "video";
        }
        if (!localFileUris.isEmpty() || hasValue(documentUrl)) {
            return "document";
        }
        return "document".equalsIgnoreCase(contentType) ? "document" : "video";
    }

    private static void copyFilesFromLesson(LessonEditorData data, Lesson lesson) {
        List<String> names = lesson.getLocalFileNames();
        List<String> uris = lesson.getLocalFileUris();
        for (int i = 0; i < uris.size(); i++) {
            String uri = uris.get(i);
            String name = i < names.size() ? names.get(i) : extractFileName(uri);
            data.addFile(name, uri, "");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getContentMap(Object content) {
        if (content instanceof Map) {
            return (Map<String, Object>) content;
        }
        if (content instanceof String && hasValue((String) content)) {
            return parseContentString((String) content);
        }
        return null;
    }

    private static Map<String, Object> parseContentString(String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject root = new JSONObject(content);
            result.put("description", root.optString("description", ""));
            result.put("video_name", root.optString("video_name", ""));
            result.put("video_url", root.optString("video_url", root.optString("video_uri", "")));

            JSONArray jsonFiles = root.optJSONArray("files");
            List<Map<String, String>> files = new ArrayList<>();
            if (jsonFiles != null) {
                for (int i = 0; i < jsonFiles.length(); i++) {
                    JSONObject jsonFile = jsonFiles.optJSONObject(i);
                    if (jsonFile == null) continue;

                    Map<String, String> file = new HashMap<>();
                    file.put("name", jsonFile.optString("name", ""));
                    file.put("url", jsonFile.optString("url", jsonFile.optString("uri", "")));
                    file.put("size", jsonFile.optString("size", ""));
                    files.add(file);
                }
            }
            result.put("files", files);

            JSONArray jsonQuizzes = root.optJSONArray("quizzes");
            List<Map<String, Object>> quizzes = new ArrayList<>();
            if (jsonQuizzes != null) {
                for (int i = 0; i < jsonQuizzes.length(); i++) {
                    JSONObject jsonQuiz = jsonQuizzes.optJSONObject(i);
                    if (jsonQuiz == null) continue;

                    Map<String, Object> quiz = new HashMap<>();
                    quiz.put("id", jsonQuiz.optString("id", ""));
                    quiz.put("lesson_id", jsonQuiz.optString("lesson_id", ""));
                    quiz.put("question", jsonQuiz.optString("question", ""));
                    quiz.put("correct_answer", jsonQuiz.optString("correct_answer", ""));

                    JSONArray jsonOptions = jsonQuiz.optJSONArray("options");
                    ArrayList<String> options = new ArrayList<>();
                    if (jsonOptions != null) {
                        for (int optionIndex = 0; optionIndex < jsonOptions.length(); optionIndex++) {
                            options.add(jsonOptions.optString(optionIndex, ""));
                        }
                    }
                    quiz.put("options", options);
                    quizzes.add(quiz);
                }
            }
            result.put("quizzes", quizzes);
        } catch (JSONException ignored) {
            return null;
        }
        return result;
    }

    private static void copyFilesFromContent(LessonEditorData data, Map<String, Object> content) {
        Object filesValue = content != null ? content.get("files") : null;
        if (!(filesValue instanceof List)) {
            return;
        }

        List<?> files = (List<?>) filesValue;
        for (Object fileValue : files) {
            if (!(fileValue instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> file = (Map<String, Object>) fileValue;
            String url = getMapString(file, "url");
            if (!hasValue(url)) {
                url = getMapString(file, "uri");
            }
            String name = getMapString(file, "name");
            String size = getMapString(file, "size");
            if (hasValue(url) || hasValue(name)) {
                data.addFile(hasValue(name) ? name : extractFileName(url), url, size);
            }
        }
    }

    private static void copyQuizzesFromContent(LessonEditorData data, Map<String, Object> content) {
        Object quizzesValue = content != null ? content.get("quizzes") : null;
        if (!(quizzesValue instanceof List)) {
            return;
        }

        List<?> quizItems = (List<?>) quizzesValue;
        for (Object quizValue : quizItems) {
            if (!(quizValue instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> quizMap = (Map<String, Object>) quizValue;
            Quiz quiz = new Quiz();
            quiz.setId(getMapString(quizMap, "id"));
            quiz.setLessonId(getMapString(quizMap, "lesson_id"));
            quiz.setQuestion(getMapString(quizMap, "question"));
            quiz.setOptions(normalizeOptions(quizMap.get("options")));
            quiz.setCorrectAnswer(getMapString(quizMap, "correct_answer"));

            if (hasValue(quiz.getQuestion()) || !normalizeOptions(quiz.getOptions()).isEmpty()) {
                data.getQuizzes().add(quiz);
            }
        }
    }

    private static ArrayList<String> normalizeOptions(Object optionsValue) {
        ArrayList<String> options = new ArrayList<>();
        if (optionsValue instanceof List) {
            for (Object value : (List<?>) optionsValue) {
                options.add(value != null ? String.valueOf(value) : "");
            }
        } else if (optionsValue instanceof String && hasValue((String) optionsValue)) {
            options.add((String) optionsValue);
        }
        return options;
    }

    private static String getMapString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "";
        }
        return String.valueOf(map.get(key));
    }

    private static String extractFileName(String value) {
        if (!hasValue(value)) return "Selected file";

        int slashIndex = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < value.length() - 1) {
            return value.substring(slashIndex + 1);
        }
        return value;
    }

    private static String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getLocalVideoName() {
        return localVideoName;
    }

    public void setLocalVideoName(String localVideoName) {
        this.localVideoName = localVideoName;
    }

    public String getLocalVideoUri() {
        return localVideoUri;
    }

    public void setLocalVideoUri(String localVideoUri) {
        this.localVideoUri = localVideoUri;
    }

    public ArrayList<String> getLocalFileNames() {
        if (localFileNames == null) {
            localFileNames = new ArrayList<>();
        }
        return localFileNames;
    }

    public ArrayList<String> getLocalFileUris() {
        if (localFileUris == null) {
            localFileUris = new ArrayList<>();
        }
        return localFileUris;
    }

    public ArrayList<String> getLocalFileSizes() {
        if (localFileSizes == null) {
            localFileSizes = new ArrayList<>();
        }
        return localFileSizes;
    }

    public ArrayList<Quiz> getQuizzes() {
        if (quizzes == null) {
            quizzes = new ArrayList<>();
        }
        return quizzes;
    }

    public void setQuizzes(List<Quiz> quizList) {
        getQuizzes().clear();
        if (quizList != null) {
            getQuizzes().addAll(quizList);
        }
    }
}
