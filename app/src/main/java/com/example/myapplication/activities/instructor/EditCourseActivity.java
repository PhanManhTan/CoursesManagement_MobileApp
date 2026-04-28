package com.example.myapplication.activities.instructor;

import android.os.Bundle;
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
        setContentView(R.layout.activity_edit_course);

        initViews();
        setupRecyclerView();
        loadInitialData();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        rvLessons = findViewById(R.id.rvLessons);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddLesson).setOnClickListener(v -> addNewLesson());

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu toàn bộ thay đổi!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(lessonList, null);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(lessonAdapter);
        rvLessons.setNestedScrollingEnabled(false);
    }

    private void loadInitialData() {
        if (lessonList.isEmpty()) {
            lessonList.add(new Lesson("1", "Bài học số 1", "00:00"));
            lessonAdapter.notifyDataSetChanged();
        }
    }

    private void addNewLesson() {
        int nextId = lessonList.size() + 1;
        lessonList.add(new Lesson(String.valueOf(nextId), "Bài học mới " + nextId, "00:00"));
        lessonAdapter.notifyItemInserted(lessonList.size() - 1);
        rvLessons.smoothScrollToPosition(lessonList.size() - 1);
    }
}
