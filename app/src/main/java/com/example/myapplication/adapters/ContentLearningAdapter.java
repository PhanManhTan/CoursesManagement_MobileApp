package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;

public class ContentLearningAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_LESSON = 0;
    public static final int TYPE_DISCUSSION = 1;
    public static final int TYPE_QUIZ = 2;
    public static final int TYPE_FILE = 3;

    private int currentType = TYPE_LESSON;

    public void setContentType(int type) {
        this.currentType = type;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return currentType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_DISCUSSION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discussion, parent, false);
                return new DiscussionViewHolder(view);
            case TYPE_QUIZ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_question, parent, false);
                return new QuizViewHolder(view);
            case TYPE_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
                return new FileViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson_learning, parent, false);
                return new LessonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LessonViewHolder) {
            ((LessonViewHolder) holder).tvTitle.setText("Lesson " + (position + 1) + ": Overview");
        } else if (holder instanceof DiscussionViewHolder) {
            ((DiscussionViewHolder) holder).tvUser.setText("User " + (position + 1));
        } else if (holder instanceof FileViewHolder) {
            ((FileViewHolder) holder).tvName.setText("Document_0" + (position + 1) + ".pdf");
        }
    }

    @Override
    public int getItemCount() {
        return 8;
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        LessonViewHolder(View v) { super(v); tvTitle = v.findViewById(R.id.tvLessonTitle); }
    }

    static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser;
        DiscussionViewHolder(View v) { super(v); tvUser = v.findViewById(R.id.tvUserName); }
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        QuizViewHolder(View v) { super(v); }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        FileViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tvFileName); }
    }
}