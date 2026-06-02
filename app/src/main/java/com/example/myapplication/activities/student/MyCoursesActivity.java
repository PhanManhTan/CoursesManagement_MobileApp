package com.example.myapplication.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.activities.student.LearningActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.MyCourseAdapter;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesActivity extends AppCompatActivity {

    private RecyclerView rvMyCourses;
    private Button btnFilterAll, btnFilterOnGoing, btnFilterCompleted;
    private BottomNavigationView bottomNav;
    private MyCourseAdapter myCourseAdapter;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager sessionManager;
    private List<Enrollment> allEnrollments = new ArrayList<>();
    private TextView tvEmptyMyCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_my_courses);

        sessionManager = new SessionManager(this);
        enrollmentRepository = new EnrollmentRepository(this);

        initViews();
        setupRecyclerView();
        setupFilters();

        loadMyCourses();

        bottomNav.setSelectedItemId(R.id.nav_courses);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });
    }

    private void initViews() {
        rvMyCourses = findViewById(R.id.rvMyCourses);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterOnGoing = findViewById(R.id.btnFilterOnGoing);
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        bottomNav = findViewById(R.id.bottomNav);
        tvEmptyMyCourses = findViewById(R.id.tvEmptyMyCourses);
    }

    private void setupRecyclerView() {
        rvMyCourses.setLayoutManager(new LinearLayoutManager(this));
        myCourseAdapter = new MyCourseAdapter();
        myCourseAdapter.setOnItemClickListener(enrollment -> {
            Intent intent = new Intent(this, LearningActivity.class);
            intent.putExtra("COURSE_ID", enrollment.getCourseId());
            if (enrollment.getCourse() != null) {
                intent.putExtra("COURSE_TITLE", enrollment.getCourse().getTitle());
            }
            startActivity(intent);
        });

        myCourseAdapter.setOnReviewClickListener(enrollment -> {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra("COURSE_ID", enrollment.getCourseId());
            startActivity(intent);
        });

        rvMyCourses.setAdapter(myCourseAdapter);
        rvMyCourses.setHasFixedSize(true);
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> {
            updateFilterButtons(btnFilterAll);
            myCourseAdapter.setEnrollmentList(allEnrollments);
            updateEmptyState(allEnrollments);
        });

        btnFilterOnGoing.setOnClickListener(v -> {
            updateFilterButtons(btnFilterOnGoing);
            filterEnrollments("ongoing");
        });

        btnFilterCompleted.setOnClickListener(v -> {
            updateFilterButtons(btnFilterCompleted);
            filterEnrollments("completed");
        });
    }

    private void updateFilterButtons(Button activeBtn) {
        btnFilterAll.setBackgroundTintList(getColorStateList(R.color.bg_secondary));
        btnFilterOnGoing.setBackgroundTintList(getColorStateList(R.color.bg_secondary));
        btnFilterCompleted.setBackgroundTintList(getColorStateList(R.color.bg_secondary));

        activeBtn.setBackgroundTintList(getColorStateList(R.color.accent));
    }

    private void loadMyCourses() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, R.string.please_login_first, Toast.LENGTH_SHORT).show();
            return;
        }

        enrollmentRepository.getByUserId(userId, new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> enrollments) {
                allEnrollments = enrollments != null ? enrollments : new ArrayList<>();
                myCourseAdapter.setEnrollmentList(allEnrollments);
                updateEmptyState(allEnrollments);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyCoursesActivity.this, getString(R.string.error_loading_courses, message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEnrollments(String status) {
        List<Enrollment> filteredList = new ArrayList<>();
        for (Enrollment e : allEnrollments) {
            boolean isCourseCompleted = e.getTotalLessons() > 0 && e.getProgress() == e.getTotalLessons();
            if (status.equals("completed") && isCourseCompleted) {
                filteredList.add(e);
            } else if (status.equals("ongoing") && !isCourseCompleted) {
                filteredList.add(e);
            }
        }
        myCourseAdapter.setEnrollmentList(filteredList);
        updateEmptyState(filteredList);
    }

    private void updateEmptyState(List<Enrollment> list) {
        if (list == null || list.isEmpty()) {
            tvEmptyMyCourses.setVisibility(View.VISIBLE);
            rvMyCourses.setVisibility(View.GONE);
        } else {
            tvEmptyMyCourses.setVisibility(View.GONE);
            rvMyCourses.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        loadMyCourses();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(0, 0);
    }
}
