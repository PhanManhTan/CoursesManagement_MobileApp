package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.EnrollmentTransactionAdapter;
import com.example.myapplication.adapters.InstructorCourseAdapter;
import com.example.myapplication.adapters.StudentAdapter;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.common.EditProfileActivity;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.viewmodels.InstructorViewModel;
import com.example.myapplication.viewmodels.RevenueViewModel;
import com.example.myapplication.viewmodels.StudentListViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstructorMainActivity extends AppCompatActivity
        implements InstructorCourseAdapter.OnCourseActionListener {

    private FrameLayout contentContainer;
    private BottomNavigationView bottomNav;
    private InstructorViewModel instructorViewModel;
    private RevenueViewModel revenueViewModel;
    private StudentListViewModel studentListViewModel;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> editCourseLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!isInstructorSession()) {
            redirectToLogin("Please log in with an instructor account.");
            return;
        }

        registerEditCourseLauncher();
        setContentView(R.layout.activity_instructor_main);

        contentContainer = findViewById(R.id.contentContainer);
        bottomNav = findViewById(R.id.bottomNav);
        userRepository = new UserRepository(this);
        courseRepository = new CourseRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);

        RetrofitClient.setAuthErrorListener(() -> runOnUiThread(() -> {
            Toast.makeText(this, "Request was not authorized. Please try again.", Toast.LENGTH_SHORT).show();
        }));

        setupBottomNav();
        if (bottomNav.getSelectedItemId() == R.id.nav_instructor_home) {
            showDashboard();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_instructor_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitClient.setAuthErrorListener(null);
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_instructor_home) {
                showDashboard();
                return true;
            } else if (id == R.id.nav_instructor_courses) {
                showCourses();
                return true;
            } else if (id == R.id.nav_instructor_students) {
                showStudents();
                return true;
            } else if (id == R.id.nav_instructor_revenue) {
                showRevenue();
                return true;
            } else if (id == R.id.nav_instructor_account) {
                showAccount();
                return true;
            }
            return false;
        });
    }

    private void registerEditCourseLauncher() {
        editCourseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (bottomNav != null && bottomNav.getSelectedItemId() == R.id.nav_instructor_courses) {
                        showCourses();
                    }
                }
        );
    }

    private void openEditCourse(Course course) {
        Intent intent = new Intent(this, EditCourseActivity.class);
        if (course != null) {
            intent.putExtra(EditCourseActivity.EXTRA_COURSE_ID, course.getId());
            intent.putExtra(EditCourseActivity.EXTRA_COURSE, course);
        } else {
            intent.putExtra("IS_NEW_COURSE", true);
        }

        if (editCourseLauncher != null) {
            editCourseLauncher.launch(intent);
        } else {
            startActivity(intent);
        }
    }

    private boolean isInstructorSession() {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }

        String role = sessionManager.getRole();
        return role != null && "instructor".equals(role.trim().toLowerCase());
    }

    private void redirectToLogin(String message) {
        sessionManager.clear();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private View inflateContent(int layoutResId) {
        contentContainer.removeAllViews();
        View root = getLayoutInflater().inflate(layoutResId, contentContainer, false);
        contentContainer.addView(root);
        return root;
    }

    private void showDashboard() {
        View root = inflateContent(R.layout.activity_instructor_dashboard);
        View innerBottomNav = root.findViewById(R.id.bottomNav);
        if (innerBottomNav != null) innerBottomNav.setVisibility(View.GONE);

        TextView tvTotalStudents = root.findViewById(R.id.tvTotalStudents);
        TextView tvMonthlyRevenue = root.findViewById(R.id.tvMonthlyRevenue);
        TextView tvAvgRating = root.findViewById(R.id.tvAvgRating);
        TextView tvLiveCourses = root.findViewById(R.id.tvLiveCourses);

        root.findViewById(R.id.btnViewCourseList).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_instructor_courses));
        root.findViewById(R.id.btnViewRevenue).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_instructor_revenue));
        root.findViewById(R.id.btnManageStudents).setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_instructor_students));

        instructorViewModel = new ViewModelProvider(this).get(InstructorViewModel.class);
        clearInstructorObservers();
        instructorViewModel.getTotalStudents().observe(this, value -> tvTotalStudents.setText(value));
        instructorViewModel.getMonthlyRevenue().observe(this, value -> tvMonthlyRevenue.setText(value));
        instructorViewModel.getAvgRating().observe(this, value -> tvAvgRating.setText(value));
        instructorViewModel.getLiveCourses().observe(this, value -> tvLiveCourses.setText(value));
        instructorViewModel.refreshStats();
    }

    private void showCourses() {
        View root = inflateContent(R.layout.activity_instructor_course_list);
        RecyclerView rvInstructorCourses = root.findViewById(R.id.rvInstructorCourses);
        ProgressBar progressBar = root.findViewById(R.id.progressBar);
        TextView tvEmpty = root.findViewById(R.id.tvEmpty);
        FloatingActionButton fabAddCourse = root.findViewById(R.id.fabAddCourse);

        List<Course> courseList = new ArrayList<>();
        InstructorCourseAdapter courseAdapter = new InstructorCourseAdapter(this, courseList, this);
        rvInstructorCourses.setLayoutManager(new LinearLayoutManager(this));
        rvInstructorCourses.setAdapter(courseAdapter);

        fabAddCourse.setOnClickListener(v -> openEditCourse(null));

        instructorViewModel = new ViewModelProvider(this).get(InstructorViewModel.class);
        clearInstructorObservers();
        instructorViewModel.getInstructorCourses().observe(this, courses -> {
            courseAdapter.setCourses(courses);
            boolean isEmpty = courses == null || courses.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
        instructorViewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));
        instructorViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
        instructorViewModel.refreshCourses();
    }

    private void showStudents() {
        View root = inflateContent(R.layout.activity_student_list);
        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        RecyclerView rvStudents = root.findViewById(R.id.rvStudents);
        EditText etSearch = root.findViewById(R.id.etSearch);
        StudentAdapter adapter = new StudentAdapter();
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        studentListViewModel = new ViewModelProvider(this).get(StudentListViewModel.class);
        studentListViewModel.getStudents().removeObservers(this);
        studentListViewModel.getStudents().observe(this, adapter::setStudents);
        studentListViewModel.fetchStudents();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                studentListViewModel.searchStudents(s.toString());
            }
        });
    }

    private void showRevenue() {
        View root = inflateContent(R.layout.activity_revenue);
        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        BarChart barChart = root.findViewById(R.id.barChart);
        PieChart pieChart = root.findViewById(R.id.pieChart);
        TextView tvTotalRevenue = root.findViewById(R.id.tvTotalRevenue);
        TextView tvEmptyTransactions = root.findViewById(R.id.tvEmptyTransactions);
        RecyclerView rvTransactions = root.findViewById(R.id.rvTransactions);
        EnrollmentTransactionAdapter transactionAdapter = new EnrollmentTransactionAdapter();

        setupRevenueCharts(barChart, pieChart);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setAdapter(transactionAdapter);

        revenueViewModel = new ViewModelProvider(this).get(RevenueViewModel.class);
        clearRevenueObservers();
        revenueViewModel.getBarEntries().observe(this, entries -> {
            BarDataSet dataSet = new BarDataSet(entries, "Monthly Revenue");
            dataSet.setColor(getColor(R.color.accent_muted));
            dataSet.setValueTextColor(getColor(R.color.text_primary));
            barChart.setData(new BarData(dataSet));
            barChart.invalidate();
        });
        revenueViewModel.getPieEntries().observe(this, entries -> {
            PieDataSet dataSet = new PieDataSet(entries, "Course Sales");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextColor(getColor(R.color.text_primary));
            dataSet.setValueTextSize(12f);
            pieChart.setData(new PieData(dataSet));
            pieChart.invalidate();
        });
        revenueViewModel.getTotalRevenue().observe(this, tvTotalRevenue::setText);
        revenueViewModel.getRecentTransactions().observe(this, transactions -> {
            transactionAdapter.setTransactions(transactions);
            boolean isEmpty = transactions == null || transactions.isEmpty();
            tvEmptyTransactions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        revenueViewModel.fetchRevenueData();
    }

    private void showAccount() {
        View root = inflateContent(R.layout.account_activity);
        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        TextView tvFullName = root.findViewById(R.id.tvFullName);
        TextView tvEmail = root.findViewById(R.id.tvEmail);
        View btnEditProfile = root.findViewById(R.id.btnEditProfile);
        View btnLogout = root.findViewById(R.id.btnLogout);

        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Missing instructor session", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                tvFullName.setText(user.getFullName() != null ? user.getFullName() : "Instructor");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(InstructorMainActivity.this, "Error loading account", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void navigateToCourses() {
        if (bottomNav.getSelectedItemId() == R.id.nav_instructor_courses) {
            showCourses();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_instructor_courses);
        }
    }

    @Override
    public void onCourseClick(Course course) {
        openEditCourse(course);
    }

    @Override
    public void onCourseMenuClick(Course course, View anchor) {
        new AlertDialog.Builder(this)
                .setTitle("Delete course")
                .setMessage("Do you want to delete this course?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> instructorViewModel.deleteCourse(course.getId()))
                .show();
    }

    private void setupRevenueCharts(BarChart barChart, PieChart pieChart) {
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setTextColor(getColor(R.color.text_primary));
        barChart.getAxisLeft().setTextColor(getColor(R.color.text_primary));
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(getColor(R.color.text_primary));

        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setCenterTextColor(getColor(R.color.text_primary));
        pieChart.getLegend().setTextColor(getColor(R.color.text_primary));
    }

    private void displayProfileCourses(LinearLayout container, List<Course> courses) {
        container.removeAllViews();
        if (courses == null || courses.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No courses yet");
            tvEmpty.setTextColor(getColor(R.color.text_secondary));
            tvEmpty.setPadding(0, 16, 0, 16);
            container.addView(tvEmpty);
            return;
        }

        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_instructor_course, container, false);
            TextView tvCourseName = itemView.findViewById(R.id.tvCourseName);
            TextView tvLessonCount = itemView.findViewById(R.id.tvLessonCount);
            TextView tvPrice = itemView.findViewById(R.id.tvPrice);
            TextView tvStatus = itemView.findViewById(R.id.tvStatus);
            ImageView ivCourseThumb = itemView.findViewById(R.id.ivCourseThumb);
            ImageButton btnMore = itemView.findViewById(R.id.btnMore);

            tvCourseName.setText(course.getTitle());
            tvLessonCount.setText(course.getLessonCount() + " Lessons");
            tvPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));
            tvStatus.setText(course.getStatus());
            btnMore.setVisibility(View.GONE);

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(this)
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .into(ivCourseThumb);
            } else {
                ivCourseThumb.setImageResource(R.drawable.image_courses);
            }
            container.addView(itemView);
        }
    }

    private void calculateProfileStudents(TextView tvStudentsCount, List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> enrollments) {
                int total = 0;
                if (courses != null && enrollments != null) {
                    for (Course course : courses) {
                        for (Enrollment enrollment : enrollments) {
                            if (enrollment.getCourseId() != null && enrollment.getCourseId().equals(course.getId())) {
                                total++;
                            }
                        }
                    }
                }
                tvStudentsCount.setText(String.valueOf(total));
            }

            @Override
            public void onError(String message) {
                tvStudentsCount.setText("0");
            }
        });
    }

    private void clearInstructorObservers() {
        if (instructorViewModel == null) return;

        instructorViewModel.getTotalStudents().removeObservers(this);
        instructorViewModel.getMonthlyRevenue().removeObservers(this);
        instructorViewModel.getAvgRating().removeObservers(this);
        instructorViewModel.getLiveCourses().removeObservers(this);
        instructorViewModel.getInstructorCourses().removeObservers(this);
        instructorViewModel.getLoading().removeObservers(this);
        instructorViewModel.getErrorMessage().removeObservers(this);
    }

    private void clearRevenueObservers() {
        if (revenueViewModel == null) return;

        revenueViewModel.getBarEntries().removeObservers(this);
        revenueViewModel.getPieEntries().removeObservers(this);
        revenueViewModel.getTotalRevenue().removeObservers(this);
        revenueViewModel.getRecentTransactions().removeObservers(this);
    }

}