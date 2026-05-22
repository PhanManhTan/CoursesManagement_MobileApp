package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.InstructorCourseAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.viewmodels.InstructorViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity
        implements InstructorCourseAdapter.OnCourseActionListener {

    private RecyclerView rvInstructorCourses;
    private InstructorCourseAdapter courseAdapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private InstructorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_course_list);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        rvInstructorCourses = findViewById(R.id.rvInstructorCourses);
        fabAddCourse = findViewById(R.id.fabAddCourse);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("My Courses");
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new InstructorCourseAdapter(this, courseList, this);
        rvInstructorCourses.setLayoutManager(new LinearLayoutManager(this));
        rvInstructorCourses.setAdapter(courseAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InstructorViewModel.class);

        viewModel.getInstructorCourses().observe(this, courses -> {
            courseList = courses != null ? courses : new ArrayList<>();
            courseAdapter.setCourses(courseList);
            if (tvEmpty != null) {
                tvEmpty.setVisibility(courseList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        if (fabAddCourse != null) {
            fabAddCourse.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditCourseActivity.class);
                intent.putExtra("IS_NEW_COURSE", true);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra("IS_NEW_COURSE", false);
        intent.putExtra(EditCourseActivity.EXTRA_COURSE_ID, course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        intent.putExtra(EditCourseActivity.EXTRA_COURSE, course);
        startActivity(intent);
    }

    @Override
    public void onCourseMenuClick(Course course, View anchor) {
        new AlertDialog.Builder(this)
                .setTitle("Delete course")
                .setMessage("Do you want to delete this course?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteCourse(course.getId()))
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshCourses();
        }
    }
}
