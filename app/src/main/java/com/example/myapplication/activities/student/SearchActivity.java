package com.example.myapplication.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class SearchActivity extends AppCompatActivity {

    LinearLayout lnCategory;
    LinearLayout lnSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        lnCategory = findViewById(R.id.lnCategory);
        lnSearchResults = findViewById(R.id.lnSearchResults);

        String[] list = {"All","Design","Business","IT","Marketing","Music"};
        for (int i = 0; i < list.length; i++) {
            Button btn = (Button) getLayoutInflater().inflate(R.layout.item_category, null);
            btn.setText(list[i]);
            btn.setOnClickListener(v -> {
                for (int j = 0; j < lnCategory.getChildCount(); j++) {
                    Button b = (Button) lnCategory.getChildAt(j);
                    b.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
                }
                btn.setBackgroundTintList(getColorStateList(R.color.purple_500));
            });
            lnCategory.addView(btn);
        }

        String[][] courseData = {
                {"Advanced UI Architecture", "Dr. Sarah Jenkins", "$89.99"},
                {"Motion Systems in Product Design", "Marcus Thorne", "$64.50"},
                {"Rust for Enterprise Systems", "Elena Rodriguez", "$112.00"},
                {"The Art of Minimalist Typography", "Jameson Bell", "$45.99"},
                {"Scalable Frontend Performance", "Amara Okoro", "$78.00"}
        };

        for (int i = 0; i < courseData.length; i++) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(courseData[i][0]);
            instructor.setText(courseData[i][1]);
            price.setText(courseData[i][2]);
            thumb.setImageResource(R.drawable.ic_launcher_background);

            lnSearchResults.addView(itemView);
        }
    }
}