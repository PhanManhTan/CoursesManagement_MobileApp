package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CourseApprovalAdapter extends RecyclerView.Adapter<CourseApprovalAdapter.CourseViewHolder> {

    private List<Course> courses = new ArrayList<>();

    public void setCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_approval, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvTitle.setText(course.getTitle());
        holder.tvInstructor.setText("By Instructor ID: " + course.getInstructorId());
        holder.tvPrice.setText("$" + course.getPrice());
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice;
        MaterialButton btnApprove, btnReject;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructorName);
            tvPrice = itemView.findViewById(R.id.tvCoursePrice);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
