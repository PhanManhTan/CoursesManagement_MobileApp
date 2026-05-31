package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;

public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Enrollment enrollment);
    }

    public interface OnReviewClickListener {
        void onReviewClick(Enrollment enrollment);
    }

    private List<Enrollment> enrollmentList = new ArrayList<>();
    private OnItemClickListener listener;
    private OnReviewClickListener reviewListener;
    private UserRepository userRepository;

    public void setEnrollmentList(List<Enrollment> enrollmentList) {
        this.enrollmentList = enrollmentList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnReviewClickListener(OnReviewClickListener reviewListener) {
        this.reviewListener = reviewListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_course, parent, false);
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Enrollment enrollment = enrollmentList.get(position);
        Course course = enrollment.getCourse();

        if (course != null) {
            holder.tvName.setText(course.getTitle());
            holder.tvAuthor.setText(R.string.loading);

            userRepository.getById(course.getInstructorId(), new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        holder.tvAuthor.setText(user.getName());
                    }
                }

                @Override
                public void onError(String message) {
                    holder.tvAuthor.setText(R.string.unknown_instructor);
                }
            });

            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(course.getThumbnailUrl())
                        .placeholder(R.drawable.image_courses)
                        .into(holder.ivThumb);
            } else {
                holder.ivThumb.setImageResource(R.drawable.image_courses);
            }

            int completed = enrollment.getProgress();
            int total = enrollment.getTotalLessons();
            holder.tvLessonCount.setText(holder.itemView.getContext().getString(R.string.progress_lessons_format, completed, total));

            if (total > 0) {
                int percent = (completed * 100) / total;
                holder.pb.setProgress(percent);
                holder.tvPercent.setText(holder.itemView.getContext().getString(R.string.progress_complete_format, percent));

                if (percent == 100) {
                    holder.btnReview.setVisibility(View.VISIBLE);
                } else {
                    holder.btnReview.setVisibility(View.GONE);
                }
            } else {
                holder.pb.setProgress(0);
                holder.tvPercent.setText(holder.itemView.getContext().getString(R.string.progress_complete_format, 0));
                holder.btnReview.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(enrollment);
        });

        holder.btnReview.setOnClickListener(v -> {
            if (reviewListener != null) reviewListener.onReviewClick(enrollment);
        });
    }

    @Override
    public int getItemCount() {
        return enrollmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPercent, tvAuthor, tvLessonCount;
        ProgressBar pb;
        ImageView ivThumb;
        Button btnReview;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCourseName);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvPercent = v.findViewById(R.id.tvPercent);
            tvLessonCount = v.findViewById(R.id.tvLessonCount);
            pb = v.findViewById(R.id.pbCourse);
            ivThumb = v.findViewById(R.id.ivCourseThumb);
            btnReview = v.findViewById(R.id.btnReview);
        }
    }
}
