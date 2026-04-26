package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.viewmodels.EditCourseViewModel;

public class EditCourseActivity extends AppCompatActivity {

    private EditCourseViewModel viewModel;
    private EditText etTitle, etDescription, etPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);

        viewModel = new ViewModelProvider(this).get(EditCourseViewModel.class);

        // Receive course object from Intent (if editing existing)
        Course existingCourse = (Course) getIntent().getSerializableExtra("COURSE");
        if (existingCourse != null) {
            viewModel.setCourse(existingCourse);
        } else {
            // New course scenario (mocked)
            // Updated to use the 10-argument Course constructor: id, instructorId, title, description, thumbnailUrl, price, discountPrice, status, categoryId, createdAt
            viewModel.setCourse(new Course("tmp", "inst_1", "New Course Title", "Description here", "", 0.0, 0.0, "draft", "cat_1", "2023-11-20"));
        }

        viewModel.getCourse().observe(this, course -> {
            if (course != null) {
                etTitle.setText(course.getTitle());
                etDescription.setText(course.getDescription());
                etPrice.setText(String.valueOf(course.getPrice()));
            }
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Course saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String desc = etDescription.getText().toString();
            String priceStr = etPrice.getText().toString();
            
            if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            viewModel.saveCourse(title, desc, price);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
