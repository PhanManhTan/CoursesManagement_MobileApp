package com.example.myapplication.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class CourseListActivity extends AppCompatActivity {

    LinearLayout lnCourseList;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        lnCourseList = findViewById(R.id.lnCourseList);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String[][] categoryData = {
                {"Logo Design Masterclass 2026", "John Doe", "$49.99"},
                {"UI/UX Design Essentials", "Jane Smith", "$55.00"},
                {"Adobe Illustrator CC Mastery", "Mike Ross", "$39.99"},
                {"Figma for Beginners", "Sarah Connor", "$29.99"},
                {"Typography and Layout", "Jameson Bell", "$45.99"},
                {"Color Theory for Designers", "Elena Rodriguez", "$35.00"}
        };

        for (int i = 0; i < categoryData.length; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(categoryData[i][0]);
            instructor.setText(categoryData[i][1]);
            price.setText(categoryData[i][2]);
            thumb.setImageResource(R.drawable.ic_launcher_background);

            lnCourseList.addView(itemView);
        }
    }
}