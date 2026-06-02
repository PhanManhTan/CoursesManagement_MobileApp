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
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewList = new ArrayList<>();
    private UserRepository userRepository;

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_detail_review, parent, false);
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.rbRating.setRating(review.getRating());
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "");

        // Fetch user data to display name and avatar
        userRepository.getById(review.getUserId(), new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    holder.tvName.setText(user.getName() != null ? user.getName() : holder.itemView.getContext().getString(R.string.student_fallback));

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .into(holder.ivAvatar);
                    } else {
                        holder.ivAvatar.setImageResource(R.drawable.ic_user);
                    }
                }
            }

            @Override
            public void onError(String message) {
                holder.tvName.setText(R.string.unknown_student);
                holder.ivAvatar.setImageResource(R.drawable.ic_user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvName, tvComment;
        RatingBar rbRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivReviewerAvatar);
            tvName = itemView.findViewById(R.id.tvReviewerName);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            rbRating = itemView.findViewById(R.id.rbReviewRating);
        }
    }
}
