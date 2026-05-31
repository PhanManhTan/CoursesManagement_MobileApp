package com.example.myapplication.models;

public class InstructorReviewItem {
    private final Review review;
    private final Course course;

    public InstructorReviewItem(Review review, Course course) {
        this.review = review;
        this.course = course;
    }

    public Review getReview() {
        return review;
    }

    public Course getCourse() {
        return course;
    }
}
