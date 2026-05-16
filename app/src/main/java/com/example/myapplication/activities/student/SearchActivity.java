package com.example.myapplication.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private LinearLayout categoryContainer;
    private LinearLayout searchResultsContainer;
    private EditText etInput;
    private Button btnSubmit;
    private ImageView btnBack;

    private CourseRepository courseRepository;
    private CategoryRepository categoryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initRepositories();

        loadCategories();
        loadAllCourses();

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String query = etInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchCourses(query);
            } else {
                loadAllCourses();
            }
        });
    }

    private void initViews() {
        categoryContainer = findViewById(R.id.lnCategory);
        searchResultsContainer = findViewById(R.id.lnSearchResults);
        etInput = findViewById(R.id.etInput);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initRepositories() {
        courseRepository = new CourseRepository(this);
        categoryRepository = new CategoryRepository(this);
    }

    private void loadCategories() {
        categoryRepository.getAll(new CategoryRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                categoryContainer.removeAllViews();
                if (categories != null) {
                    for (Category category : categories) {
                        View btnView = getLayoutInflater().inflate(R.layout.item_category, categoryContainer, false);
                        com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) btnView;
                        btn.setText(category.getName());
                        btn.setOnClickListener(v -> loadCoursesByCategory(category.getId()));
                        categoryContainer.addView(btn);
                    }
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllCourses() {
        courseRepository.getAll(new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                displayCourses(courses);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchCourses(String query) {
        courseRepository.search(query, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                displayCourses(courses);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCoursesByCategory(String categoryId) {
        courseRepository.getByCategoryId(categoryId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                displayCourses(courses);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, "Failed to load courses for category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCourses(List<Course> courses) {
        searchResultsContainer.removeAllViews();
        
        if (courses == null || courses.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No courses found.");
            tvEmpty.setPadding(32, 32, 32, 32);
            tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            searchResultsContainer.addView(tvEmpty);
            return;
        }

        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, searchResultsContainer, false);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(course.getTitle());
            instructor.setText("Instructor ID: " + course.getInstructorId());
            price.setText(String.format("%,.0fđ", course.getDiscountPrice()));

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(this).load(course.getThumbnailUrl()).placeholder(R.drawable.image_courses).into(thumb);
            } else {
                thumb.setImageResource(R.drawable.image_courses);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, CourseDetailActivity.class);
                intent.putExtra("COURSE_ID", course.getId());
                startActivity(intent);
            });

            searchResultsContainer.addView(itemView);
        }
    }
}
