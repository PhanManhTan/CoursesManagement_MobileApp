package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;

public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String title);
    }

    private OnItemClickListener listener;

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
        String[] titles = {"Advanced UI Architecture", "Mastering Productivity Tools", "React Design Patterns"};
        String title = titles[position % 3];
        holder.tvName.setText(title);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(title);
        });
        
        int progress = (position % 2 == 0 ? 65 : 32);
        holder.pb.setProgress(progress);
        holder.tvPercent.setText(progress + "% COMPLETE");
    }

    @Override
    public int getItemCount() { return 10; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPercent;
        ProgressBar pb;
        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvCourseName);
            tvPercent = v.findViewById(R.id.tvPercent);
            pb = v.findViewById(R.id.pbCourse);
        }
    }
}
