package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.StudentCourseProgress;
import com.example.myapplication.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<StudentCourseProgress> students = new ArrayList<>();

    public void setStudents(List<StudentCourseProgress> newStudents) {
        this.students = newStudents != null ? newStudents : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentCourseProgress row = students.get(position);
        User student = row.getStudent();
        Course course = row.getCourse();

        String studentName = hasValue(student != null ? student.getFullName() : null)
                ? student.getFullName()
                : holder.itemView.getContext().getString(R.string.student_fallback);
        String email = hasValue(student != null ? student.getEmail() : null)
                ? student.getEmail()
                : holder.itemView.getContext().getString(R.string.no_email);
        String courseTitle = hasValue(course != null ? course.getTitle() : null)
                ? course.getTitle()
                : holder.itemView.getContext().getString(R.string.untitled_course);

        holder.tvName.setText(studentName);
        holder.tvEmail.setText(email);
        holder.tvCourse.setText(courseTitle);
        holder.tvProgressText.setText(holder.itemView.getContext().getString(
                R.string.student_progress_format,
                row.getProgressPercent(),
                row.getCompletedLessons(),
                row.getTotalLessons()
        ));
        holder.tvStatus.setText(row.isCompleted() ? R.string.completed : R.string.in_progress);
        holder.tvStatus.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                row.isCompleted() ? R.color.status_success : R.color.status_warning
        ));
        holder.tvAvatar.setText(studentName.substring(0, 1).toUpperCase(Locale.US));
        holder.pbProgress.setProgress(row.getProgressPercent());
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvAvatar, tvCourse, tvProgressText, tvStatus;
        ProgressBar pbProgress;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvAvatar = itemView.findViewById(R.id.tvStudentAvatar);
            tvCourse = itemView.findViewById(R.id.tvStudentCourse);
            tvProgressText = itemView.findViewById(R.id.tvStudentProgressText);
            tvStatus = itemView.findViewById(R.id.tvStudentStatus);
            pbProgress = itemView.findViewById(R.id.pbStudentProgress);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
