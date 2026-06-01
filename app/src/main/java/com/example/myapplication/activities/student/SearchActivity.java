package com.example.myapplication.activities.student;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String ALL_COURSES_ID = "";

    private LinearLayout categoryContainer;
    private LinearLayout searchResultsContainer;
    private EditText etInput;
    private Button btnSubmit;
    private BottomNavigationView bottomNav;

    private CourseRepository courseRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    private String targetCategoryId;
    private String userEmail;

    private String currentCategoryId = "";
    private String lastSearchedCategoryId = "";
    private String lastSearchedQuery = "";
    private List<MaterialButton> categoryButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initRepositories();

        userEmail = getIntent().getStringExtra("email");
        targetCategoryId = getIntent().getStringExtra("category_id");

        setupSearchInput();
        loadCategories();

        btnSubmit.setOnClickListener(v -> performSearch());

        bottomNav.setSelectedItemId(R.id.nav_search);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    private void initViews() {
        categoryContainer = findViewById(R.id.lnCategory);
        searchResultsContainer = findViewById(R.id.lnSearchResults);
        etInput = findViewById(R.id.etInput);
        btnSubmit = findViewById(R.id.btnSubmit);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void initRepositories() {
        courseRepository = new CourseRepository(this);
        categoryRepository = new CategoryRepository(this);
        userRepository = new UserRepository(this);
    }

    private void setupSearchInput() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCategories() {
        categoryRepository.getAll(new CategoryRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                categoryContainer.removeAllViews();
                categoryButtons.clear();

                if (categories == null) {
                    categories = new ArrayList<>();
                }

                Category matchedCategory = null;

                // Prepend "All Courses" category button
                View allBtnView = getLayoutInflater().inflate(R.layout.item_category, categoryContainer, false);
                MaterialButton allBtn = (MaterialButton) allBtnView;
                allBtn.setText(getString(R.string.all_courses));
                allBtn.setTag(ALL_COURSES_ID);
                allBtn.setOnClickListener(v -> {
                    selectCategory(ALL_COURSES_ID);
                    performSearch();
                });
                categoryButtons.add(allBtn);
                categoryContainer.addView(allBtn);

                for (Category category : categories) {
                    View btnView = getLayoutInflater().inflate(R.layout.item_category, categoryContainer, false);
                    MaterialButton btn = (MaterialButton) btnView;
                    btn.setText(category.getName());
                    btn.setTag(category.getId());

                    btn.setOnClickListener(v -> {
                        selectCategory(category.getId());
                        performSearch();
                    });

                    categoryButtons.add(btn);
                    categoryContainer.addView(btn);

                    if (targetCategoryId != null && targetCategoryId.equals(category.getId())) {
                        matchedCategory = category;
                    }
                }

                if (matchedCategory != null) {
                    selectCategory(matchedCategory.getId());
                } else {
                    selectCategory(ALL_COURSES_ID);
                }

                performSearch();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, R.string.failed_load_categories, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectCategory(String categoryId) {
        currentCategoryId = categoryId;

        for (MaterialButton btn : categoryButtons) {
            if (btn.getTag().equals(categoryId)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_muted)));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bg_secondary)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }
        checkSubmitButtonState();
    }

    private void checkSubmitButtonState() {
        String currentQuery = etInput.getText().toString().trim();
        boolean isChanged = !currentQuery.equals(lastSearchedQuery) || !currentCategoryId.equals(lastSearchedCategoryId);

        btnSubmit.setEnabled(isChanged);
        if (isChanged) {
            btnSubmit.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent_muted)));
        } else {
            btnSubmit.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    private void performSearch() {
        lastSearchedQuery = etInput.getText().toString().trim();
        lastSearchedCategoryId = currentCategoryId;
        checkSubmitButtonState();

        if (lastSearchedQuery.isEmpty()) {
            if (currentCategoryId.equals(ALL_COURSES_ID)) {
                courseRepository.getAll(new CourseRepository.RepositoryCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> courses) {
                        displayCourses(courses);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(SearchActivity.this, R.string.failed_load_courses_plain, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                courseRepository.getByCategoryId(currentCategoryId, new CourseRepository.RepositoryCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> courses) {
                        displayCourses(courses);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(SearchActivity.this, R.string.failed_load_courses_plain, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            if (currentCategoryId.equals(ALL_COURSES_ID)) {
                courseRepository.search(lastSearchedQuery, new CourseRepository.RepositoryCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> courses) {
                        displayCourses(courses);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(SearchActivity.this, R.string.failed_search_courses, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                courseRepository.searchByCategoryAndTitle(currentCategoryId, lastSearchedQuery, new CourseRepository.RepositoryCallback<List<Course>>() {
                    @Override
                    public void onSuccess(List<Course> courses) {
                        displayCourses(courses);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(SearchActivity.this, R.string.failed_search_courses, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void displayCourses(List<Course> courses) {
        searchResultsContainer.removeAllViews();

        List<Course> visibleCourses = new java.util.ArrayList<>();
        if (courses != null) {
            for (Course course : courses) {
                if (isVisibleToStudent(course)) {
                    visibleCourses.add(course);
                }
            }
        }

        if (visibleCourses.isEmpty()) {
            android.util.Log.d("SearchActivity", "No courses found for the given search criteria.");
            TextView tvEmpty = new TextView(SearchActivity.this);
            tvEmpty.setText(R.string.no_courses_found);
            tvEmpty.setPadding(32, 32, 32, 32);
            tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            searchResultsContainer.addView(tvEmpty);
            return;
        }

        android.util.Log.d("SearchActivity", "Displaying " + visibleCourses.size() + " courses.");

        for (Course course : visibleCourses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, searchResultsContainer, false);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(course.getTitle());
            instructor.setText(R.string.loading);
            price.setText(String.format("%,.0fđ", course.getDiscountPrice()));

            userRepository.getById(course.getInstructorId(), new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        instructor.setText(user.getName());
                    }
                }

                @Override
                public void onError(String message) {
                    android.util.Log.e("SearchActivity", "Failed to load instructor for course ID: " + course.getId());
                    instructor.setText(R.string.unknown_instructor);
                }
            });

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(SearchActivity.this)
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .error(R.drawable.image_courses)
                        .into(thumb);
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

    private boolean isVisibleToStudent(Course course) {
        if (course.getStatus() == null || course.getStatus().isEmpty()) {
            return true; // default to true if status is not set
        }
        String status = course.getStatus().trim().toLowerCase(java.util.Locale.US);
        // Do not show pending, review, rejected, draft or private/hidden courses
        if (status.contains("pending") || status.contains("review") || status.contains("reject")
                || status.contains("draft") || status.contains("private") || status.contains("hidden")
                || status.contains("inactive")) {
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        overridePendingTransition(0, 0);
        if (etInput != null) {
            etInput.setText("");
        }
        selectCategory(ALL_COURSES_ID);
        performSearch();
    }
}
