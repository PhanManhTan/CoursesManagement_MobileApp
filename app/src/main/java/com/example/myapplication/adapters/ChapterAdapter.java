package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.HashSet;
import java.util.Set;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final Set<Integer> expandedPositions = new HashSet<>();

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        String[] chapterNames = {"Getting Started", "Core Principles", "Deep Dive", "Conclusion"};
        holder.tvChapterTitle.setText("Chapter " + (position + 1) + ": " + chapterNames[position % chapterNames.length]);

        boolean isExpanded = expandedPositions.contains(position);
        holder.layoutLessonsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpandArrow.setRotation(isExpanded ? 90 : -90);

        holder.layoutChapterHeader.setOnClickListener(v -> {
            if (expandedPositions.contains(position)) {
                expandedPositions.remove(position);
                holder.layoutLessonsContainer.setVisibility(View.GONE);
                holder.ivExpandArrow.animate().rotation(-90).setDuration(200).start();
            } else {
                expandedPositions.add(position);
                holder.layoutLessonsContainer.setVisibility(View.VISIBLE);
                holder.ivExpandArrow.animate().rotation(90).setDuration(200).start();
            }
        });

        addDummyLessons(holder.layoutLessonsContainer);
    }

    private void addDummyLessons(LinearLayout container) {
        container.removeAllViews();
        String[] titles = {"Introduction to Course", "Setting up Environment", "First Practical Steps"};
        String[] times = {"12:45 mins", "24:20 mins", "15:00 mins"};

        for (int i = 0; i < titles.length; i++) {
            View lessonView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_lesson, container, false);

            TextView tvTitle = lessonView.findViewById(R.id.tvLessonTitle);
            TextView tvTime = lessonView.findViewById(R.id.tvLessonDuration);

            tvTitle.setText(titles[i]);
            tvTime.setText(times[i]);

            container.addView(lessonView);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterTitle;
        LinearLayout layoutLessonsContainer;
        RelativeLayout layoutChapterHeader;
        ImageView ivExpandArrow;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            layoutLessonsContainer = itemView.findViewById(R.id.layoutLessonsContainer);
            layoutChapterHeader = itemView.findViewById(R.id.layoutChapterHeader);
            ivExpandArrow = itemView.findViewById(R.id.ivExpandArrow);
        }
    }
}
