package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.ChapterWithLessons;
import com.example.myapplication.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class InstructorChapterAdapter extends RecyclerView.Adapter<InstructorChapterAdapter.ChapterViewHolder> {
    private final List<ChapterWithLessons> chapters = new ArrayList<>();
    private final OnChapterActionListener listener;
    private boolean actionsEnabled = true;

    public interface OnChapterActionListener {
        void onEditChapter(int chapterPosition);
        void onDeleteChapter(int chapterPosition);
        void onAddLesson(int chapterPosition);
        void onEditLesson(int chapterPosition, int lessonPosition);
        void onDeleteLesson(int chapterPosition, int lessonPosition);
    }

    public InstructorChapterAdapter(OnChapterActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChapterWithLessons> data) {
        chapters.clear();
        if (data != null) {
            chapters.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setActionsEnabled(boolean enabled) {
        if (actionsEnabled == enabled) {
            return;
        }
        actionsEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instructor_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        ChapterWithLessons draft = chapters.get(position);
        Chapter chapter = draft.getChapter();
        List<Lesson> lessons = draft.getLessons();

        String chapterTitle = chapter != null && hasValue(chapter.getTitle())
                ? chapter.getTitle().trim()
                : holder.itemView.getContext().getString(R.string.default_chapter_title, position + 1);
        holder.tvChapterTitle.setText(holder.itemView.getContext().getString(R.string.chapter_title_format, position + 1, chapterTitle));
        holder.tvLessonCount.setText(holder.itemView.getContext().getString(R.string.lesson_count_format, lessons.size()));
        holder.tvEmptyLessons.setVisibility(lessons.isEmpty() ? View.VISIBLE : View.GONE);

        holder.btnEditChapter.setOnClickListener(v -> {
            if (!actionsEnabled) return;
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onEditChapter(adapterPosition);
            }
        });
        holder.btnDeleteChapter.setOnClickListener(v -> {
            if (!actionsEnabled) return;
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteChapter(adapterPosition);
            }
        });
        holder.btnAddLesson.setOnClickListener(v -> {
            if (!actionsEnabled) return;
            int adapterPosition = holder.getBindingAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onAddLesson(adapterPosition);
            }
        });
        holder.btnEditChapter.setEnabled(actionsEnabled);
        holder.btnDeleteChapter.setEnabled(actionsEnabled);
        holder.btnAddLesson.setEnabled(actionsEnabled);
        float actionAlpha = actionsEnabled ? 1.0f : 0.45f;
        holder.btnEditChapter.setAlpha(actionAlpha);
        holder.btnDeleteChapter.setAlpha(actionAlpha);
        holder.btnAddLesson.setAlpha(actionAlpha);

        renderLessonRows(holder, lessons, position);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    private void renderLessonRows(ChapterViewHolder holder, List<Lesson> lessons, int chapterPosition) {
        holder.llLessonRows.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());

        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            View row = inflater.inflate(R.layout.item_instructor_chapter_lesson, holder.llLessonRows, false);
            TextView tvLessonIndex = row.findViewById(R.id.tvLessonIndex);
            TextView tvLessonTitle = row.findViewById(R.id.tvLessonTitle);
            View btnEditLesson = row.findViewById(R.id.btnEditLesson);
            View btnDeleteLesson = row.findViewById(R.id.btnDeleteLesson);

            tvLessonIndex.setText(holder.itemView.getContext().getString(R.string.default_lesson_title, i + 1));
            String lessonTitle = hasValue(lesson.getTitle()) ? lesson.getTitle().trim() : "";
            tvLessonTitle.setText(hasValue(lessonTitle) ? lessonTitle : holder.itemView.getContext().getString(R.string.untitled_lesson));

            final int lessonPosition = i;
            btnEditLesson.setOnClickListener(v -> {
                if (!actionsEnabled) return;
                int currentChapterPosition = holder.getBindingAdapterPosition();
                if (listener != null && currentChapterPosition != RecyclerView.NO_POSITION) {
                    listener.onEditLesson(currentChapterPosition, lessonPosition);
                }
            });
            btnDeleteLesson.setOnClickListener(v -> {
                if (!actionsEnabled) return;
                int currentChapterPosition = holder.getBindingAdapterPosition();
                if (listener != null && currentChapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteLesson(currentChapterPosition, lessonPosition);
                }
            });
            btnEditLesson.setEnabled(actionsEnabled);
            btnDeleteLesson.setEnabled(actionsEnabled);
            btnEditLesson.setAlpha(actionsEnabled ? 1.0f : 0.45f);
            btnDeleteLesson.setAlpha(actionsEnabled ? 1.0f : 0.45f);

            holder.llLessonRows.addView(row);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterTitle;
        TextView tvLessonCount;
        TextView tvEmptyLessons;
        View btnEditChapter;
        View btnDeleteChapter;
        LinearLayout llLessonRows;
        Button btnAddLesson;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            tvLessonCount = itemView.findViewById(R.id.tvLessonCount);
            tvEmptyLessons = itemView.findViewById(R.id.tvEmptyLessons);
            btnEditChapter = itemView.findViewById(R.id.btnEditChapter);
            btnDeleteChapter = itemView.findViewById(R.id.btnDeleteChapter);
            llLessonRows = itemView.findViewById(R.id.llLessonRows);
            btnAddLesson = itemView.findViewById(R.id.btnAddLesson);
        }
    }
}
