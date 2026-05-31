package com.example.myapplication.models;

public class StudentCourseProgress {
    private final User student;
    private final Course course;
    private final Enrollment enrollment;
    private final int completedLessons;
    private final int totalLessons;
    private final int progressPercent;

    public StudentCourseProgress(User student, Course course, Enrollment enrollment) {
        this.student = student;
        this.course = course;
        this.enrollment = enrollment;
        this.completedLessons = enrollment != null ? Math.max(enrollment.getProgress(), 0) : 0;
        this.totalLessons = enrollment != null ? Math.max(enrollment.getTotalLessons(), 0) : 0;
        this.progressPercent = totalLessons > 0
                ? Math.min(100, Math.round((completedLessons * 100f) / totalLessons))
                : 0;
    }

    public User getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public boolean isCompleted() {
        return totalLessons > 0 && completedLessons >= totalLessons;
    }
}
