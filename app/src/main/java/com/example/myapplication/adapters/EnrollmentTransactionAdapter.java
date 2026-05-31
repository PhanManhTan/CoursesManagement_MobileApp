package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.utils.CurrencyFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnrollmentTransactionAdapter extends RecyclerView.Adapter<EnrollmentTransactionAdapter.TransactionViewHolder> {

    private List<Enrollment> transactions = new ArrayList<>();

    public void setTransactions(List<Enrollment> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enrollment_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Enrollment enrollment = transactions.get(position);
        Course course = enrollment.getCourse();

        String courseName = course != null && course.getTitle() != null
                ? course.getTitle()
                : "Course " + enrollment.getCourseId();
        String date = formatDate(enrollment.getCreatedAt(), enrollment.getEnrolledAt());

        holder.tvCourseTitle.setText(courseName);
        holder.tvDate.setText(date);
        holder.tvAmount.setText("+" + CurrencyFormatter.formatVnd(enrollment.getPaidAmount()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String formatDate(String createdAt, String enrolledAt) {
        String value = createdAt != null && !createdAt.isEmpty() ? createdAt : enrolledAt;
        if (value == null || value.isEmpty()) return "N/A";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseTitle, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
