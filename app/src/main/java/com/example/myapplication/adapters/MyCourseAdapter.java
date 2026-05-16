package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import java.util.ArrayList;
import java.util.List;

public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Enrollment enrollment);
    }

    private List<Enrollment> enrollmentList = new ArrayList<>();
    private OnItemClickListener listener;

    public void setEnrollmentList(List<Enrollment> enrollmentList) {
        this.enrollmentList = enrollmentList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_course, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Enrollment enrollment = enrollmentList.get(position);
        Course course = enrollment.getCourse();

        if (course != null) {
            holder.tvName.setText(course.getTitle());
            holder.tvAuthor.setText("Instructor ID: " + course.getInstructorId());
            
            if (course.getThumbnailUrl() != null && !course.getThumbnailUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                     .load(course.getThumbnailUrl())
                     .placeholder(R.drawable.image_courses)
                     .into(holder.ivThumb);
            } else {
                holder.ivThumb.setImageResource(R.drawable.image_courses);
            }
        }

        holder.pb.setProgress(enrollment.getProgress());
        holder.tvPercent.setText(enrollment.getProgress() + "% COMPLETE");
        
        // Placeholder for lesson count as it might need another API call or nested data
        holder.tvLessonCount.setText(""); 

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(enrollment);
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

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCourseName);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvPercent = v.findViewById(R.id.tvPercent);
            tvLessonCount = v.findViewById(R.id.tvLessonCount);
            pb = v.findViewById(R.id.pbCourse);
            ivThumb = v.findViewById(R.id.ivCourseThumb);
        }
    }
}
