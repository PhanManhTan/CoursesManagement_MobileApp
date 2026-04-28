package com.example.myapplication.utils;

import com.example.myapplication.models.*;
import java.util.ArrayList;
import java.util.List;

public class MockData {

    // --- Users ---
    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("admin_id", "Admin User", "admin@gmail.com", "12345678", "https://via.placeholder.com/150", "Admin", "System Administrator", "2023-01-01"));
        users.add(new User("instr_id", "Instructor User", "instructor@gmail.com", "12345678", "https://via.placeholder.com/150", "Instructor", "Professional Educator", "2023-01-01"));
        users.add(new User("student_id", "Student User", "student@gmail.com", "12345678", "https://via.placeholder.com/150", "Student", "Eager Learner", "2023-01-01"));
        users.add(new User("u1", "Manh Tan", "tan@example.com", "12345678", "https://via.placeholder.com/150", "Admin", "Senior Software Engineer and Project Lead.", "2023-01-01"));
        return users;
    }

    // --- Categories ---
    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("cat1", "Development", "development", null, "https://via.placeholder.com/100x100?text=Dev", "2023-01-01"));
        categories.add(new Category("cat2", "Business", "business", null, "https://via.placeholder.com/100x100?text=Biz", "2023-01-01"));
        categories.add(new Category("cat3", "Design", "design", null, "https://via.placeholder.com/100x100?text=Design", "2023-01-01"));
        categories.add(new Category("cat4", "Marketing", "marketing", null, "https://via.placeholder.com/100x100?text=Market", "2023-01-01"));
        return categories;
    }

    // --- Courses ---
    public static List<Course> getCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("c1", "instr_id", "Android Development Masterclass", "Learn Android development from scratch to advanced level with real-world projects.", "image_courses", 99.99, 79.99, "published", "cat1", "2023-06-01"));
        courses.add(new Course("c2", "u1", "Modern Web Architecture", "Deep dive into modern web patterns, microservices, and scalable systems.", "image_courses", 129.99, 109.99, "published", "cat1", "2023-07-01"));
        courses.add(new Course("c3", "instr_id", "UI/UX Design Fundamentals", "Master the principles of user interface and user experience design for mobile and web.", "image_courses", 89.99, 69.99, "published", "cat3", "2023-08-01"));
        courses.add(new Course("c4", "u1", "Database Optimization Secrets", "Learn how to optimize SQL queries and design efficient database schemas for high performance.", "image_courses", 110.00, 95.00, "pending", "cat1", "2023-09-01"));
        return courses;
    }

    // --- Chapters ---
    public static List<Chapter> getChapters(String courseId) {
        List<Chapter> chapters = new ArrayList<>();
        if ("c1".equals(courseId)) {
            chapters.add(new Chapter("ch1", "c1", "Introduction to Android", 1));
            chapters.add(new Chapter("ch2", "c1", "Activities and Fragments", 2));
            chapters.add(new Chapter("ch3", "c1", "RecyclerView and Adapters", 3));
        } else {
            chapters.add(new Chapter("ch_other", courseId, "General Concepts", 1));
            chapters.add(new Chapter("ch_other_2", courseId, "Advanced Techniques", 2));
        }
        return chapters;
    }

    // --- Lessons ---
    public static List<Lesson> getLessons(String chapterId) {
        List<Lesson> lessons = new ArrayList<>();
        if ("ch1".equals(chapterId)) {
            lessons.add(new Lesson("l1", "ch1", "Setting up Android Studio", "video", "https://example.com/video1", null, 1, "2023-06-01"));
            lessons.add(new Lesson("l2", "ch1", "Understanding Project Structure", "video", "https://example.com/video2", null, 2, "2023-06-01"));
        } else {
            lessons.add(new Lesson("l_other", chapterId, "Standard Lesson", "video", null, null, 1, "2023-06-01"));
            lessons.add(new Lesson("l_other_2", chapterId, "Supplementary Material", "document", null, "https://example.com/doc", 2, "2023-06-01"));
        }
        return lessons;
    }

    // --- Enrollments ---
    public static List<Enrollment> getEnrollments() {
        return new ArrayList<>(); 
    }

    // --- Helper methods ---
    public static User getUserById(String id) {
        for (User u : getUsers()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public static List<Course> getCoursesByInstructor(String instructorId) {
        List<Course> result = new ArrayList<>();
        for (Course c : getCourses()) {
            if (c.getInstructorId().equals(instructorId)) result.add(c);
        }
        return result;
    }
}
