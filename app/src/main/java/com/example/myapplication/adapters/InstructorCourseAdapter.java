package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;

import java.util.ArrayList;
import java.util.List;

public class InstructorCourseAdapter extends RecyclerView.Adapter<InstructorCourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Course> courseList;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onCourseClick(Course course);
        void onCourseMenuClick(Course course, View anchor);
    }

    public InstructorCourseAdapter(Context context, List<Course> courseList, OnCourseActionListener listener) {
        this.context = context;
        this.courseList = courseList;
        this.listener = listener;
    }

    public void setCourses(List<Course> courses) {
        this.courseList = courses != null ? courses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.tvCourseName.setText(course.getTitle());
        String duration = course.getDuration() != null ? course.getDuration() : "N/A";
        holder.tvLessonCount.setText(course.getLessonCount() + " Lessons • " + duration);
        holder.tvPrice.setText("$" + String.format("%.2f", course.getPrice()));
        holder.tvStatus.setText(course.getStatus());

        // Update status background/color based on status
        if ("PUBLISHED".equalsIgnoreCase(course.getStatus()) || "APPROVED".equalsIgnoreCase(course.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_published);
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            // Default or DRAFT
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_published); // Should have a draft bg ideally
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .placeholder(R.drawable.image_courses)
                    .into(holder.ivCourseThumb);
        } else if (course.getThumbnailResId() != 0) {
            holder.ivCourseThumb.setImageResource(course.getThumbnailResId());
        } else {
            holder.ivCourseThumb.setImageResource(R.drawable.image_courses);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onCourseMenuClick(course, v);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourseThumb;
        TextView tvCourseName, tvLessonCount, tvPrice, tvStatus;
        ImageButton btnMore;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseThumb = itemView.findViewById(R.id.ivCourseThumb);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvLessonCount = itemView.findViewById(R.id.tvLessonCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
