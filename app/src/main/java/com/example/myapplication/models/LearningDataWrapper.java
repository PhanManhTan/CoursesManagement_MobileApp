package com.example.myapplication.models;

import java.util.List;

public class LearningDataWrapper {

    public static class LearningLesson {
        private Lesson lesson;
        private LessonProgress progress;
        private boolean isLocked;

        public LearningLesson(Lesson lesson, LessonProgress progress, boolean isLocked) {
            this.lesson = lesson;
            this.progress = progress;
            this.isLocked = isLocked;
        }

        public Lesson getLesson() { return lesson; }
        public LessonProgress getProgress() { return progress; }
        public boolean isLocked() { return isLocked; }
        public void setLocked(boolean locked) { isLocked = locked; }
    }

    public static class LearningChapter {
        private Chapter chapter;
        private List<LearningLesson> lessons;
        private boolean isExpanded;

        public LearningChapter(Chapter chapter, List<LearningLesson> lessons) {
            this.chapter = chapter;
            this.lessons = lessons;
            this.isExpanded = false;
        }

        public Chapter getChapter() { return chapter; }
        public List<LearningLesson> getLessons() { return lessons; }
        public boolean isExpanded() { return isExpanded; }
        public void setExpanded(boolean expanded) { isExpanded = expanded; }
    }
}