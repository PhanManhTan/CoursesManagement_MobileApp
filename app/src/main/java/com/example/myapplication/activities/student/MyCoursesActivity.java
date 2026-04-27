package com.example.myapplication.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MyCourseAdapter;

public class MyCoursesActivity extends AppCompatActivity {

    private RecyclerView rvMyCourses;
    private ImageView btnBack;
    private MyCourseAdapter myCourseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvMyCourses = findViewById(R.id.rvMyCourses);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        // Thiết lập danh sách cuộn dọc
        rvMyCourses.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo và gắn adapter
        myCourseAdapter = new MyCourseAdapter();
        myCourseAdapter.setOnItemClickListener(title -> {
            Intent intent = new Intent(this, LearningActivity.class);
            intent.putExtra("course_title", title);
            startActivity(intent);
        });
        rvMyCourses.setAdapter(myCourseAdapter);

        // Tối ưu hóa hiệu năng nếu kích thước item không đổi
        rvMyCourses.setHasFixedSize(true);
    }
}