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
import com.example.myapplication.activities.common.CartActivity;
import com.example.myapplication.adapters.ChapterAdapter;
import com.example.myapplication.data.repository.CartRepository;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Cart;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private RecyclerView rvReviews;
    private ImageView btnBack, ivThumbnail;
    private TextView tvCartBadge;
    private View btnCartContainer;
    private ShapeableImageView avtTeacher;
    private Button btnEnroll;
    private TextView tvCourseTitle, tvDiscountPrice, tvOriginalPrice, tvPerDiscount, tvCourseDuration, tvRating, tvCategory, tvDesDetail, tvTeacherName, tvTeacherRole, tvReviewsTitle;
    private Button btnEnroll, btnAddToCart;
    private TextView tvCourseTitle, tvDiscountPrice, tvOriginalPrice, tvPerDiscount, tvCourseDuration, tvRating, tvCategory, tvDesDetail, tvTeacherName, tvTeacherRole;

    private CourseRepository courseRepository;
    private ChapterRepository chapterRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private EnrollmentRepository enrollmentRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    private ChapterAdapter chapterAdapter;
    private ReviewAdapter reviewAdapter;
    private String courseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        sessionManager = new SessionManager(this);
        courseId = getIntent().getStringExtra("COURSE_ID");
        userId = sessionManager.getUserId();

        if (courseId == null) {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initRepositories();
        setupRecyclerViews();

        loadCourseDetails();
        loadChapters();
        loadReviews();
        checkEnrollmentStatus();

        btnBack.setOnClickListener(v -> finish());
        btnCartContainer.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnAddToCart.setOnClickListener(v -> addToCart());
        
        btnEnroll.setOnClickListener(v -> {
            Toast.makeText(this, "Buy feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        rvReviews = findViewById(R.id.rvReviews);
        btnBack = findViewById(R.id.btnBack);
        btnCartContainer = findViewById(R.id.btnCartContainer);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnEnroll = findViewById(R.id.btnEnroll);
        btnAddToCart = findViewById(R.id.btnAddToCart);
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
        tvReviewsTitle = findViewById(R.id.tvReviewsTitle);
    }

    private void initRepositories() {
        courseRepository = new CourseRepository(this);
        chapterRepository = new ChapterRepository(this);
        userRepository = new UserRepository(this);
        categoryRepository = new CategoryRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        cartRepository = new CartRepository(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void updateCartBadge() {
        if (userId == null) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        cartRepository.getByUserId(userId, new CartRepository.RepositoryCallback<List<Cart>>() {
            @Override
            public void onSuccess(List<Cart> data) {
                runOnUiThread(() -> {
                    if (data != null && !data.isEmpty()) {
                        tvCartBadge.setText(String.valueOf(data.size()));
                        tvCartBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvCartBadge.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> tvCartBadge.setVisibility(View.GONE));
            }
        });
    }

    private void setupRecyclerViews() {
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setNestedScrollingEnabled(false);
        chapterAdapter = new ChapterAdapter(this);
        rvLessons.setAdapter(chapterAdapter);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setNestedScrollingEnabled(false);
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadCourseDetails() {
        courseRepository.getById(courseId, new CourseRepository.RepositoryCallback<Course>() {
            @Override
            public void onSuccess(Course course) {
                if (course != null) {
                    runOnUiThread(() -> {
                        displayCourseDetails(course);
                        loadInstructorDetails(course.getInstructorId());
                        loadCategoryDetails(course.getCategoryId());
                    });
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CourseDetailActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show());
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
            avtTeacher.setImageResource(R.drawable.noavatar);
            return;
        }

        userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    runOnUiThread(() -> {
                        tvTeacherName.setText(user.getName() != null ? user.getName() : "Unknown Instructor");
                        tvTeacherRole.setText(user.getRole() != null ? user.getRole() : "Instructor");

                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(CourseDetailActivity.this)
                                    .load(user.getAvatarUrl())
                                    .into(avtTeacher);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    tvTeacherName.setText("Unknown Instructor");
                    tvTeacherRole.setText("");
                });
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
                    runOnUiThread(() -> tvCategory.setText("Category: " + category.getName()));
                }
            }

            @Override
            public void onError(String message) {
                // Thay thế bằng giá trị mặc định khi lỗi
                runOnUiThread(() -> tvCategory.setText("Category: N/A"));
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
                runOnUiThread(() -> {
                    if (isEnrolled) {
                        btnEnroll.setText("Owned");
                        btnEnroll.setEnabled(false);
                        btnEnroll.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                        btnAddToCart.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void addToCart() {
        if (userId == null) {
            Toast.makeText(this, "Please login to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        Cart cartItem = new Cart(null, userId, courseId);
        btnAddToCart.setEnabled(false);
        
        cartRepository.addToCart(cartItem, new CartRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    btnAddToCart.setEnabled(true);
                    Toast.makeText(CourseDetailActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                    updateCartBadge(); // Cập nhật lại số lượng ngay lập tức
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnAddToCart.setEnabled(true);
                    Toast.makeText(CourseDetailActivity.this, "Failed to add to cart: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadChapters() {
        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(CourseDetailActivity.this, "This course has no chapters yet.", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> chapterAdapter.setChapterList(chapters));
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(CourseDetailActivity.this, "Failed to load chapters: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadReviews() {
        reviewRepository.getByCourseId(courseId, new ReviewRepository.RepositoryCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (reviews == null || reviews.isEmpty()) {
                    tvReviewsTitle.setText("Student Reviews (0)");
                    rvReviews.setVisibility(View.GONE);
                } else {
                    tvReviewsTitle.setText("Student Reviews (" + reviews.size() + ")");
                    rvReviews.setVisibility(View.VISIBLE);
                    reviewAdapter.setReviewList(reviews);
                }
            }

            @Override
            public void onError(String message) {
                tvReviewsTitle.setText("Student Reviews");
            }
        });
    }
}