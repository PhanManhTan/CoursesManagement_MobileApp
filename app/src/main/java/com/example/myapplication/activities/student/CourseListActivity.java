package com.example.myapplication.activities.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.MockData;
import java.util.List;

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

        List<Course> courses = MockData.getCourses();
        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(course.getTitle());
            User inst = MockData.getUserById(course.getInstructorId());
            instructor.setText(inst != null ? inst.getFullName() : "Instructor");
            price.setText("đ" + String.format("%,.0f", course.getPrice() * 1000));
            thumb.setImageResource(R.drawable.image_courses);

            lnCourseList.addView(itemView);
        }
    }
}