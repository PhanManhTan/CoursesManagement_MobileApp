package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.InstructorReviewItem;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class InstructorReviewAdapter extends RecyclerView.Adapter<InstructorReviewAdapter.ViewHolder> {

    private final List<InstructorReviewItem> reviews = new ArrayList<>();
    private UserRepository userRepository;

    public void setReviews(List<InstructorReviewItem> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instructor_review, parent, false);
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstructorReviewItem item = reviews.get(position);
        Review review = item.getReview();
        Course course = item.getCourse();
        Context context = holder.itemView.getContext();

        holder.tvReviewerName.setText(R.string.student_fallback);
        holder.ivReviewerAvatar.setImageResource(R.drawable.ic_user);
        holder.tvCourseTitle.setText(course != null && hasValue(course.getTitle()) ? course.getTitle() : context.getString(R.string.untitled_course));
        holder.tvReviewDate.setText(formatDate(context, review != null ? review.getCreatedAt() : null));
        holder.rbReviewRating.setRating(review != null ? review.getRating() : 0);
        holder.tvReviewComment.setText(review != null && hasValue(review.getComment()) ? review.getComment() : context.getString(R.string.no_comment));

        if (review == null || !hasValue(review.getUserId())) {
            return;
        }

        userRepository.getById(review.getUserId(), new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                holder.tvReviewerName.setText(hasValue(user.getFullName()) ? user.getFullName() : context.getString(R.string.student_fallback));
                if (hasValue(user.getAvatarUrl())) {
                    Glide.with(holder.itemView.getContext())
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(holder.ivReviewerAvatar);
                }
            }

            @Override
            public void onError(String message) {
                holder.tvReviewerName.setText(R.string.unknown_student);
                holder.ivReviewerAvatar.setImageResource(R.drawable.ic_user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String formatDate(Context context, String value) {
        if (!hasValue(value)) return context.getString(R.string.na_value);
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivReviewerAvatar;
        TextView tvReviewerName, tvCourseTitle, tvReviewDate, tvReviewComment;
        RatingBar rbReviewRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerAvatar = itemView.findViewById(R.id.ivReviewerAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvCourseTitle = itemView.findViewById(R.id.tvReviewCourseTitle);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewComment = itemView.findViewById(R.id.tvReviewComment);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
        }
    }
}
