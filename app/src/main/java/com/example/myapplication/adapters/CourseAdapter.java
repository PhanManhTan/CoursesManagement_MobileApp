package com.example.myapplication.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.utils.MockData;
import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    private OnItemClickListener listener;
    private List<Course> courses = new ArrayList<>();

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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvTitle.setText(course.getTitle());
        
        // Link click to listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(course);
        });
        
        // Fetch instructor name from mock data
        String instructorName = "Unknown Instructor";
        if (course.getInstructorId() != null) {
            com.example.myapplication.models.User instructor = MockData.getUserById(course.getInstructorId());
            if (instructor != null) {
                instructorName = instructor.getFullName();
            }
        }
        holder.tvInstructor.setText(instructorName);
        
        // Formatting price for VND as shown in image
        holder.tvPrice.setText("đ" + String.format("%,.0f", course.getDiscountPrice() > 0 ? course.getDiscountPrice() * 1000 : course.getPrice() * 1000));
        
        if (course.getDiscountPrice() > 0) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText("đ" + String.format("%,.0f", course.getPrice() * 1000));
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }

        // Mock rating and badge
        holder.tvRatingValue.setText("4.8");
        holder.tvReviewCount.setText("(24)");
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvPrice, tvOriginalPrice, tvRatingValue, tvReviewCount;
        ImageView ivThumb;
        public ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvCourseTitle);
            tvInstructor = v.findViewById(R.id.tvInstructor);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvOriginalPrice = v.findViewById(R.id.tvOriginalPrice);
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvRatingValue = v.findViewById(R.id.tvRatingValue);
            tvReviewCount = v.findViewById(R.id.tvReviewCount);
            ivThumb = v.findViewById(R.id.ivCourseThumb);
        }
    }
}
