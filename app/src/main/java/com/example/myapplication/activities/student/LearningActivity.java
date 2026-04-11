package com.example.myapplication.activities.student;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ContentLearningAdapter;

import java.util.ArrayList;
import java.util.List;

public class LearningActivity extends AppCompatActivity {

    private RecyclerView rvContent;
    private ContentLearningAdapter adapter;
    private Button btnLessons, btnDiscuss, btnQuiz, btnFiles;
    private ImageView btnBack;
    private final List<Button> tabButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        initViews();
        setupRecyclerView();
        setupTabEvents();

        btnBack.setOnClickListener(v -> finish());
        
        // Initial state
        updateButtonState(btnLessons);
    }

    private void initViews() {
        rvContent = findViewById(R.id.rvContent);
        btnBack = findViewById(R.id.btnBack);
        btnLessons = findViewById(R.id.btnListLess);
        btnDiscuss = findViewById(R.id.btnDiscuss);
        btnQuiz = findViewById(R.id.btnQuizz);
        btnFiles = findViewById(R.id.btnFiles);

        tabButtons.add(btnLessons);
        tabButtons.add(btnDiscuss);
        tabButtons.add(btnQuiz);
        tabButtons.add(btnFiles);
    }

    private void setupRecyclerView() {
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContentLearningAdapter();
        rvContent.setAdapter(adapter);
    }

    private void setupTabEvents() {
        btnLessons.setOnClickListener(v -> {
            updateButtonState(btnLessons);
            adapter.setContentType(ContentLearningAdapter.TYPE_LESSON);
        });

        btnDiscuss.setOnClickListener(v -> {
            updateButtonState(btnDiscuss);
            adapter.setContentType(ContentLearningAdapter.TYPE_DISCUSSION);
        });

        btnQuiz.setOnClickListener(v -> {
            updateButtonState(btnQuiz);
            adapter.setContentType(ContentLearningAdapter.TYPE_QUIZ);
        });

        btnFiles.setOnClickListener(v -> {
            updateButtonState(btnFiles);
            adapter.setContentType(ContentLearningAdapter.TYPE_FILE);
        });
    }

    private void updateButtonState(Button selected) {
        // Use android.R.color.transparent or a custom color if cardview_dark_background is missing
        int defaultColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        int selectedColor = ContextCompat.getColor(this, R.color.purple_500);

        for (Button btn : tabButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == selected ? selectedColor : defaultColor));
        }
    }
}
