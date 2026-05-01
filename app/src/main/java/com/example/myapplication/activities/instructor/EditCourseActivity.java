package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.LessonAdapter;
import com.example.myapplication.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class EditCourseActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etPrice;
    private RecyclerView rvLessons;
    private LessonAdapter lessonAdapter;
    private final List<Lesson> lessonList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SỬA LỖI: Sử dụng đúng layout chi tiết có quản lý bài học
        setContentView(R.layout.activity_instructor_course_detail);

        initViews();
        setupRecyclerView();
        loadInitialData();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        rvLessons = findViewById(R.id.rvLessons);

        // Nút quay lại (ID btnBack)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Nút thêm bài học (ID btnAddLesson)
        View btnAddLesson = findViewById(R.id.btnAddLesson);
        if (btnAddLesson != null) {
            btnAddLesson.setOnClickListener(v -> addNewLesson());
        }

        // Nút lưu (ID btnSave)
        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(this, "Course saved successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }
    }

    private void setupRecyclerView() {
        if (rvLessons != null) {
            lessonAdapter = new LessonAdapter(lessonList, null);
            rvLessons.setLayoutManager(new LinearLayoutManager(this));
            rvLessons.setAdapter(lessonAdapter);
            // Quan trọng: Để không bị xung đột cuộn với ScrollView bên ngoài
            rvLessons.setNestedScrollingEnabled(false);
        }
    }

    private void loadInitialData() {
        if (getIntent() != null) {
            String title = getIntent().getStringExtra("COURSE_TITLE");
            if (title != null && etTitle != null) {
                etTitle.setText(title);
            }
        }

        // Dữ liệu mẫu bài học để hiển thị danh sách
        lessonList.add(new Lesson("1", "Overview and Introduction", "05:00"));
        lessonList.add(new Lesson("2", "Project Setup", "10:45"));
        if (lessonAdapter != null) lessonAdapter.notifyDataSetChanged();
    }

    private void addNewLesson() {
        if (lessonAdapter != null) {
            int nextId = lessonList.size() + 1;
            lessonList.add(new Lesson(String.valueOf(nextId), "Lesson " + nextId, "00:00"));
            lessonAdapter.notifyItemInserted(lessonList.size() - 1);
            if (rvLessons != null) rvLessons.smoothScrollToPosition(lessonList.size() - 1);
        }
    }
}
