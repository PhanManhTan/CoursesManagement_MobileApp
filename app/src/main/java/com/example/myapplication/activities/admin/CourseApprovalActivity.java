package com.example.myapplication.activities.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.CourseApprovalAdapter;
import com.example.myapplication.models.Course;
import com.example.myapplication.viewmodels.CourseApprovalViewModel;

public class CourseApprovalActivity extends AppCompatActivity {

    private CourseApprovalViewModel viewModel;
    private CourseApprovalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_approval);

        RecyclerView rvCourses = findViewById(R.id.rvCoursesPending);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CourseApprovalAdapter();
        rvCourses.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CourseApprovalViewModel.class);
        
        viewModel.getPendingCourses().observe(this, courses -> adapter.setCourses(courses));

        adapter.setListener(new CourseApprovalAdapter.OnApprovalListener() {
            @Override
            public void onApprove(Course course) {
                viewModel.approveCourse(course);
            }

            @Override
            public void onReject(Course course) {
                viewModel.rejectCourse(course);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
