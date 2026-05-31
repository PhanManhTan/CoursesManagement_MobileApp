package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        // Luôn giữ background resource để có khung (rounded corners và stroke)
        holder.itemView.setBackgroundResource(R.drawable.bg_rounded_dark);

        if (!notification.isRead()) {
            // Thông báo chưa đọc: Hiện rõ, có thể đổi màu nền hoặc stroke để nhấn mạnh
            holder.tvTitle.setAlpha(1.0f);
            holder.tvMessage.setAlpha(1.0f);
            holder.itemView.setBackgroundTintList(null); // Trở về mặc định của drawable
        } else {
            // Thông báo đã đọc: Làm mờ đi
            holder.tvTitle.setAlpha(0.5f);
            holder.tvMessage.setAlpha(0.5f);
            // Làm mờ cả khung nếu muốn
            holder.itemView.setAlpha(0.8f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
        }
    }
}