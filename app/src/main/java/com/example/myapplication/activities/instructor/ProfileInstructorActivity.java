package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.InstructorCourseAdapter;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.utils.CurrencyFormatter;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class ProfileInstructorActivity extends AppCompatActivity {

    private LinearLayout lnInstructorCourses;
    private ImageView btnBack;
    private TextView tvName, tvBio, tvExpertise, tvStudentsCount, tvRatingValue;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private ReviewRepository reviewRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_instructor);

        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        tvExpertise = findViewById(R.id.tvExpertise);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        lnInstructorCourses = findViewById(R.id.lnInstructorCourses);
        btnBack = findViewById(R.id.btnBack);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        userRepository = new UserRepository(this);
        courseRepository = new CourseRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        reviewRepository = new ReviewRepository(this);

        btnBack.setOnClickListener(v -> finish());

        loadInstructorProfile();
    }

    private void loadInstructorProfile() {
        String instructorId = sessionManager.getUserId();
        if (instructorId == null) {
            redirectToLogin();
            return;
        }

        // Fetch User Info
        userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvName.setText(user.getFullName());
                    tvBio.setText(user.getBio() != null ? user.getBio() : getString(R.string.no_bio_available));
                    tvExpertise.setText(user.getRole());
                }
            }
            @Override public void onError(String message) {}
        });

        // Fetch Courses
        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                if (courses != null) {
                    List<Course> safeCourses = courses;
                    ChapterRepository chapterRepository = new ChapterRepository(ProfileInstructorActivity.this);
                    LessonRepository lessonRepository = new LessonRepository(ProfileInstructorActivity.this);

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

                                    displayCourses(safeCourses);
                                    calculateTotalStudents(safeCourses);
                                    calculateAverageRating(safeCourses);
                                }

                                @Override
                                public void onError(String message) {
                                    displayCourses(safeCourses);
                                    calculateTotalStudents(safeCourses);
                                    calculateAverageRating(safeCourses);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            displayCourses(safeCourses);
                            calculateTotalStudents(safeCourses);
                            calculateAverageRating(safeCourses);
                        }
                    });
                }
            }
            @Override public void onError(String message) {
                Toast.makeText(ProfileInstructorActivity.this, R.string.failed_load_courses_plain, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAverageRating(List<Course> courses) {
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
                double average = ratingCount > 0 ? totalRating / ratingCount : 0;
                tvRatingValue.setText(String.format(Locale.US, "%.1f ★", average));
            }

            @Override
            public void onError(String message) {
                tvRatingValue.setText("0.0 ★");
            }
        });
    }

    private void calculateTotalStudents(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                int total = 0;
                if (allEnrollments != null && courses != null) {
                    for (Course course : courses) {
                        for (Enrollment e : allEnrollments) {
                            if (e.getCourseId() != null && e.getCourseId().equals(course.getId())) {
                                total++;
                            }
                        }
                    }
                }
                tvStudentsCount.setText(String.valueOf(total));
            }
            @Override public void onError(String message) {
                tvStudentsCount.setText("0");
            }
        });
    }

    private void displayCourses(List<Course> courses) {
        lnInstructorCourses.removeAllViews();
        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_instructor_course, lnInstructorCourses, false);

            TextView title = itemView.findViewById(R.id.tvCourseName);
            TextView lessons = itemView.findViewById(R.id.tvLessonCount);
            TextView price = itemView.findViewById(R.id.tvPrice);
            TextView status = itemView.findViewById(R.id.tvStatus);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);
            ImageButton btnMore = itemView.findViewById(R.id.btnMore);

            title.setText(course.getTitle());
            lessons.setText(getString(R.string.lesson_count_format, course.getLessonCount()));
            price.setText(CurrencyFormatter.formatVnd(course.getPrice()));
            InstructorCourseAdapter.bindStatus(this, status, course.getStatus());
            btnMore.setVisibility(View.GONE);

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(this)
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .error(R.drawable.image_courses)
                        .into(thumb);
            } else {
                thumb.setImageResource(R.drawable.image_courses);
            }

            lnInstructorCourses.addView(itemView);
        }
    }

    private void redirectToLogin() {
        sessionManager.clear();
        Toast.makeText(this, R.string.session_expired_login_again, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
