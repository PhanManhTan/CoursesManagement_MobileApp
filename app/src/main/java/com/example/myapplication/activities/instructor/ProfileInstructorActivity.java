package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class ProfileInstructorActivity extends AppCompatActivity {

    LinearLayout lnInstructorCourses;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_instructor);

        lnInstructorCourses = findViewById(R.id.lnInstructorCourses);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String[][] instructorCourses = {
                {"Advanced UI Architecture", "Dr. Sarah Jenkins", "$89.99"},
                {"Design Patterns in Java", "Dr. Sarah Jenkins", "$75.00"},
                {"Clean Code Principles", "Dr. Sarah Jenkins", "$45.00"}
        };

        for (int i = 0; i < instructorCourses.length; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(instructorCourses[i][0]);e
            instructor.setText(instructorCourses[i][1]);
            price.setText(instructorCourses[i][2]);
            thumb.setImageResource(R.drawable.ic_launcher_background);

            lnInstructorCourses.addView(itemView);
        }
    }
}