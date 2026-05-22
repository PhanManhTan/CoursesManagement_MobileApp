package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class ChapterWithLessons {
    private Chapter chapter;
    private List<Lesson> lessons;

    public ChapterWithLessons() {
        this(new Chapter(), new ArrayList<>());
    }

    public ChapterWithLessons(Chapter chapter) {
        this(chapter, new ArrayList<>());
    }

    public ChapterWithLessons(Chapter chapter, List<Lesson> lessons) {
        this.chapter = chapter != null ? chapter : new Chapter();
        this.lessons = lessons != null ? lessons : new ArrayList<>();
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter != null ? chapter : new Chapter();
    }

    public List<Lesson> getLessons() {
        if (lessons == null) {
            lessons = new ArrayList<>();
        }
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
    }

    public ChapterWithLessons deepCopy() {
        List<Lesson> copiedLessons = new ArrayList<>();
        for (Lesson lesson : getLessons()) {
            if (lesson != null) {
                copiedLessons.add(lesson.deepCopy());
            }
        }
        return new ChapterWithLessons(
                chapter != null ? chapter.deepCopy() : new Chapter(),
                copiedLessons
        );
    }
}
