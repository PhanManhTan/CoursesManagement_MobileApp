package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Lesson;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;

    public LessonAdapter(List<Lesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng file XML lesson bạn đã gửi
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.tvLessonNumber.setText("Bài " + (position + 1) + ": " + lesson.getTitle());

        // LOGIC CHÍNH: Mỗi lần ấn nút này, nó sẽ nạp item_intructor_quizz_question vào container
        holder.btnAddQuiz.setOnClickListener(v -> {
            View quizView = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.item_intructor_quizz_question, holder.llQuizContainer, false);

            // Xử lý nút xóa câu hỏi đó nếu cần
            View btnRemove = quizView.findViewById(R.id.btnRemoveQuiz);
            if (btnRemove != null) {
                btnRemove.setOnClickListener(v2 -> holder.llQuizContainer.removeView(quizView));
            }

            holder.llQuizContainer.addView(quizView);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonNumber;
        LinearLayout llQuizContainer; // Container bạn đã thêm vào XML lesson
        MaterialButton btnAddQuiz;    // Nút "Thêm Quiz" bạn đã thêm vào XML lesson

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            llQuizContainer = itemView.findViewById(R.id.llQuizContainer);
            btnAddQuiz = itemView.findViewById(R.id.btnAddQuiz);
        }
    }

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }
}