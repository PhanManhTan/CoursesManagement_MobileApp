package com.example.myapplication.utils;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.NotificationRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.Notification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseContentNotifier {
    private static final String TAG = "CourseContentNotifier";

    private final Context context;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;

    public CourseContentNotifier(Context context) {
        this.context = context.getApplicationContext();
        this.enrollmentRepository = new EnrollmentRepository(this.context);
        this.notificationRepository = new NotificationRepository(this.context);
    }

    public void notifyChapterAdded(Course course, Chapter chapter) {
        String courseId = course != null ? course.getId() : null;
        String courseTitle = course != null ? course.getTitle() : null;
        String chapterTitle = chapter != null ? chapter.getTitle() : null;
        notifyChapterAdded(courseId, courseTitle, chapterTitle);
    }

    public void notifyChapterAdded(String courseId, String courseTitle, String chapterTitle) {
        String resolvedChapterTitle = resolveText(chapterTitle, context.getString(R.string.untitled_chapter));
        String resolvedCourseTitle = resolveText(courseTitle, context.getString(R.string.untitled_course));
        notifyEnrolledStudents(
                courseId,
                context.getString(R.string.notification_new_chapter_title),
                context.getString(R.string.notification_new_chapter_body, resolvedChapterTitle, resolvedCourseTitle)
        );
    }

    public void notifyLessonAdded(Course course, Lesson lesson) {
        String courseId = course != null ? course.getId() : null;
        String courseTitle = course != null ? course.getTitle() : null;
        String lessonTitle = lesson != null ? lesson.getTitle() : null;
        notifyLessonAdded(courseId, courseTitle, lessonTitle);
    }

    public void notifyLessonAdded(String courseId, String courseTitle, String lessonTitle) {
        String resolvedLessonTitle = resolveText(lessonTitle, context.getString(R.string.untitled_lesson));
        String resolvedCourseTitle = resolveText(courseTitle, context.getString(R.string.untitled_course));
        notifyEnrolledStudents(
                courseId,
                context.getString(R.string.notification_new_lesson_title),
                context.getString(R.string.notification_new_lesson_body, resolvedLessonTitle, resolvedCourseTitle)
        );
    }

    private void notifyEnrolledStudents(String courseId, String title, String message) {
        if (!isRealCourseId(courseId)) {
            return;
        }

        enrollmentRepository.getByCourseId(courseId, new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> enrollments) {
                if (enrollments == null || enrollments.isEmpty()) {
                    return;
                }

                Set<String> userIds = new HashSet<>();
                for (Enrollment enrollment : enrollments) {
                    if (enrollment != null && hasValue(enrollment.getUserId())) {
                        userIds.add(enrollment.getUserId());
                    }
                }

                for (String userId : userIds) {
                    Notification notification = new Notification(null, userId, title, message, false, null);
                    notificationRepository.insert(notification, new NotificationRepository.RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.w(TAG, "Could not notify student " + userId + ": " + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "Could not load enrollments for course " + courseId + ": " + errorMessage);
            }
        });
    }

    private String resolveText(String value, String fallback) {
        return hasValue(value) ? value.trim() : fallback;
    }

    private boolean isRealCourseId(String courseId) {
        return hasValue(courseId)
                && !"draft".equalsIgnoreCase(courseId.trim())
                && !"tmp".equalsIgnoreCase(courseId.trim());
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
