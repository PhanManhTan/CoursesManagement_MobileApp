package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.example.myapplication.adapters.InstructorReviewAdapter;
import com.example.myapplication.adapters.StudentAdapter;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.activities.common.EditProfileActivity;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.InstructorReviewItem;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.LanguageManager;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InstructorMainActivity extends AppCompatActivity
        implements InstructorCourseAdapter.OnCourseActionListener {

    public static final String EXTRA_DESTINATION = "EXTRA_DESTINATION";
    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    public static final String DESTINATION_HOME = "home";
    public static final String DESTINATION_COURSES = "courses";
    public static final String DESTINATION_STUDENTS = "students";
    public static final String DESTINATION_REVENUE = "revenue";
    public static final String DESTINATION_REVIEWS = "reviews";
    public static final String DESTINATION_ACCOUNT = "account";

    private FrameLayout contentContainer;
    private BottomNavigationView bottomNav;
    private InstructorViewModel instructorViewModel;
    private RevenueViewModel revenueViewModel;
    private StudentListViewModel studentListViewModel;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private ReviewRepository reviewRepository;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> editCourseLauncher;
    private String pendingStudentCourseId;
    private String pendingRevenueCourseId;
    private String pendingReviewCourseId;
    private String selectedReviewCourseId;
    private Spinner spReviewCourseFilter;
    private TextView tvReviewCourseName, tvAverageRating, tvReviewCount, tvEmptyReviews;
    private RecyclerView rvReviews;
    private InstructorReviewAdapter reviewAdapter;
    private List<Course> reviewCourses = new ArrayList<>();
    private List<InstructorReviewItem> allReviewItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!isInstructorSession()) {
            redirectToLogin(getString(R.string.login_with_instructor_account));
            return;
        }

        registerEditCourseLauncher();
        setContentView(R.layout.activity_instructor_main);

        contentContainer = findViewById(R.id.contentContainer);
        bottomNav = findViewById(R.id.bottomNav);
        userRepository = new UserRepository(this);
        courseRepository = new CourseRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        reviewRepository = new ReviewRepository(this);

        RetrofitClient.setAuthErrorListener(() -> runOnUiThread(() ->
                redirectToLogin(getString(R.string.session_expired_login_again))));

        setupBottomNav();
        showInitialDestination(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && !isInstructorSession()) {
            redirectToLogin(getString(R.string.session_expired_login_again));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RetrofitClient.setAuthErrorListener(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showInitialDestination(intent);
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

        String initialCourseId = pendingStudentCourseId;
        pendingStudentCourseId = null;
        RecyclerView rvStudents = root.findViewById(R.id.rvStudents);
        EditText etSearch = root.findViewById(R.id.etSearch);
        Spinner spCourseFilter = root.findViewById(R.id.spCourseFilter);
        TextView tvEmptyStudents = root.findViewById(R.id.tvEmptyStudents);
        TextView tvTotalEnrollments = root.findViewById(R.id.tvTotalEnrollments);
        TextView tvUniqueStudents = root.findViewById(R.id.tvUniqueStudents);
        TextView tvAverageProgress = root.findViewById(R.id.tvAverageProgress);
        TextView tvCompletedEnrollments = root.findViewById(R.id.tvCompletedEnrollments);
        StudentAdapter adapter = new StudentAdapter();
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        studentListViewModel = new ViewModelProvider(this).get(StudentListViewModel.class);
        clearStudentObservers();
        studentListViewModel.setCourseFilter(initialCourseId);
        studentListViewModel.getStudents().observe(this, students -> {
            adapter.setStudents(students);
            boolean isEmpty = students == null || students.isEmpty();
            tvEmptyStudents.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvStudents.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        studentListViewModel.getCourses().observe(this, courses ->
                setupStudentCourseFilter(spCourseFilter, courses, studentListViewModel.getSelectedCourseIdValue()));
        studentListViewModel.getTotalEnrollments().observe(this, tvTotalEnrollments::setText);
        studentListViewModel.getUniqueStudents().observe(this, tvUniqueStudents::setText);
        studentListViewModel.getAverageProgress().observe(this, tvAverageProgress::setText);
        studentListViewModel.getCompletedEnrollments().observe(this, tvCompletedEnrollments::setText);
        studentListViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
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

        String initialCourseId = pendingRevenueCourseId;
        pendingRevenueCourseId = null;
        BarChart barChart = root.findViewById(R.id.barChart);
        PieChart pieChart = root.findViewById(R.id.pieChart);
        Spinner spRevenueCourseFilter = root.findViewById(R.id.spRevenueCourseFilter);
        TextView tvTotalRevenue = root.findViewById(R.id.tvTotalRevenue);
        TextView tvTotalTransactions = root.findViewById(R.id.tvTotalTransactions);
        TextView tvAverageRevenue = root.findViewById(R.id.tvAverageRevenue);
        TextView tvRevenueCourseName = root.findViewById(R.id.tvRevenueCourseName);
        TextView tvEmptyTransactions = root.findViewById(R.id.tvEmptyTransactions);
        RecyclerView rvTransactions = root.findViewById(R.id.rvTransactions);
        EnrollmentTransactionAdapter transactionAdapter = new EnrollmentTransactionAdapter();

        setupRevenueCharts(barChart, pieChart);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setAdapter(transactionAdapter);

        revenueViewModel = new ViewModelProvider(this).get(RevenueViewModel.class);
        clearRevenueObservers();
        revenueViewModel.setCourseFilter(initialCourseId);
        revenueViewModel.getBarEntries().observe(this, entries -> {
            BarDataSet dataSet = new BarDataSet(entries, getString(R.string.monthly_revenue));
            dataSet.setColor(getColor(R.color.accent_muted));
            dataSet.setValueTextColor(getColor(R.color.text_primary));
            barChart.setData(new BarData(dataSet));
            barChart.invalidate();
        });
        revenueViewModel.getPieEntries().observe(this, entries -> {
            PieDataSet dataSet = new PieDataSet(entries, getString(R.string.course_sales));
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextColor(getColor(R.color.text_primary));
            dataSet.setValueTextSize(12f);
            pieChart.setData(new PieData(dataSet));
            pieChart.invalidate();
        });
        revenueViewModel.getTotalRevenue().observe(this, tvTotalRevenue::setText);
        revenueViewModel.getTotalTransactions().observe(this, tvTotalTransactions::setText);
        revenueViewModel.getAverageTransactionValue().observe(this, tvAverageRevenue::setText);
        revenueViewModel.getSelectedCourseName().observe(this, tvRevenueCourseName::setText);
        revenueViewModel.getCourses().observe(this, courses ->
                setupRevenueCourseFilter(spRevenueCourseFilter, courses, revenueViewModel.getSelectedCourseIdValue()));
        revenueViewModel.getRecentTransactions().observe(this, transactions -> {
            transactionAdapter.setTransactions(transactions);
            boolean isEmpty = transactions == null || transactions.isEmpty();
            tvEmptyTransactions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        revenueViewModel.fetchRevenueData();
    }

    private void showReviews() {
        View root = inflateContent(R.layout.activity_instructor_reviews);
        String initialCourseId = pendingReviewCourseId;
        pendingReviewCourseId = null;

        View btnBack = root.findViewById(R.id.btnBack);
        spReviewCourseFilter = root.findViewById(R.id.spReviewCourseFilter);
        tvReviewCourseName = root.findViewById(R.id.tvReviewCourseName);
        tvAverageRating = root.findViewById(R.id.tvAverageRating);
        tvReviewCount = root.findViewById(R.id.tvReviewCount);
        tvEmptyReviews = root.findViewById(R.id.tvEmptyReviews);
        rvReviews = root.findViewById(R.id.rvReviews);

        reviewAdapter = new InstructorReviewAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        btnBack.setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_instructor_courses));
        loadInstructorReviews(initialCourseId);
    }

    private void loadInstructorReviews(String initialCourseId) {
        selectedReviewCourseId = hasValue(initialCourseId) ? initialCourseId : null;

        courseRepository.getByInstructor(sessionManager.getUserId(), new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                reviewCourses = courses != null ? courses : new ArrayList<>();
                ensureSelectedReviewCourseExists();
                reviewRepository.getAll(new ReviewRepository.RepositoryCallback<List<Review>>() {
                    @Override
                    public void onSuccess(List<Review> reviews) {
                        allReviewItems = buildInstructorReviewItems(reviews);
                        setupReviewCourseFilter();
                        applyReviewFilter();
                    }

                    @Override
                    public void onError(String message) {
                        allReviewItems = new ArrayList<>();
                        setupReviewCourseFilter();
                        applyReviewFilter();
                        Toast.makeText(InstructorMainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                reviewCourses = new ArrayList<>();
                allReviewItems = new ArrayList<>();
                setupReviewCourseFilter();
                applyReviewFilter();
                Toast.makeText(InstructorMainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<InstructorReviewItem> buildInstructorReviewItems(List<Review> reviews) {
        Map<String, Course> courseById = new HashMap<>();
        for (Course course : reviewCourses) {
            if (course.getId() != null) {
                courseById.put(course.getId(), course);
            }
        }

        List<InstructorReviewItem> items = new ArrayList<>();
        if (reviews != null) {
            for (Review review : reviews) {
                if (review == null || review.getCourseId() == null) continue;
                Course course = courseById.get(review.getCourseId());
                if (course != null) {
                    items.add(new InstructorReviewItem(review, course));
                }
            }
        }

        Collections.sort(items, (first, second) -> {
            String firstDate = first.getReview() != null ? first.getReview().getCreatedAt() : "";
            String secondDate = second.getReview() != null ? second.getReview().getCreatedAt() : "";
            if (firstDate == null) firstDate = "";
            if (secondDate == null) secondDate = "";
            return secondDate.compareTo(firstDate);
        });
        return items;
    }

    private void setupReviewCourseFilter() {
        if (spReviewCourseFilter == null) return;

        List<String> labels = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        labels.add(getString(R.string.all_courses));
        ids.add(null);
        for (Course course : reviewCourses) {
            if (course.getId() == null) continue;
            labels.add(hasValue(course.getTitle()) ? course.getTitle() : getString(R.string.untitled_course));
            ids.add(course.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spReviewCourseFilter.setOnItemSelectedListener(null);
        spReviewCourseFilter.setAdapter(adapter);

        int selectedIndex = findCourseIndex(ids, selectedReviewCourseId);
        spReviewCourseFilter.setSelection(selectedIndex, false);
        selectedReviewCourseId = ids.get(selectedIndex);

        spReviewCourseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < ids.size()) {
                    selectedReviewCourseId = ids.get(position);
                    applyReviewFilter();
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyReviewFilter() {
        List<InstructorReviewItem> visibleReviews = new ArrayList<>();
        int ratingTotal = 0;

        for (InstructorReviewItem item : allReviewItems) {
            Course course = item.getCourse();
            if (selectedReviewCourseId != null && (course == null || !selectedReviewCourseId.equals(course.getId()))) {
                continue;
            }
            visibleReviews.add(item);
            if (item.getReview() != null) {
                ratingTotal += item.getReview().getRating();
            }
        }

        reviewAdapter.setReviews(visibleReviews);
        boolean isEmpty = visibleReviews.isEmpty();
        tvEmptyReviews.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvReviews.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvReviewCount.setText(String.valueOf(visibleReviews.size()));
        float average = isEmpty ? 0f : ratingTotal / (float) visibleReviews.size();
        tvAverageRating.setText(String.format(Locale.US, "%.1f", average));
        tvReviewCourseName.setText(resolveReviewCourseName());
    }

    private void ensureSelectedReviewCourseExists() {
        if (selectedReviewCourseId == null) return;
        for (Course course : reviewCourses) {
            if (selectedReviewCourseId.equals(course.getId())) {
                return;
            }
        }
        selectedReviewCourseId = null;
    }

    private String resolveReviewCourseName() {
        if (selectedReviewCourseId == null) {
            return getString(R.string.all_courses);
        }
        for (Course course : reviewCourses) {
            if (selectedReviewCourseId.equals(course.getId())) {
                return hasValue(course.getTitle()) ? course.getTitle() : getString(R.string.untitled_course);
            }
        }
        return getString(R.string.all_courses);
    }

    private void showAccount() {
        View root = inflateContent(R.layout.account_activity);
        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setVisibility(View.GONE);

        TextView tvFullName = root.findViewById(R.id.tvFullName);
        TextView tvEmail = root.findViewById(R.id.tvEmail);
        TextView tvBio = root.findViewById(R.id.tvBio);
        View btnEditProfile = root.findViewById(R.id.btnEditProfile);
        View btnLogout = root.findViewById(R.id.btnLogout);
        setupAccountLanguage(root);
        applyAccountLanguageText(root);
        tvFullName.setText(R.string.loading);
        tvEmail.setText("");
        if (tvBio != null) {
            tvBio.setText(R.string.loading);
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            redirectToLogin(getString(R.string.session_expired_login_again));
            return;
        }

        userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                tvFullName.setText(user.getFullName() != null ? user.getFullName() : getString(R.string.instructor_fallback));
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                if (tvBio != null) {
                    tvBio.setText(hasValue(user.getBio()) ? user.getBio() : getString(R.string.no_bio_available));
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(InstructorMainActivity.this, R.string.error_loading_account, Toast.LENGTH_SHORT).show();
            }
        });

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupAccountLanguage(View root) {
        Spinner spLanguage = root.findViewById(R.id.spLanguage);
        if (spLanguage == null) return;

        String[] labels = {getString(R.string.language_english), getString(R.string.language_vietnamese)};
        String[] codes = {LanguageManager.LANGUAGE_ENGLISH, LanguageManager.LANGUAGE_VIETNAMESE};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spLanguage.setAdapter(adapter);
        spLanguage.setSelection(findLanguageIndex(codes, LanguageManager.getSavedLanguage(this)), false);
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= codes.length) return;
                String selectedCode = codes[position];
                if (!selectedCode.equals(LanguageManager.getSavedLanguage(InstructorMainActivity.this))) {
                    getIntent().putExtra(EXTRA_DESTINATION, DESTINATION_ACCOUNT);
                    LanguageManager.saveLanguage(InstructorMainActivity.this, selectedCode);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int findLanguageIndex(String[] codes, String selectedCode) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(selectedCode)) {
                return i;
            }
        }
        return 0;
    }

    private void applyAccountLanguageText(View root) {
        setText(root, R.id.tvAccountTitle, getString(R.string.my_account));
        setText(root, R.id.tvBioLabel, getString(R.string.bio_label));
        setText(root, R.id.tvLanguageLabel, getString(R.string.app_language));
        setText(root, R.id.tvLanguageHint, getString(R.string.choose_display_language));
        setText(root, R.id.btnEditProfile, getString(R.string.edit_profile_upper));
        setText(root, R.id.btnLogout, getString(R.string.logout_upper));
    }

    private void setText(View root, int viewId, String text) {
        TextView textView = root.findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
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
        final int actionStudents = 1;
        final int actionRevenue = 2;
        final int actionReviews = 3;
        final int actionStatus = 4;
        final int actionEdit = 5;
        final int actionDelete = 6;

        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(0, actionStudents, 0, getString(R.string.manage_students));
        popupMenu.getMenu().add(0, actionRevenue, 1, getString(R.string.view_revenue));
        popupMenu.getMenu().add(0, actionReviews, 2, getString(R.string.view_reviews));
        popupMenu.getMenu().add(0, actionStatus, 3, getString(R.string.edit_course_status));
        popupMenu.getMenu().add(0, actionEdit, 4, getString(R.string.edit_course_action));
        popupMenu.getMenu().add(0, actionDelete, 5, getString(R.string.delete_course));
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == actionStudents) {
                openStudentsForCourse(course);
            } else if (itemId == actionRevenue) {
                openRevenueForCourse(course);
            } else if (itemId == actionReviews) {
                openReviewsForCourse(course);
            } else if (itemId == actionStatus) {
                showEditStatusDialog(course);
            } else if (itemId == actionEdit) {
                openEditCourse(course);
            } else if (itemId == actionDelete) {
                confirmDeleteCourse(course);
            }
            return true;
        });
        popupMenu.show();
    }

    private void setupRevenueCharts(BarChart barChart, PieChart pieChart) {
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setTextColor(getColor(R.color.text_primary));
        barChart.getAxisLeft().setTextColor(getColor(R.color.text_primary));
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(getColor(R.color.text_primary));
        barChart.setNoDataText(getString(R.string.no_revenue_data));

        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setCenterTextColor(getColor(R.color.text_primary));
        pieChart.getLegend().setTextColor(getColor(R.color.text_primary));
        pieChart.setNoDataText(getString(R.string.no_course_revenue_data));
    }

    private void openStudentsForCourse(Course course) {
        pendingStudentCourseId = course != null ? course.getId() : null;
        if (bottomNav.getSelectedItemId() == R.id.nav_instructor_students) {
            showStudents();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_instructor_students);
        }
    }

    private void openRevenueForCourse(Course course) {
        pendingRevenueCourseId = course != null ? course.getId() : null;
        if (bottomNav.getSelectedItemId() == R.id.nav_instructor_revenue) {
            showRevenue();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_instructor_revenue);
        }
    }

    private void openReviewsForCourse(Course course) {
        pendingReviewCourseId = course != null ? course.getId() : null;
        showReviews();
    }

    private void showEditStatusDialog(Course course) {
        if (course == null) return;
        if (instructorViewModel == null) {
            instructorViewModel = new ViewModelProvider(this).get(InstructorViewModel.class);
        }

        String[] labels = {
                getString(R.string.course_status_published),
                getString(R.string.course_status_pending)
        };
        String[] values = {"approved", "pending"};

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_course_status)
                .setSingleChoiceItems(labels, findStatusSelection(course.getStatus()), (dialog, which) -> {
                    if (which >= 0 && which < values.length) {
                        instructorViewModel.updateCourseStatus(course, values[which]);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private int findStatusSelection(String status) {
        if (!hasValue(status)) return -1;
        String normalized = status.trim().toLowerCase(Locale.US).replace("-", "_").replace(" ", "_");
        if (normalized.contains("pending") || normalized.contains("review") || normalized.contains("submitted") || normalized.contains("waiting") || normalized.contains("reject")) {
            return 1;
        }
        if (normalized.contains("publish") || normalized.equals("approved") || normalized.equals("active") || normalized.equals("public")) {
            return 0;
        }
        if (normalized.contains("draft") || normalized.contains("private") || normalized.contains("hidden") || normalized.contains("inactive") || normalized.contains("unpublish")) {
            return 1;
        }
        return -1;
    }

    private void confirmDeleteCourse(Course course) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_course)
                .setMessage(R.string.delete_course_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> instructorViewModel.deleteCourse(course.getId()))
                .show();
    }

    private void setupStudentCourseFilter(Spinner spinner, List<Course> courses, String selectedCourseId) {
        List<String> labels = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        labels.add(getString(R.string.all_courses));
        ids.add(null);

        if (courses != null) {
            for (Course course : courses) {
                if (course.getId() == null) continue;
                labels.add(hasValue(course.getTitle()) ? course.getTitle() : getString(R.string.untitled_course));
                ids.add(course.getId());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setOnItemSelectedListener(null);
        spinner.setAdapter(adapter);

        int selectedIndex = findCourseIndex(ids, selectedCourseId);
        spinner.setSelection(selectedIndex, false);
        studentListViewModel.setCourseFilter(ids.get(selectedIndex));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < ids.size()) {
                    studentListViewModel.setCourseFilter(ids.get(position));
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showInitialDestination(Intent intent) {
        String destination = intent != null ? intent.getStringExtra(EXTRA_DESTINATION) : null;
        String courseId = intent != null ? intent.getStringExtra(EXTRA_COURSE_ID) : null;

        if (DESTINATION_COURSES.equals(destination)) {
            if (bottomNav.getSelectedItemId() == R.id.nav_instructor_courses) {
                showCourses();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_instructor_courses);
            }
        } else if (DESTINATION_STUDENTS.equals(destination)) {
            pendingStudentCourseId = courseId;
            if (bottomNav.getSelectedItemId() == R.id.nav_instructor_students) {
                showStudents();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_instructor_students);
            }
        } else if (DESTINATION_REVENUE.equals(destination)) {
            pendingRevenueCourseId = courseId;
            if (bottomNav.getSelectedItemId() == R.id.nav_instructor_revenue) {
                showRevenue();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_instructor_revenue);
            }
        } else if (DESTINATION_REVIEWS.equals(destination)) {
            pendingReviewCourseId = courseId;
            showReviews();
        } else if (DESTINATION_ACCOUNT.equals(destination)) {
            if (bottomNav.getSelectedItemId() == R.id.nav_instructor_account) {
                showAccount();
            } else {
                bottomNav.setSelectedItemId(R.id.nav_instructor_account);
            }
        } else if (bottomNav.getSelectedItemId() == R.id.nav_instructor_home) {
            showDashboard();
        } else {
            bottomNav.setSelectedItemId(R.id.nav_instructor_home);
        }
    }

    private void setupRevenueCourseFilter(Spinner spinner, List<Course> courses, String selectedCourseId) {
        List<String> labels = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        labels.add(getString(R.string.all_courses));
        ids.add(null);

        if (courses != null) {
            for (Course course : courses) {
                if (course.getId() == null) continue;
                labels.add(hasValue(course.getTitle()) ? course.getTitle() : getString(R.string.untitled_course));
                ids.add(course.getId());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setOnItemSelectedListener(null);
        spinner.setAdapter(adapter);

        int selectedIndex = findCourseIndex(ids, selectedCourseId);
        spinner.setSelection(selectedIndex, false);
        revenueViewModel.setCourseFilter(ids.get(selectedIndex));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < ids.size()) {
                    revenueViewModel.setCourseFilter(ids.get(position));
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int findCourseIndex(List<String> ids, String courseId) {
        if (!hasValue(courseId)) return 0;
        for (int i = 0; i < ids.size(); i++) {
            if (courseId.equals(ids.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void displayProfileCourses(LinearLayout container, List<Course> courses) {
        container.removeAllViews();
        if (courses == null || courses.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(R.string.no_courses_yet);
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
            tvLessonCount.setText(getString(R.string.lesson_count_format, course.getLessonCount()));
            tvPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));
            InstructorCourseAdapter.bindStatus(this, tvStatus, course.getStatus());
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
        revenueViewModel.getTotalTransactions().removeObservers(this);
        revenueViewModel.getAverageTransactionValue().removeObservers(this);
        revenueViewModel.getSelectedCourseName().removeObservers(this);
        revenueViewModel.getCourses().removeObservers(this);
        revenueViewModel.getRecentTransactions().removeObservers(this);
    }

    private void clearStudentObservers() {
        if (studentListViewModel == null) return;

        studentListViewModel.getStudents().removeObservers(this);
        studentListViewModel.getCourses().removeObservers(this);
        studentListViewModel.getTotalEnrollments().removeObservers(this);
        studentListViewModel.getUniqueStudents().removeObservers(this);
        studentListViewModel.getAverageProgress().removeObservers(this);
        studentListViewModel.getCompletedEnrollments().removeObservers(this);
        studentListViewModel.getErrorMessage().removeObservers(this);
    }

}
