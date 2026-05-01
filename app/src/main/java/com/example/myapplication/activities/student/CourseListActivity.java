package com.example.myapplication.activities.student;

import android.content.Intent;
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

    private LinearLayout courseListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // Correct ID from activity_course_list.xml is lnCourseList
        courseListContainer = (LinearLayout) findViewById(R.id.lnCourseList);

        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        List<Course> courses = MockData.getCourses();
        if (courses != null && courseListContainer != null) {
            for (int i = 0; i < courses.size(); i++) {
                final Course course = courses.get(i);
                View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

                TextView title = itemView.findViewById(R.id.tvCourseTitle);
                TextView instructor = itemView.findViewById(R.id.tvInstructor);
                TextView price = itemView.findViewById(R.id.tvPrice);
                ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

                if (title != null) title.setText(course.getTitle());
                
                String instructorName = "Instructor";
                User inst = MockData.getUserById(course.getInstructorId());
                if (inst != null) instructorName = inst.getFullName();
                
                if (instructor != null) instructor.setText(instructorName);
                if (price != null) price.setText("đ" + String.format("%,.0f", course.getPrice() * 1000));
                if (thumb != null) thumb.setImageResource(R.drawable.image_courses);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CourseListActivity.this, CourseDetailActivity.class);
                        intent.putExtra("course_id", course.getId());
                        intent.putExtra("course_title", course.getTitle());
                        startActivity(intent);
                    }
                });

                courseListContainer.addView(itemView);
            }
        }
    }
}
