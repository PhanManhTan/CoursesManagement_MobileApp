package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.InstructorCourseAdapter;
import com.example.myapplication.models.Course;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity
        implements InstructorCourseAdapter.OnCourseActionListener {

    private RecyclerView rvInstructorCourses;
    private InstructorCourseAdapter courseAdapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_course_list);

        // Khởi tạo View
        rvInstructorCourses = findViewById(R.id.rvInstructorCourses);
        fabAddCourse = findViewById(R.id.fabAddCourse);
        TextView tvTitle = findViewById(R.id.tvTitle);
        
        if (tvTitle != null) tvTitle.setText("My Courses");

        setupRecyclerView();
        loadCourses();
        
        if (fabAddCourse != null) {
            fabAddCourse.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditCourseActivity.class);
                intent.putExtra("IS_NEW_COURSE", true);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new InstructorCourseAdapter(this, courseList, this);
        rvInstructorCourses.setLayoutManager(new LinearLayoutManager(this));
        rvInstructorCourses.setAdapter(courseAdapter);
    }

    private void loadCourses() {
        courseList.clear();
        courseList.add(new Course("1", "Lập trình Android Pro", 24, "12h 30m", 49.99, "PUBLISHED", android.R.drawable.ic_menu_gallery));
        courseList.add(new Course("2", "Thiết kế giao diện UI/UX", 15, "08h 20m", 29.99, "DRAFT", android.R.drawable.ic_menu_gallery));
        courseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCourseClick(Course course) {
        // Mở màn hình Edit khi click vào khóa học
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra("IS_NEW_COURSE", false);
        intent.putExtra("COURSE_ID", course.getId());
        intent.putExtra("COURSE_TITLE", course.getTitle());
        startActivity(intent);
    }

    @Override
    public void onCourseMenuClick(Course course, View anchor) {
        // Cũng có thể mở Edit từ Menu More nếu muốn
        onCourseClick(course);
    }
}
