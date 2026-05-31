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
    private SessionManager sessionManager;

    private String courseId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        courseId = getIntent().getStringExtra("COURSE_ID");
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        reviewRepository = new ReviewRepository(this);

        initViews();
        setupListeners();
        checkExistingReview();
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

    private void checkExistingReview() {
        if (userId == null || courseId == null) return;

        // Disable submit button while fetching to prevent duplicate submissions
        btnSubmit.setEnabled(false);
        btnSubmit.setText(R.string.loading);

        reviewRepository.getReviewByUserAndCourse(userId, courseId, new ReviewRepository.RepositoryCallback<Review>() {
            @Override
            public void onSuccess(Review review) {
                lockUIWithExistingReview(review);
            }

            @Override
            public void onError(String message) {
                // Review not found, allow user to submit
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
