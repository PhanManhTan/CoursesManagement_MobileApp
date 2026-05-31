package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.utils.LanguageManager;

public class StudentListActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "EXTRA_COURSE_ID";
    public static final String EXTRA_COURSE_TITLE = "EXTRA_COURSE_TITLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, InstructorMainActivity.class);
        intent.putExtra(InstructorMainActivity.EXTRA_DESTINATION, InstructorMainActivity.DESTINATION_STUDENTS);

        String courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        if (courseId != null && !courseId.trim().isEmpty()) {
            intent.putExtra(InstructorMainActivity.EXTRA_COURSE_ID, courseId);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
