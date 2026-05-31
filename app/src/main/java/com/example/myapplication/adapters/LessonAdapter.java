package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Lesson;

import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<Lesson> lessonList;
    private final OnLessonClickListener listener;

    public LessonAdapter(List<Lesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instructor_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.tvLessonNumber.setText(buildLessonTitle(holder.itemView.getContext(), lesson, position));
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        renderSingleFile(holder.llVideoFiles, lesson.getLocalVideoName(), holder.itemView.getContext().getString(R.string.no_file_selected), () -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onRemoveVideo(adapterPosition);
            }
        });
        renderSingleFile(holder.llThumbnailFiles, lesson.getLocalThumbnailName(), holder.itemView.getContext().getString(R.string.no_file_selected), () -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onRemoveThumbnail(adapterPosition);
            }
        });
        renderFiles(holder.llFilesList, lesson.getLocalFileNames(), holder);

        holder.btnUploadVideo.setText(lesson.getLocalVideoName() == null ? R.string.upload : R.string.change);
        holder.btnUploadThumbnail.setText(lesson.getLocalThumbnailName() == null ? R.string.upload : R.string.change);

        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteLesson(adapterPosition);
            }
        });
        holder.btnUploadVideo.setOnClickListener(v -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPickVideo(adapterPosition);
            }
        });
        holder.btnUploadThumbnail.setOnClickListener(v -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPickThumbnail(adapterPosition);
            }
        });
        holder.btnEditFiles.setOnClickListener(v -> {
            int adapterPosition = resolveAdapterPosition(holder, position);
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onPickFiles(adapterPosition);
            }
        });

        holder.btnAddQuiz.setOnClickListener(v -> {
            View quizView = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.item_intructor_quizz_question, holder.llQuizContainer, false);

            setupQuizView(quizView, holder.llQuizContainer);
            holder.llQuizContainer.addView(quizView);
            refreshQuizIndexes(holder.llQuizContainer);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    private void renderSingleFile(LinearLayout container, String fileName, String placeholder, RemoveAction removeAction) {
        container.removeAllViews();

        if (fileName == null || fileName.isEmpty()) {
            container.addView(createPlaceholder(container.getContext(), placeholder));
            return;
        }

        container.addView(createFileRow(container.getContext(), fileName, removeAction));
    }

    private void renderFiles(LinearLayout container, List<String> fileNames, LessonViewHolder holder) {
        container.removeAllViews();

        if (fileNames == null || fileNames.isEmpty()) {
            container.addView(createPlaceholder(container.getContext(), container.getContext().getString(R.string.no_files)));
            return;
        }

        for (int i = 0; i < fileNames.size(); i++) {
            final int filePosition = i;
            container.addView(createFileRow(container.getContext(), fileNames.get(i), () -> {
                int adapterPosition = resolveAdapterPosition(holder, holder.getLayoutPosition());
                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onRemoveFile(adapterPosition, filePosition);
                }
            }));
        }
    }

    private TextView createPlaceholder(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
        textView.setTextSize(12);
        textView.setSingleLine(true);
        textView.setPadding(dp(context, 4), dp(context, 4), dp(context, 4), dp(context, 4));
        return textView;
    }

    private View createFileRow(Context context, String fileName, RemoveAction removeAction) {
        LinearLayout row = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(context, 4));
        row.setLayoutParams(params);
        row.setBackgroundResource(R.drawable.bg_file_chip);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(context, 8), dp(context, 5), dp(context, 4), dp(context, 5));

        TextView tvName = new TextView(context);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvName.setMaxLines(1);
        tvName.setText(fileName);
        tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        tvName.setTextSize(12);

        TextView btnRemove = new TextView(context);
        btnRemove.setLayoutParams(new LinearLayout.LayoutParams(dp(context, 28), dp(context, 28)));
        btnRemove.setGravity(android.view.Gravity.CENTER);
        btnRemove.setText("X");
        btnRemove.setTextColor(ContextCompat.getColor(context, R.color.status_error));
        btnRemove.setTextSize(12);
        btnRemove.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        btnRemove.setOnClickListener(v -> removeAction.onRemove());

        row.addView(tvName);
        row.addView(btnRemove);
        return row;
    }

    private void setupQuizView(View quizView, LinearLayout container) {
        View btnRemove = quizView.findViewById(R.id.btnRemoveQuiz);
        if (btnRemove != null) {
            btnRemove.setOnClickListener(v -> {
                container.removeView(quizView);
                refreshQuizIndexes(container);
            });
        }

        RadioButton rbA = quizView.findViewById(R.id.rbA);
        RadioButton rbB = quizView.findViewById(R.id.rbB);
        RadioButton rbC = quizView.findViewById(R.id.rbC);
        RadioButton rbD = quizView.findViewById(R.id.rbD);
        View.OnClickListener radioClickListener = clicked -> {
            if (rbA != null) rbA.setChecked(clicked == rbA);
            if (rbB != null) rbB.setChecked(clicked == rbB);
            if (rbC != null) rbC.setChecked(clicked == rbC);
            if (rbD != null) rbD.setChecked(clicked == rbD);
        };
        if (rbA != null) rbA.setOnClickListener(radioClickListener);
        if (rbB != null) rbB.setOnClickListener(radioClickListener);
        if (rbC != null) rbC.setOnClickListener(radioClickListener);
        if (rbD != null) rbD.setOnClickListener(radioClickListener);
    }

    private void refreshQuizIndexes(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView tvQuizIndex = container.getChildAt(i).findViewById(R.id.tvQuizIndex);
            if (tvQuizIndex != null) {
                tvQuizIndex.setText(container.getContext().getString(R.string.question_count_format, i + 1));
            }
        }
    }

    private int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private int resolveAdapterPosition(LessonViewHolder holder, int fallbackPosition) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION) {
            return adapterPosition;
        }
        if (fallbackPosition >= 0 && fallbackPosition < lessonList.size()) {
            return fallbackPosition;
        }
        return RecyclerView.NO_POSITION;
    }

    private String buildLessonTitle(Context context, Lesson lesson, int position) {
        int lessonNumber = position + 1;
        String baseTitle = context.getString(R.string.default_lesson_title, lessonNumber);
        String englishBaseTitle = "Lesson " + lessonNumber;
        if (lesson == null || lesson.getTitle() == null) {
            return baseTitle;
        }

        String title = lesson.getTitle().trim();
        if (title.isEmpty()) {
            return baseTitle;
        }

        String compactTitle = title.replaceAll("\\s+", "");
        if (compactTitle.equalsIgnoreCase(baseTitle.replaceAll("\\s+", ""))
                || compactTitle.equalsIgnoreCase("Lesson" + lessonNumber)) {
            return baseTitle;
        }

        String strippedTitle = stripGeneratedLessonPrefix(title, baseTitle);
        if (strippedTitle == null) {
            strippedTitle = stripGeneratedLessonPrefix(title, englishBaseTitle);
        }
        if (strippedTitle != null) {
            if (strippedTitle.isEmpty()
                    || strippedTitle.replaceAll("\\s+", "").equalsIgnoreCase(baseTitle.replaceAll("\\s+", ""))
                    || strippedTitle.replaceAll("\\s+", "").equalsIgnoreCase("Lesson" + lessonNumber)) {
                return baseTitle;
            }
            return baseTitle + " - " + strippedTitle;
        }

        return baseTitle + " - " + title;
    }

    private String stripGeneratedLessonPrefix(String title, String baseTitle) {
        String lowerTitle = title.toLowerCase(Locale.US);
        String lowerBaseTitle = baseTitle.toLowerCase(Locale.US);
        if (lowerTitle.startsWith(lowerBaseTitle) && hasLessonPrefixBoundary(title, baseTitle.length())) {
            return title.substring(baseTitle.length()).replaceFirst("^[\\s:.-]+", "").trim();
        }

        return null;
    }

    private boolean hasLessonPrefixBoundary(String text, int prefixLength) {
        if (text.length() == prefixLength) {
            return true;
        }

        char boundary = text.charAt(prefixLength);
        return Character.isWhitespace(boundary) || boundary == ':' || boundary == '-' || boundary == '.';
    }

    private interface RemoveAction {
        void onRemove();
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonNumber;
        TextView btnDelete;
        LinearLayout llVideoFiles;
        LinearLayout llThumbnailFiles;
        LinearLayout llFilesList;
        LinearLayout llQuizContainer;
        Button btnUploadVideo;
        Button btnUploadThumbnail;
        Button btnEditFiles;
        Button btnAddQuiz;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            setIsRecyclable(false);
            tvLessonNumber = itemView.findViewById(R.id.tvLessonNumber);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            llVideoFiles = itemView.findViewById(R.id.llVideoFiles);
            llThumbnailFiles = itemView.findViewById(R.id.llThumbnailFiles);
            llFilesList = itemView.findViewById(R.id.llFilesList);
            llQuizContainer = itemView.findViewById(R.id.llQuizContainer);
            btnUploadVideo = itemView.findViewById(R.id.btnUploadVideo);
            btnUploadThumbnail = itemView.findViewById(R.id.btnUploadThumbnail);
            btnEditFiles = itemView.findViewById(R.id.btnEditFiles);
            btnAddQuiz = itemView.findViewById(R.id.btnAddQuiz);
        }
    }

    public interface OnLessonClickListener {
        default void onLessonClick(Lesson lesson) {
        }

        default void onDeleteLesson(int position) {
        }

        default void onPickVideo(int position) {
        }

        default void onPickThumbnail(int position) {
        }

        default void onPickFiles(int position) {
        }

        default void onRemoveVideo(int position) {
        }

        default void onRemoveThumbnail(int position) {
        }

        default void onRemoveFile(int lessonPosition, int filePosition) {
        }
    }
}
