package com.example.myapplication.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.ReviewRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Review;
import com.example.myapplication.models.User;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    private OnItemClickListener listener;
    private List<Course> courses = new ArrayList<>();
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_home, parent, false);

        if (reviewRepository == null) {
            reviewRepository = new ReviewRepository(parent.getContext());
        }
        if (userRepository == null) {
            userRepository = new UserRepository(parent.getContext());
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvTitle.setText(course.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(course);
        });

        if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .placeholder(R.drawable.image_courses)
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(R.drawable.image_courses);
        }

        holder.tvInstructor.setText(R.string.loading);
        if (course.getInstructorId() != null) {
            userRepository.getById(course.getInstructorId(), new UserRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && user.getName() != null) {
                        holder.tvInstructor.setText(user.getName());
                    } else {
                        holder.tvInstructor.setText(R.string.unknown_instructor);
                    }
                }

                @Override
                public void onError(String message) {
                    holder.tvInstructor.setText(R.string.unknown_instructor);
                }
            });
        } else {
            holder.tvInstructor.setText(R.string.unknown_instructor);
        }

        double originalPrice = course.getPrice();
        double discountPrice = course.getDiscountPrice();

        holder.tvPrice.setText(holder.itemView.getContext().getString(R.string.vnd_price_format, discountPrice));

        if (originalPrice > discountPrice) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(holder.itemView.getContext().getString(R.string.vnd_price_format, originalPrice));

            int discountPercent = (int) Math.round(((originalPrice - discountPrice) / originalPrice) * 100);
            holder.tvDiscountPercent.setVisibility(View.VISIBLE);
            holder.tvDiscountPercent.setText("-" + discountPercent + "%");
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscountPercent.setVisibility(View.GONE);
        }

        holder.tvRatingValue.setText("0.0");
        holder.tvReviewCount.setText("(0)");

        reviewRepository.getByCourseId(course.getId(), new ReviewRepository.RepositoryCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (reviews != null && !reviews.isEmpty()) {
                    int totalReviews = reviews.size();
                    float sumRating = 0;

                    for (Review r : reviews) {
                        sumRating += r.getRating();
                    }

                    float averageRating = sumRating / totalReviews;

                    holder.tvRatingValue.setText(String.format("%.1f", averageRating));
                    holder.tvReviewCount.setText("(" + totalReviews + ")");
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice, tvOriginalPrice, tvDiscountPercent, tvRatingValue, tvReviewCount;
        ImageView ivThumb;
        public ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvCourseTitle);
            tvInstructor = v.findViewById(R.id.tvInstructor);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvOriginalPrice = v.findViewById(R.id.tvOriginalPrice);
            tvDiscountPercent = v.findViewById(R.id.tvDiscountPercent);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvRatingValue = v.findViewById(R.id.tvRatingValue);
            tvReviewCount = v.findViewById(R.id.tvReviewCount);
            ivThumb = v.findViewById(R.id.ivCourseThumb);
        }
    }
}