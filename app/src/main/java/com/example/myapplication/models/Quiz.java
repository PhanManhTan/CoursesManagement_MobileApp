package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Quiz implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("lesson_id")
    private String lessonId;

    @SerializedName("question")
    private String question;

    @SerializedName("options")
    private Object options;

    @SerializedName("correct_answer")
    private String correctAnswer;

    public Quiz() {
    }

    public Quiz(String id, String lessonId, String question, Object options, String correctAnswer) {
        this.id = id;
        this.lessonId = lessonId;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
