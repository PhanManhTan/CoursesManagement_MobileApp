package com.example.myapplication.activities.student;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ChapterAdapter;

public class CourseDetailActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setNestedScrollingEnabled(false);

        ChapterAdapter chapterAdapter = new ChapterAdapter();
        rvLessons.setAdapter(chapterAdapter);
    }
}
