package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Report;
import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reports = new ArrayList<>();

    public void setReports(List<Report> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        // Updated to use fields from Report model: courseId, reason, userId, createdAt
        holder.tvType.setText(holder.itemView.getContext().getString(R.string.course_id_format, report.getCourseId()));
        holder.tvReason.setText(report.getReason());
        holder.tvMeta.setText(holder.itemView.getContext().getString(R.string.reporter_meta_format, report.getUserId(), report.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvReason, tvMeta;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvReportType);
            tvReason = itemView.findViewById(R.id.tvReportReason);
            tvMeta = itemView.findViewById(R.id.tvReportMeta);
        }
    }
}
