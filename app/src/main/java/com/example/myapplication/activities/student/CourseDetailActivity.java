package com.example.myapplication.activities.student;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ChapterAdapter;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private ImageView btnBack, ivThumbnail;
    private ShapeableImageView avtTeacher;
    private Button btnEnroll;
    private TextView tvCourseTitle, tvDiscountPrice, tvOriginalPrice, tvPerDiscount, tvCourseDuration, tvRating, tvCategory, tvDesDetail, tvTeacherName, tvTeacherRole;

    private CourseRepository courseRepository;
    private ChapterRepository chapterRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private EnrollmentRepository enrollmentRepository;

    private ChapterAdapter chapterAdapter;
    private String courseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        courseId = getIntent().getStringExtra("COURSE_ID");
        userId = getIntent().getStringExtra("USER_ID");

        if (courseId == null) {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initRepositories();
        setupRecyclerView();

        loadCourseDetails();
        loadChapters();
        checkEnrollmentStatus();

        btnBack.setOnClickListener(v -> finish());
//        btnEnroll.setOnClickListener(v -> {
//            Intent intent = new Intent(this, LearningActivity.class);
//            intent.putExtra("COURSE_ID", courseId);
//            startActivity(intent);
//        });
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        btnBack = findViewById(R.id.btnBack);
        btnEnroll = findViewById(R.id.btnEnroll);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        avtTeacher = findViewById(R.id.avtTeacher);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvDiscountPrice = findViewById(R.id.tvDiscountPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvPerDiscount = findViewById(R.id.tvPerDiscount);
        tvCourseDuration = findViewById(R.id.tvCourseDuration);
        tvRating = findViewById(R.id.tvRating);
        tvCategory = findViewById(R.id.tvCategory);
        tvDesDetail = findViewById(R.id.tvDesDetail);
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvTeacherRole = findViewById(R.id.textView6);
    }

    private void initRepositories() {
        courseRepository = new CourseRepository(this);
        chapterRepository = new ChapterRepository(this);
        userRepository = new UserRepository(this);
        categoryRepository = new CategoryRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
    }

    private void setupRecyclerView() {
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setNestedScrollingEnabled(false);
        chapterAdapter = new ChapterAdapter(this);
        rvLessons.setAdapter(chapterAdapter);
    }

    private void loadCourseDetails() {
        courseRepository.getById(courseId, new CourseRepository.RepositoryCallback<Course>() {
            @Override
            public void onSuccess(Course course) {
                if (course != null) {
                    displayCourseDetails(course);
                    loadInstructorDetails(course.getInstructorId());
                    loadCategoryDetails(course.getCategoryId());
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CourseDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCourseDetails(Course course) {
        tvCourseTitle.setText(course.getTitle());
        tvDesDetail.setText(course.getDescription() != null ? course.getDescription() : "No description available.");

        tvDiscountPrice.setText(String.format("%,.0fđ", course.getDiscountPrice()));

        if (course.getPrice() > course.getDiscountPrice()) {
            tvOriginalPrice.setText(String.format("%,.0fđ", course.getPrice()));
            tvOriginalPrice.setVisibility(View.VISIBLE);

            int discountPercent = (int) (((course.getPrice() - course.getDiscountPrice()) / course.getPrice()) * 100);
            tvPerDiscount.setText(discountPercent + "% OFF");
            tvPerDiscount.setVisibility(View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvPerDiscount.setVisibility(View.GONE);
        }

        tvCourseDuration.setText(course.getDuration() != null ? course.getDuration() : "N/A");
        tvRating.setText(String.format("%.1f", course.getRating()));

        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(course.getThumbnailUrl())
                    .placeholder(R.drawable.image_courses)
                    .into(ivThumbnail);
        } else {
            ivThumbnail.setImageResource(R.drawable.image_courses);
        }
    }

    private void loadInstructorDetails(String instructorId) {
        if (instructorId == null || instructorId.isEmpty()) {
            tvTeacherName.setText("Unknown Instructor");
            tvTeacherRole.setText("");
            return;
        }

        userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvTeacherName.setText(user.getName() != null ? user.getName() : "Unknown Instructor");
                    tvTeacherRole.setText(user.getRole() != null ? user.getRole() : "Instructor");

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(CourseDetailActivity.this)
                                .load(user.getAvatarUrl())
                                .into(avtTeacher);
                    }
                }
            }

            @Override
            public void onError(String message) {
                tvTeacherName.setText("Unknown Instructor");
                tvTeacherRole.setText("");
            }
        });
    }

    private void loadCategoryDetails(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            tvCategory.setText("Category: N/A");
            return;
        }

        categoryRepository.getById(categoryId, new CategoryRepository.RepositoryCallback<Category>() {
            @Override
            public void onSuccess(Category category) {
                if (category != null) {
                    tvCategory.setText("Category: " + category.getName());
                }
            }

            @Override
            public void onError(String message) {
                tvCategory.setText("Category: N/A");
            }
        });
    }

    private void checkEnrollmentStatus() {
        if (userId == null || courseId == null) {
            return;
        }

        enrollmentRepository.checkEnrollment(userId, courseId, new EnrollmentRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isEnrolled) {
                if (isEnrolled) {
                    btnEnroll.setText("Owned");

                    btnEnroll.setEnabled(false);

                    btnEnroll.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void loadChapters() {
        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    Toast.makeText(CourseDetailActivity.this, "This course has no chapters yet.", Toast.LENGTH_LONG).show();
                } else {
                    chapterAdapter.setChapterList(chapters);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CourseDetailActivity.this, "Failed to load chapters: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
