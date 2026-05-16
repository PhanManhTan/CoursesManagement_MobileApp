package com.example.myapplication.activities.student;

import android.content.Intent;
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
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Course;

import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private ImageView btnBack, ivThumbnail;
    private Button btnEnroll;
    private TextView tvCourseTitle, tvDiscountPrice, tvOriginalPrice, tvPerDiscount, tvCourseDuration, tvRating, tvCategory, tvDesDetail, tvTeacherName;
    
    private CourseRepository courseRepository;
    private ChapterRepository chapterRepository;
    private ChapterAdapter chapterAdapter;
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Get Course ID from Intent
        courseId = getIntent().getStringExtra("COURSE_ID");
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

        btnBack.setOnClickListener(v -> finish());
        btnEnroll.setOnClickListener(v -> {
            Intent intent = new Intent(this, LearningActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        btnBack = findViewById(R.id.btnBack);
        btnEnroll = findViewById(R.id.btnEnroll);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvDiscountPrice = findViewById(R.id.tvDiscountPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvPerDiscount = findViewById(R.id.tvPerDiscount);
        tvCourseDuration = findViewById(R.id.tvCourseDuration);
        tvRating = findViewById(R.id.tvRating);
        tvCategory = findViewById(R.id.tvCategory);
        tvDesDetail = findViewById(R.id.tvDesDetail);
        tvTeacherName = findViewById(R.id.tvTeacherName);
    }

    private void initRepositories() {
        courseRepository = new CourseRepository(this);
        chapterRepository = new ChapterRepository(this);
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
        tvDesDetail.setText(course.getDescription());
        tvDiscountPrice.setText(String.format("%,.0fđ", course.getDiscountPrice()));
        tvOriginalPrice.setText(String.format("%,.0fđ", course.getPrice()));
        
        if (course.getPrice() > 0) {
            int discountPercent = (int) ((course.getPrice() - course.getDiscountPrice()) / course.getPrice() * 100);
            tvPerDiscount.setText(discountPercent + "% OFF");
        }

        tvCourseDuration.setText(course.getDuration() != null ? course.getDuration() : "N/A");
        tvRating.setText(String.format("%.1f", course.getRating()));
        
        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(course.getThumbnailUrl()).into(ivThumbnail);
        }

        tvTeacherName.setText("Instructor ID: " + course.getInstructorId());
    }

    private void loadChapters() {
        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    // If no chapters, show a simple toast or text (could add a TextView for "No Content")
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
