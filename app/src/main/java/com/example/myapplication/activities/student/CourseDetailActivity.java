package com.example.myapplication.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ChapterAdapter;

public class CourseDetailActivity extends AppCompatActivity {

    private RecyclerView rvLessons;
    private ImageView btnBack;
    private Button btnEnroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        initViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());
        btnEnroll.setOnClickListener(v -> {
            Intent intent = new Intent(this, LearningActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        btnBack = findViewById(R.id.btnBack);
        btnEnroll = findViewById(R.id.btnEnroll);
    }

    private void setupRecyclerView() {
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setNestedScrollingEnabled(false);

        ChapterAdapter chapterAdapter = new ChapterAdapter();
        rvLessons.setAdapter(chapterAdapter);
    }
}
