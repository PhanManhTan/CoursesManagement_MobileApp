package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CourseApprovalAdapter extends RecyclerView.Adapter<CourseApprovalAdapter.CourseViewHolder> {
    public interface OnApprovalListener {
        void onApprove(Course course);
        void onReject(Course course);
    }

    private List<Course> courses = new ArrayList<>();
    private OnApprovalListener listener;

    public void setListener(OnApprovalListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<Course> newCourses) {
        if (newCourses != null) {
            this.courses = newCourses;
        } else {
            this.courses = new ArrayList<>();
        }
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
        
        // Handling long UUIDs gracefully
        String instructorDisplay = course.getInstructorId();
        if (instructorDisplay != null && instructorDisplay.length() > 8) {
            instructorDisplay = "ID: " + instructorDisplay.substring(0, 8) + "...";
        }
        holder.tvInstructor.setText(instructorDisplay);
        holder.tvPrice.setText("$" + course.getPrice());

        // Using Glide for thumbnail loading
        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .placeholder(R.drawable.image_courses)
                    .error(R.drawable.image_courses)
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(R.drawable.image_courses);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(course);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(course);
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice;
        ImageView ivThumb;
        MaterialButton btnApprove, btnReject;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructorName);
            tvPrice = itemView.findViewById(R.id.tvCoursePrice);
            ivThumb = itemView.findViewById(R.id.ivCourseThumb);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
