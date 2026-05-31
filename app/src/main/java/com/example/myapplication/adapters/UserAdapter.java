package com.example.myapplication.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onBanToggle(User user);
    }

    private List<User> users = new ArrayList<>();
    private OnUserActionListener listener;

    public void setListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> newUsers) {
        if (newUsers != null) {
            this.users = newUsers;
        } else {
            this.users = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(formatRole(holder.itemView, user.getRole()));

        boolean isBanned = "banned".equalsIgnoreCase(user.getStatus());
        if (isBanned) {
            holder.btnBan.setText(R.string.unban);
            holder.btnBan.setTextColor(Color.parseColor("#10B981")); // Emerald/Green
            holder.btnBan.setStrokeColorResource(android.R.color.transparent);
        } else {
            holder.btnBan.setText(R.string.ban);
            holder.btnBan.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.btnBan.setStrokeColorResource(android.R.color.transparent);
        }

        holder.btnBan.setOnClickListener(v -> {
            if (listener != null) listener.onBanToggle(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private String formatRole(View itemView, String role) {
        if (role == null) return "";
        if ("student".equalsIgnoreCase(role)) {
            return itemView.getContext().getString(R.string.student);
        }
        if ("instructor".equalsIgnoreCase(role)) {
            return itemView.getContext().getString(R.string.instructor_fallback);
        }
        if ("admin".equalsIgnoreCase(role)) {
            return itemView.getContext().getString(R.string.admin_role);
        }
        return role;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        MaterialButton btnBan;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnBan = itemView.findViewById(R.id.btnBan);
        }
    }
}
