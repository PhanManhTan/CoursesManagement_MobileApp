package com.example.myapplication.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

import com.example.myapplication.models.Category;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.MockData;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    LinearLayout lnCategory;
    LinearLayout lnSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        lnCategory = findViewById(R.id.lnCategory);
        lnSearchResults = findViewById(R.id.lnSearchResults);

        List<Category> categories = MockData.getCategories();
        for (Category category : categories) {
            com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) getLayoutInflater().inflate(R.layout.item_category, null);
            btn.setText(category.getName());
            
            btn.setOnClickListener(v -> {
                // Handle category click
            });
            lnCategory.addView(btn);
        }

        List<Course> courses = MockData.getCourses();
        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(course.getTitle());
            
            String instructorName = "Instructor";
            User inst = MockData.getUserById(course.getInstructorId());
            if (inst != null) instructorName = inst.getFullName();
            
            instructor.setText(instructorName);
            price.setText("đ" + String.format("%,.0f", course.getPrice() * 1000));
            thumb.setImageResource(R.drawable.image_courses);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, CourseDetailActivity.class);
                intent.putExtra("course_id", course.getId());
                intent.putExtra("course_title", course.getTitle());
                startActivity(intent);
            });

            lnSearchResults.addView(itemView);
        }
    }
}