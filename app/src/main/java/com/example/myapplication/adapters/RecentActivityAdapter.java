package com.example.myapplication.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Notification;
import com.example.myapplication.utils.NotificationLocalizer;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
    private List<Notification> activityList;

    public RecentActivityAdapter(List<Notification> activityList) {
        this.activityList = activityList;
    }

    public void setActivities(List<Notification> activityList) {
        this.activityList = activityList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Notification notification = activityList.get(position);

        NotificationLocalizer.localize(holder.itemView.getContext(), notification, holder.tvTitle, holder.tvMessage);

        String rawDate = notification.getCreatedAt();
        if (rawDate != null && !rawDate.isEmpty()) {
            holder.tvTime.setText(formatDateTime(rawDate));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // Custom styling based on activity type (Title)
        String titleLower = notification.getTitle() != null ? notification.getTitle().toLowerCase() : "";
        if (titleLower.contains("approved") || titleLower.contains("duyệt")) {
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B3B2B")));
            holder.ivIcon.setImageResource(R.drawable.ic_check);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else if (titleLower.contains("rejected") || titleLower.contains("reject") || titleLower.contains("từ chối")) {
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3B1F24")));
            holder.ivIcon.setImageResource(R.drawable.ic_close_24);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EF5350")));
        } else if (titleLower.contains("purchased") || titleLower.contains("purchase") || titleLower.contains("mua")) {
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1F1C42")));
            holder.ivIcon.setImageResource(R.drawable.ic_cart);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#5A4FCF")));
        } else {
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E285A")));
            holder.ivIcon.setImageResource(R.drawable.ic_notification);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9E96E3")));
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    private String formatDateTime(String rawDate) {
        try {
            // rawDate format is usually "2026-05-31T12:00:00+00:00" or similar
            String clean = rawDate.replace("T", " ");
            if (clean.contains(".")) {
                clean = clean.substring(0, clean.indexOf("."));
            }
            if (clean.contains("+")) {
                clean = clean.substring(0, clean.indexOf("+"));
            }
            return clean;
        } catch (Exception e) {
            return rawDate;
        }
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivIcon = itemView.findViewById(R.id.ivActivityIcon);
            tvTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvMessage = itemView.findViewById(R.id.tvActivityMessage);
            tvTime = itemView.findViewById(R.id.tvActivityTime);
        }
    }
}
