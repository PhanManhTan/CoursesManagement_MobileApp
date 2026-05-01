package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.SessionManager;
import java.util.List;

public class ProfileInstructorActivity extends AppCompatActivity {

    private LinearLayout lnInstructorCourses;
    private ImageView btnBack;
    private TextView tvName, tvBio, tvExpertise, tvStudentsCount;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_instructor);

        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        tvExpertise = findViewById(R.id.tvExpertise);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        lnInstructorCourses = findViewById(R.id.lnInstructorCourses);
        btnBack = findViewById(R.id.btnBack);

        userRepository = new UserRepository(this);
        courseRepository = new CourseRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
        sessionManager = new SessionManager(this);

        btnBack.setOnClickListener(v -> finish());

        loadInstructorProfile();
    }

    private void loadInstructorProfile() {
        String instructorId = sessionManager.getUserId();
        if (instructorId == null) return;

        // Fetch User Info
        userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvName.setText(user.getFullName());
                    tvBio.setText(user.getBio() != null ? user.getBio() : "No bio available.");
                    tvExpertise.setText(user.getRole());
                }
            }
            @Override public void onError(String message) {}
        });

        // Fetch Courses
        courseRepository.getByInstructor(instructorId, new CourseRepository.RepositoryCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                if (courses != null) {
                    displayCourses(courses);
                    calculateTotalStudents(courses);
                }
            }
            @Override public void onError(String message) {
                Toast.makeText(ProfileInstructorActivity.this, "Error loading courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateTotalStudents(List<Course> courses) {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> allEnrollments) {
                int total = 0;
                if (allEnrollments != null && courses != null) {
                    for (Course course : courses) {
                        for (Enrollment e : allEnrollments) {
                            if (e.getCourseId() != null && e.getCourseId().equals(course.getId())) {
                                total++;
                            }
                        }
                    }
                }
                tvStudentsCount.setText(String.valueOf(total));
            }
            @Override public void onError(String message) {
                tvStudentsCount.setText("0");
            }
        });
    }

    private void displayCourses(List<Course> courses) {
        lnInstructorCourses.removeAllViews();
        for (Course course : courses) {
            View itemView = getLayoutInflater().inflate(R.layout.item_course_search, null);

            TextView title = itemView.findViewById(R.id.tvCourseTitle);
            TextView instructor = itemView.findViewById(R.id.tvInstructor);
            TextView price = itemView.findViewById(R.id.tvPrice);
            ImageView thumb = itemView.findViewById(R.id.ivCourseThumb);

            title.setText(course.getTitle());
            instructor.setText(tvName.getText());
            price.setText("$" + course.getPrice());

            // Use Glide for live thumbnails
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(this)
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .error(R.drawable.image_courses)
                        .into(thumb);
            } else {
                thumb.setImageResource(R.drawable.image_courses);
            }

            lnInstructorCourses.addView(itemView);
        }
    }
}