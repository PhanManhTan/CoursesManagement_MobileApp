package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String duration = course.getDuration() != null ? course.getDuration() : context.getString(R.string.na_value);
        holder.tvLessonCount.setText(context.getString(R.string.lesson_count_with_duration_format, course.getLessonCount(), duration));
        holder.tvPrice.setText(context.getString(R.string.vnd_price_format, course.getPrice()));
        bindStatus(context, holder.tvStatus, course.getStatus());

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

    public static void bindStatus(Context context, TextView statusView, String status) {
        StatusUi statusUi = resolveStatusUi(context, status);
        statusView.setText(statusUi.label);
        statusView.setBackgroundResource(statusUi.backgroundRes);
        statusView.setTextColor(ContextCompat.getColor(context, statusUi.textColorRes));
    }

    private static StatusUi resolveStatusUi(Context context, String status) {
        String normalized = status == null
                ? ""
                : status.trim().toLowerCase(Locale.US).replace("-", "_").replace(" ", "_");

        if (normalized.contains("reject") || normalized.contains("decline") || normalized.contains("denied")) {
            return new StatusUi(
                    context.getString(R.string.course_status_rejected),
                    R.drawable.bg_course_status_rejected,
                    R.color.status_error
            );
        }
        if (normalized.contains("pending")
                || normalized.contains("review")
                || normalized.contains("submitted")
                || normalized.contains("waiting")) {
            return new StatusUi(
                    context.getString(R.string.course_status_pending),
                    R.drawable.bg_course_status_pending,
                    R.color.status_warning
            );
        }
        if (normalized.contains("draft")
                || normalized.contains("private")
                || normalized.contains("hidden")
                || normalized.contains("inactive")
                || normalized.contains("unpublish")) {
            return new StatusUi(
                    context.getString(R.string.course_status_pending),
                    R.drawable.bg_course_status_pending,
                    R.color.status_warning
            );
        }
        if (normalized.contains("publish")
                || normalized.equals("approved")
                || normalized.equals("active")
                || normalized.equals("public")) {
            return new StatusUi(
                    context.getString(R.string.course_status_published),
                    R.drawable.bg_course_status_published,
                    R.color.status_success
            );
        }
        return new StatusUi(
                context.getString(R.string.course_status_pending),
                R.drawable.bg_course_status_pending,
                R.color.status_warning
        );
    }

    private static class StatusUi {
        final String label;
        final int backgroundRes;
        final int textColorRes;

        StatusUi(String label, int backgroundRes, int textColorRes) {
            this.label = label;
            this.backgroundRes = backgroundRes;
            this.textColorRes = textColorRes;
        }
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
