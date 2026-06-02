package com.example.myapplication.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.models.Review;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

public class ReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RatingBar ratingCourse;
    private EditText etReview;
    private Button btnSubmit;

    private ReviewRepository reviewRepository;
    private com.example.myapplication.data.repository.EnrollmentRepository enrollmentRepository;
    private SessionManager sessionManager;

    private String courseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_review);

        courseId = getIntent().getStringExtra("COURSE_ID");
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        reviewRepository = new ReviewRepository(this);
        enrollmentRepository = new com.example.myapplication.data.repository.EnrollmentRepository(this);

        initViews();
        setupListeners();
        checkEnrollmentAndExistingReview();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ratingCourse = findViewById(R.id.ratingCourse);
        etReview = findViewById(R.id.etReview);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void checkEnrollmentAndExistingReview() {
        if (userId == null || courseId == null) {
            Toast.makeText(this, "Missing user or course details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText(R.string.loading);

        enrollmentRepository.checkEnrollment(userId, courseId, new com.example.myapplication.data.repository.EnrollmentRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isEnrolled) {
                if (isEnrolled == null || !isEnrolled) {
                    runOnUiThread(() -> {
                        Toast.makeText(ReviewActivity.this, "You must enroll in this course to leave a review.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }
                checkExistingReview();
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ReviewActivity.this, "Failed to verify enrollment: " + message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void checkExistingReview() {
        if (userId == null || courseId == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText(R.string.loading);

        reviewRepository.getReviewByUserAndCourse(userId, courseId, new ReviewRepository.RepositoryCallback<Review>() {
            @Override
            public void onSuccess(Review review) {
                lockUIWithExistingReview(review);
            }

            @Override
            public void onError(String message) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText(R.string.submit_review_upper);
            }
        });
    }


    private void lockUIWithExistingReview(Review review) {
        ratingCourse.setRating(review.getRating());
        ratingCourse.setIsIndicator(true);

        etReview.setText(review.getComment());
        etReview.setEnabled(false);
        etReview.setFocusable(false);

        btnSubmit.setText(R.string.already_reviewed_upper);
        btnSubmit.setEnabled(false);
    }

    private void submitReview() {
        float rating = ratingCourse.getRating();
        String comment = etReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, R.string.provide_rating, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText(R.string.submitting);

        Review newReview = new Review();
        newReview.setUserId(userId);
        newReview.setCourseId(courseId);
        newReview.setRating((int) rating);
        newReview.setComment(comment);

        reviewRepository.insert(newReview, new ReviewRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(ReviewActivity.this, R.string.review_submitted, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReviewActivity.this, getString(R.string.failed_submit_review, message), Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText(R.string.submit_review_upper);
            }
        });
    }
}
