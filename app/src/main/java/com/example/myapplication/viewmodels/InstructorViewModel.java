package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.utils.CurrencyFormatter;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class InstructorViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final String instructorId;

    private final MutableLiveData<String> totalStudents = new MutableLiveData<>();
    private final MutableLiveData<String> monthlyRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> avgRating = new MutableLiveData<>();
    private final MutableLiveData<String> liveCourses = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> instructorCourses = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private boolean coursesRequestInFlight;
    private boolean statsRefreshQueued;

    public InstructorViewModel(@NonNull Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        reviewRepository = new ReviewRepository(application);

        SessionManager sessionManager = new SessionManager(application);
        this.instructorId = sessionManager.getUserId();
    }

    public void refreshStats() {
        fetchInstructorCourses(true);
    }

    public void refreshCourses() {
        fetchInstructorCourses(false);
    }

    private void fetchInstructorCourses(boolean includeStats) {
        if (instructorId == null) {
            instructorCourses.setValue(new ArrayList<>());
            if (includeStats) {
                totalStudents.setValue("0");
                monthlyRevenue.setValue(CurrencyFormatter.formatVnd(0));
                avgRating.setValue("0.0 ★");
                liveCourses.setValue("0");
            }
            errorMessage.setValue("Missing instructor session");
            return;
        }

        if (coursesRequestInFlight) {
            statsRefreshQueued = statsRefreshQueued || includeStats;
            return;
        }

        coursesRequestInFlight = true;
        statsRefreshQueued = false;
        loading.setValue(true);
        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                coursesRequestInFlight = false;
                boolean shouldRefreshStats = includeStats || statsRefreshQueued;
                statsRefreshQueued = false;
                loading.setValue(false);
                List<Course> safeCourses = courses != null ? courses : new ArrayList<>();

                ChapterRepository chapterRepository = new ChapterRepository(getApplication());
                LessonRepository lessonRepository = new LessonRepository(getApplication());

                chapterRepository.getAll(new ChapterRepository.RepositoryCallback<List<Chapter>>() {
                    @Override
                    public void onSuccess(List<Chapter> allChapters) {
                        lessonRepository.getAll(new LessonRepository.RepositoryCallback<List<Lesson>>() {
                            @Override
                            public void onSuccess(List<Lesson> allLessons) {
                                Map<String, List<Chapter>> chaptersByCourse = new HashMap<>();
                                for (Chapter chapter : allChapters) {
                                    if (chapter.getCourseId() != null) {
                                        chaptersByCourse.computeIfAbsent(chapter.getCourseId(), k -> new ArrayList<>()).add(chapter);
                                    }
                                }

                                Map<String, List<Lesson>> lessonsByChapter = new HashMap<>();
                                for (Lesson lesson : allLessons) {
                                    if (lesson.getChapterId() != null) {
                                        lessonsByChapter.computeIfAbsent(lesson.getChapterId(), k -> new ArrayList<>()).add(lesson);
                                    }
                                }

                                for (Course course : safeCourses) {
                                    int count = 0;
                                    List<Chapter> courseChapters = chaptersByCourse.get(course.getId());
                                    if (courseChapters != null) {
                                        for (Chapter chapter : courseChapters) {
                                            List<Lesson> chapterLessons = lessonsByChapter.get(chapter.getId());
                                            if (chapterLessons != null) {
                                                count += chapterLessons.size();
                                            }
                                        }
                                    }
                                    course.setLessonCount(count);
                                }

                                instructorCourses.setValue(safeCourses);
                                liveCourses.setValue(String.valueOf(countLiveCourses(safeCourses)));
                                if (shouldRefreshStats) {
                                    fetchEnrollmentStats(safeCourses);
                                    fetchReviewStats(safeCourses);
                                }
                            }

                            @Override
                            public void onError(String message) {
                                instructorCourses.setValue(safeCourses);
                                liveCourses.setValue(String.valueOf(countLiveCourses(safeCourses)));
                                if (shouldRefreshStats) {
                                    fetchEnrollmentStats(safeCourses);
                                    fetchReviewStats(safeCourses);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        instructorCourses.setValue(safeCourses);
                        liveCourses.setValue(String.valueOf(countLiveCourses(safeCourses)));
                        if (shouldRefreshStats) {
                            fetchEnrollmentStats(safeCourses);
                            fetchReviewStats(safeCourses);
                        }
                    }
                });
            }
            @Override public void onError(String message) {
                coursesRequestInFlight = false;
                statsRefreshQueued = false;
                loading.setValue(false);
                instructorCourses.setValue(new ArrayList<>());
                errorMessage.setValue(message);
                if (includeStats) {
                    liveCourses.setValue("0");
                    totalStudents.setValue("0");
                    monthlyRevenue.setValue(CurrencyFormatter.formatVnd(0));
                    avgRating.setValue("0.0 ★");
                }
            }
        });
    }

    public void deleteCourse(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            errorMessage.setValue("Invalid course id");
            return;
        }
        if (instructorId == null || instructorId.trim().isEmpty()) {
            errorMessage.setValue("Missing instructor session");
            return;
        }

        loading.setValue(true);
        courseRepository.deleteForInstructor(courseId, instructorId, new CourseRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                refreshCourses();
            }

            @Override
            public void onError(String message) {
                loading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void updateCourseStatus(Course course, String status) {
        if (course == null || course.getId() == null || course.getId().isEmpty()) {
            errorMessage.setValue("Invalid course id");
            return;
        }
        if (status == null || status.trim().isEmpty()) {
            errorMessage.setValue("Invalid course status");
            return;
        }
        if (instructorId == null || instructorId.trim().isEmpty()) {
            errorMessage.setValue("Missing instructor session");
            return;
        }

        String previousStatus = course.getStatus();
        course.setStatus(status);
        loading.setValue(true);
        courseRepository.updateForInstructor(course.getId(), instructorId, course, new CourseRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                refreshCourses();
            }

            @Override
            public void onError(String message) {
                course.setStatus(previousStatus);
                loading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    private int countLiveCourses(List<Course> courses) {
        int liveCount = 0;
        if (courses != null) {
            for (Course c : courses) {
                if ("approved".equalsIgnoreCase(c.getStatus())) {
                    liveCount++;
                }
            }
        }
        return liveCount;
    }

    private void fetchEnrollmentStats(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                int studentCount = 0;
                double monthlyTotal = 0;

                if (courses != null && allEnrollments != null) {
                    for (Course course : courses) {
                        for (Enrollment enrollment : allEnrollments) {
                            if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(course.getId())) {
                                studentCount++;

                                // Fallback: If enrollment has no price, use the course price
                                double amount = enrollment.getPaidAmount();
                                if (amount <= 0) {
                                    amount = course.getPrice();
                                }
                                if (isCurrentMonthEnrollment(enrollment)) {
                                    monthlyTotal += amount;
                                }
                            }
                        }
                    }
                }

                totalStudents.setValue(String.valueOf(studentCount));
                monthlyRevenue.setValue(CurrencyFormatter.formatVnd(monthlyTotal));
            }
            @Override public void onError(String message) {
                totalStudents.setValue("0");
                monthlyRevenue.setValue(CurrencyFormatter.formatVnd(0));
            }
        });
    }

    private boolean isCurrentMonthEnrollment(Enrollment enrollment) {
        String date = enrollment.getCreatedAt() != null ? enrollment.getCreatedAt() : enrollment.getEnrolledAt();
        if (date == null || date.length() < 7) {
            return false;
        }

        try {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            Calendar now = Calendar.getInstance();
            return year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void fetchReviewStats(List<Course> courses) {
        reviewRepository.getAll(new ReviewRepository.RepositoryCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> allReviews) {
                double totalRating = 0;
                int ratingCount = 0;

                if (courses != null && allReviews != null) {
                    for (Course course : courses) {
                        for (Review review : allReviews) {
                            if (review.getCourseId() != null && review.getCourseId().equals(course.getId())) {
                                totalRating += review.getRating();
                                ratingCount++;
                            }
                        }
                    }
                }

                if (ratingCount > 0) {
                    avgRating.setValue(String.format(Locale.US, "%.1f ★", totalRating / ratingCount));
                } else {
                    avgRating.setValue("0.0 ★");
                }
            }

            @Override
            public void onError(String message) {
                avgRating.setValue("0.0 ★");
            }
        });
    }

    public LiveData<String> getTotalStudents() { return totalStudents; }
    public LiveData<String> getMonthlyRevenue() { return monthlyRevenue; }
    public LiveData<String> getAvgRating() { return avgRating; }
    public LiveData<String> getLiveCourses() { return liveCourses; }
    public LiveData<List<Course>> getInstructorCourses() { return instructorCourses; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
