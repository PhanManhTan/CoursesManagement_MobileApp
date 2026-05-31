package com.example.myapplication.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Quiz;
import java.util.ArrayList;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private final Context context;
    private List<Quiz> quizList;
    private SparseArray<Integer> selectedAnswers;
    private boolean isSubmitted = false;

    public QuizAdapter(Context context) {
        this.context = context;
        this.quizList = new ArrayList<>();
        this.selectedAnswers = new SparseArray<>();
    }

    public void submitList(List<Quiz> quizzes) {
        this.quizList = quizzes != null ? quizzes : new ArrayList<>();
        this.selectedAnswers.clear();
        this.isSubmitted = false;
        notifyDataSetChanged();
    }

    public void submitQuiz() {
        this.isSubmitted = true;
        notifyDataSetChanged();
    }

    public int getCorrectAnswersCount() {
        int correctCount = 0;
        for (int i = 0; i < quizList.size(); i++) {
            Quiz quiz = quizList.get(i);
            int selectedIndex = selectedAnswers.get(i, -1);
            int correctIndex = -1;
            if (quiz.getCorrectAnswer() != null && !quiz.getCorrectAnswer().isEmpty()) {
                correctIndex = quiz.getCorrectAnswer().toUpperCase().charAt(0) - 'A';
            }
            if (selectedIndex != -1 && selectedIndex == correctIndex) {
                correctCount++;
            }
        }
        return correctCount;
    }

    public int getTotalQuestionsCount() {
        return quizList.size();
    }

    public void resetQuiz() {
        this.isSubmitted = false;
        this.selectedAnswers.clear();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_question, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.tvQuestion.setText(context.getString(R.string.quiz_question_number_format, position + 1, quiz.getQuestion()));

        holder.rgOptions.removeAllViews();
        holder.rgOptions.setOnCheckedChangeListener(null);

        List<String> options = new ArrayList<>();
        if (quiz.getOptions() instanceof List) {
            options = (List<String>) quiz.getOptions();
        }

        int correctIndex = -1;
        if (quiz.getCorrectAnswer() != null && !quiz.getCorrectAnswer().isEmpty()) {
            correctIndex = quiz.getCorrectAnswer().toUpperCase().charAt(0) - 'A';
        }

        for (int i = 0; i < options.size(); i++) {
            RadioButton rb = new RadioButton(context);
            rb.setId(View.generateViewId());

            char letter = (char) ('A' + i);
            rb.setText(letter + ". " + options.get(i));
            rb.setTextColor(context.getResources().getColor(R.color.text_primary));
            rb.setPadding(0, 16, 0, 16);

            rb.setEnabled(!isSubmitted);

            if (selectedAnswers.get(position, -1) == i) {
                rb.setChecked(true);
            }

            if (isSubmitted) {
                if (i == correctIndex) {
                    rb.setTextColor(Color.parseColor("#4CAF50")); // Green for correct
                    rb.setTypeface(null, android.graphics.Typeface.BOLD);
                } else if (selectedAnswers.get(position, -1) == i) {
                    rb.setTextColor(Color.parseColor("#F44336")); // Red for incorrect selection
                }
            }

            holder.rgOptions.addView(rb);
        }

        int finalPosition = position;
        holder.rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isSubmitted) {
                int checkedIndex = group.indexOfChild(group.findViewById(checkedId));
                selectedAnswers.put(finalPosition, checkedIndex);
            }
        });

        if (isSubmitted) {
            holder.tvFeedback.setVisibility(View.VISIBLE);
            int selectedIndex = selectedAnswers.get(position, -1);
            if (selectedIndex == -1) {
                holder.tvFeedback.setText(R.string.not_answered);
                holder.tvFeedback.setTextColor(Color.parseColor("#FF9800"));
            } else if (selectedIndex == correctIndex) {
                holder.tvFeedback.setText(R.string.correct);
                holder.tvFeedback.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                holder.tvFeedback.setText(R.string.incorrect);
                holder.tvFeedback.setTextColor(Color.parseColor("#F44336"));
            }
        } else {
            holder.tvFeedback.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvFeedback;
        RadioGroup rgOptions;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvFeedback = itemView.findViewById(R.id.tvFeedback);
            rgOptions = itemView.findViewById(R.id.rgOptions);
        }
    }
}
