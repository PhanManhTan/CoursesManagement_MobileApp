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
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.User;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseApprovalAdapter extends RecyclerView.Adapter<CourseApprovalAdapter.CourseViewHolder> {
    public interface OnApprovalListener {
        void onApprove(Course course);
        void onReject(Course course);
        void onCourseClick(Course course);
    }

    private List<Course> courses = new ArrayList<>();
    private OnApprovalListener listener;
    private UserRepository userRepository;
    private final Map<String, String> instructorNameCache = new HashMap<>();

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
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvTitle.setText(course.getTitle());
        bindInstructorName(holder, course.getInstructorId());
        holder.tvPrice.setText(holder.itemView.getContext().getString(R.string.vnd_price_format, course.getPrice()));

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

        if ("approved".equalsIgnoreCase(course.getStatus())) {
            if (holder.layoutActionButtons != null) {
                holder.layoutActionButtons.setVisibility(View.GONE);
            }
        } else {
            if (holder.layoutActionButtons != null) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
            }
            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(course);
            });

            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(course);
            });
        }

        if (holder.btnPreview != null) {
            holder.btnPreview.setOnClickListener(v -> {
                if (listener != null) listener.onCourseClick(course);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
    }

    private void bindInstructorName(@NonNull CourseViewHolder holder, String instructorId) {
        holder.tvInstructor.setTag(instructorId);
        if (instructorId == null || instructorId.trim().isEmpty()) {
            holder.tvInstructor.setText(R.string.unknown_instructor);
            return;
        }

        String cachedName = instructorNameCache.get(instructorId);
        if (cachedName != null) {
            holder.tvInstructor.setText(cachedName);
            return;
        }

        holder.tvInstructor.setText(R.string.loading);
        userRepository.getById(instructorId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                String name = user != null && hasValue(user.getName())
                        ? user.getName()
                        : holder.itemView.getContext().getString(R.string.unknown_instructor);
                instructorNameCache.put(instructorId, name);
                if (instructorId.equals(holder.tvInstructor.getTag())) {
                    holder.tvInstructor.setText(name);
                }
            }

            @Override
            public void onError(String message) {
                String fallback = holder.itemView.getContext().getString(R.string.unknown_instructor);
                instructorNameCache.put(instructorId, fallback);
                if (instructorId.equals(holder.tvInstructor.getTag())) {
                    holder.tvInstructor.setText(fallback);
                }
            }
        });
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice;
        ImageView ivThumb;
        MaterialButton btnApprove, btnReject, btnPreview;
        View layoutActionButtons;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructorName);
            tvPrice = itemView.findViewById(R.id.tvCoursePrice);
            ivThumb = itemView.findViewById(R.id.ivCourseThumb);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnPreview = itemView.findViewById(R.id.btnPreview);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
        }
    }
}
