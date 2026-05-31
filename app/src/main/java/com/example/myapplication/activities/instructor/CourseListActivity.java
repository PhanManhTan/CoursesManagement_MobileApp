package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.utils.LanguageManager;

public class CourseListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, InstructorMainActivity.class);
        intent.putExtra(InstructorMainActivity.EXTRA_DESTINATION, InstructorMainActivity.DESTINATION_COURSES);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
