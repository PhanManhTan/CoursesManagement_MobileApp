package com.example.myapplication.adapters;

import android.content.Context;
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
import com.example.myapplication.models.LearningDataWrapper.LearningChapter;
import com.example.myapplication.models.LearningDataWrapper.LearningLesson;

import java.util.ArrayList;
import java.util.List;

public class LearningChapterAdapter extends RecyclerView.Adapter<LearningChapterAdapter.ChapterViewHolder> {

    public interface OnLessonClickListener {
        void onLessonValueClick(LearningLesson learningLesson);
    }

    private final Context context;
    private List<LearningChapter> chapterList = new ArrayList<>();
    private final OnLessonClickListener listener;
    private boolean showCompletionStatus = true;

    public LearningChapterAdapter(Context context, OnLessonClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<LearningChapter> chapters) {
        this.chapterList = chapters;
        processLockStatus();
        notifyDataSetChanged();
    }

    public void setShowCompletionStatus(boolean showCompletionStatus) {
        this.showCompletionStatus = showCompletionStatus;
        notifyDataSetChanged();
    }

    private void processLockStatus() {
        boolean isNextUnlocked = true;
        for (LearningChapter chapter : chapterList) {
            for (LearningLesson learningLesson : chapter.getLessons()) {
                learningLesson.setLocked(!isNextUnlocked);

                if (!learningLesson.isLocked()) {
                    int duration = learningLesson.getLesson().getDurationSeconds();
                    boolean isCompleted = learningLesson.getProgress() != null && learningLesson.getProgress().isCompleted();
                    int watchTime = learningLesson.getProgress() != null ? learningLesson.getProgress().getWatchTimeSeconds() : 0;

                    if (isCompleted) {
                        isNextUnlocked = true;
                    } else if (duration > 0) {
                        isNextUnlocked = ((double) watchTime / duration) >= 0.8;
                    } else {
                        isNextUnlocked = true;
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        LearningChapter chapterData = chapterList.get(position);
        holder.tvChapterTitle.setText(context.getString(R.string.chapter_title_format, position + 1, chapterData.getChapter().getTitle()));

        holder.layoutLessonsContainer.setVisibility(chapterData.isExpanded() ? View.VISIBLE : View.GONE);
        holder.ivExpandArrow.setRotation(chapterData.isExpanded() ? 90 : -90);

        holder.layoutChapterHeader.setOnClickListener(v -> {
            chapterData.setExpanded(!chapterData.isExpanded());
            holder.layoutLessonsContainer.setVisibility(chapterData.isExpanded() ? View.VISIBLE : View.GONE);
            holder.ivExpandArrow.animate().rotation(chapterData.isExpanded() ? 90 : -90).setDuration(200).start();

            if (chapterData.isExpanded()) {
                renderLessons(holder.layoutLessonsContainer, chapterData.getLessons());
            }
        });

        if (chapterData.isExpanded()) {
            renderLessons(holder.layoutLessonsContainer, chapterData.getLessons());
        }
    }

    private void renderLessons(LinearLayout container, List<LearningLesson> lessons) {
        container.removeAllViews();
        for (LearningLesson learningLesson : lessons) {
            View lessonView = LayoutInflater.from(context).inflate(R.layout.item_lesson_learning, container, false);

            TextView tvTitle = lessonView.findViewById(R.id.tvLessonTitle);
            TextView tvInfo = lessonView.findViewById(R.id.tvLessonInfo);
            ImageView ivPlayIcon = lessonView.findViewById(R.id.ivPlayIcon);
            ImageView ivLockIcon = lessonView.findViewById(R.id.ivLockIcon);
            View cvLessonContainer = lessonView.findViewById(R.id.cvLessonContainer);

            tvTitle.setText(learningLesson.getLesson().getTitle());
            boolean isCompleted = learningLesson.getProgress() != null && learningLesson.getProgress().isCompleted();
            String durationText = formatDuration(learningLesson.getLesson().getDurationSeconds());
            boolean displayCompleted = showCompletionStatus && isCompleted;
            tvInfo.setText(displayCompleted
                    ? durationText + " • " + context.getString(R.string.completed)
                    : durationText);

            if (learningLesson.isLocked()) {
                ivPlayIcon.setVisibility(View.GONE);
                ivLockIcon.setVisibility(View.VISIBLE);
                cvLessonContainer.setAlpha(0.5f);
                lessonView.setOnClickListener(null);
            } else {
                ivPlayIcon.setVisibility(View.VISIBLE);
                ivLockIcon.setVisibility(View.GONE);
                cvLessonContainer.setAlpha(1.0f);
                lessonView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onLessonValueClick(learningLesson);
                    }
                });
            }
            container.addView(lessonView);
        }
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0) return "00:00";
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
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
