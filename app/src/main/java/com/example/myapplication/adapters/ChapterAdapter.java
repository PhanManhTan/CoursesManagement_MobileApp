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
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Lesson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final Context context;
    private List<Chapter> chapterList = new ArrayList<>();
    private final Set<Integer> expandedPositions = new HashSet<>();
    private final LessonRepository lessonRepository;

    public ChapterAdapter(Context context) {
        this.context = context;
        this.lessonRepository = new LessonRepository(context);
    }

    public void setChapterList(List<Chapter> chapters) {
        this.chapterList = chapters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.tvChapterTitle.setText(context.getString(R.string.chapter_title_format, position + 1, chapter.getTitle()));

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
                // Load lessons only when expanded
                loadLessons(chapter.getId(), holder.layoutLessonsContainer);
            }
        });

        if (isExpanded) {
            loadLessons(chapter.getId(), holder.layoutLessonsContainer);
        }
    }

    private void loadLessons(String chapterId, LinearLayout container) {
        container.removeAllViews();
        
        // Show a loading or placeholder view if needed, for simplicity we just fetch
        lessonRepository.getByChapterId(chapterId, new LessonRepository.RepositoryCallback<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> lessons) {
                container.removeAllViews();
                if (lessons == null || lessons.isEmpty()) {
                    TextView tvNoLesson = new TextView(context);
                    tvNoLesson.setText(R.string.no_lessons_available_chapter);
                    tvNoLesson.setPadding(50, 20, 20, 20);
                    tvNoLesson.setTextColor(context.getResources().getColor(R.color.text_secondary));
                    container.addView(tvNoLesson);
                    return;
                }

                for (Lesson lesson : lessons) {
                    View lessonView = LayoutInflater.from(context).inflate(R.layout.item_course_detail_lesson, container, false);
                    TextView tvTitle = lessonView.findViewById(R.id.tvLessonTitle);
                    TextView tvTime = lessonView.findViewById(R.id.tvLessonDuration);

                    tvTitle.setText(lesson.getTitle());
                    
                    String durationStr = formatDuration(lesson.getDurationSeconds());
                    tvTime.setText(durationStr);

                    container.addView(lessonView);
                }
            }

            @Override
            public void onError(String message) {
                // Handle error
            }
        });
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0) return "";
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
