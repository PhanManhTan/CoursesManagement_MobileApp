package com.example.myapplication.activities.student;

import static com.google.android.material.internal.ViewUtils.showKeyboard;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.DiscussionAdapter;
import com.example.myapplication.adapters.LearningChapterAdapter;
import com.example.myapplication.adapters.QuizAdapter;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CommentRepository;
import com.example.myapplication.data.repository.LessonProgressRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.data.repository.QuizRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Comment;
import com.example.myapplication.models.LearningDataWrapper.LearningChapter;
import com.example.myapplication.models.LearningDataWrapper.LearningLesson;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.LessonProgress;
import com.example.myapplication.models.Quiz;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LearningActivity extends AppCompatActivity implements LearningChapterAdapter.OnLessonClickListener, DiscussionAdapter.OnReplyClickListener {

    private enum TabState { LESSONS, DISCUSSIONS, QUIZZES, FILES }

    private VideoView vvCourse;
    private RecyclerView rvContent;
    private ImageView btnBack;
    private TextView tvCourseTitle;
    private Button btnListLess, btnDiscuss, btnQuizz, btnFiles, btnAddDiscussion, btnDownloadFile;

    private LinearLayout layoutCommentInput, layoutQuizControls, layoutFiles;
    private EditText etCommentInput;
    private ImageView btnSendComment;
    private Button btnSubmitQuiz, btnResetQuiz;
    private TextView tvFileName;

    private LearningChapterAdapter chapterAdapter;
    private DiscussionAdapter discussionAdapter;
    private QuizAdapter quizAdapter;
    private List<LearningChapter> chapterList;
    private LearningLesson currentPlayingLesson;

    private Handler progressHandler;
    private Runnable progressRunnable;

    private String courseId;
    private String userId;
    private int lastSavedSeconds = 0;
    private boolean hasPendingSave = false;
    private String replyingParentId = null;

    private ChapterRepository chapterRepository;
    private LessonRepository lessonRepository;
    private LessonProgressRepository progressRepository;
    private CommentRepository commentRepository;
    private QuizRepository quizRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        vvCourse = findViewById(R.id.vvCourse);
        rvContent = findViewById(R.id.rvContent);
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        btnListLess = findViewById(R.id.btnListLess);
        btnDiscuss = findViewById(R.id.btnDiscuss);
        btnQuizz = findViewById(R.id.btnQuizz);
        btnFiles = findViewById(R.id.btnFiles);
        btnAddDiscussion = findViewById(R.id.btnAddDiscussion);

        layoutCommentInput = findViewById(R.id.layoutCommentInput);
        layoutQuizControls = findViewById(R.id.layoutQuizControls);
        layoutFiles = findViewById(R.id.layoutFiles);

        etCommentInput = findViewById(R.id.etCommentInput);
        btnSendComment = findViewById(R.id.btnSendComment);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);
        btnResetQuiz = findViewById(R.id.btnResetQuiz);
        btnDownloadFile = findViewById(R.id.btnDownloadFile);
        tvFileName = findViewById(R.id.tvFileName);

        btnBack.setOnClickListener(v -> finish());

        rvContent.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new LearningChapterAdapter(this, this);
        discussionAdapter = new DiscussionAdapter(this, this);
        quizAdapter = new QuizAdapter(this);
        rvContent.setAdapter(chapterAdapter);

        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(vvCourse);
        vvCourse.setMediaController(mediaController);

        vvCourse.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(LearningActivity.this, R.string.video_not_found_network, Toast.LENGTH_SHORT).show();
            return true;
        });

        progressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                trackVideoProgress();
                progressHandler.postDelayed(this, 1000);
            }
        };

        setupTabButtons();
        initData();
    }

    private void setupTabButtons() {
        updateTabUI(TabState.LESSONS);

        btnListLess.setOnClickListener(v -> updateTabUI(TabState.LESSONS));

        btnDiscuss.setOnClickListener(v -> {
            if (currentPlayingLesson == null) {
                Toast.makeText(this, R.string.select_lesson_first, Toast.LENGTH_SHORT).show();
                return;
            }
            updateTabUI(TabState.DISCUSSIONS);
            loadComments();
        });

        btnQuizz.setOnClickListener(v -> {
            if (currentPlayingLesson == null) {
                Toast.makeText(this, R.string.select_lesson_first, Toast.LENGTH_SHORT).show();
                return;
            }
            updateTabUI(TabState.QUIZZES);
            loadQuizzes();
        });

        btnFiles.setOnClickListener(v -> {
            if (currentPlayingLesson == null) {
                Toast.makeText(this, R.string.select_lesson_first, Toast.LENGTH_SHORT).show();
                return;
            }
            updateTabUI(TabState.FILES);
            loadFileUi();
        });

        btnAddDiscussion.setOnClickListener(v -> {
            layoutCommentInput.setVisibility(View.VISIBLE);
            replyingParentId = null;
            etCommentInput.setHint(R.string.add_discussion_hint);
            etCommentInput.requestFocus();
            showKeyboard(etCommentInput);
        });

        btnSendComment.setOnClickListener(v -> postComment());
        btnSubmitQuiz.setOnClickListener(v -> {
            quizAdapter.submitQuiz();
            int correct = quizAdapter.getCorrectAnswersCount();
            int total = quizAdapter.getTotalQuestionsCount();
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.quiz)
                .setMessage("You answered " + correct + " out of " + total + " questions correctly!")
                .setPositiveButton(android.R.string.ok, null)
                .show();
        });
        btnResetQuiz.setOnClickListener(v -> quizAdapter.resetQuiz());
    }


    private void initData() {
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (getIntent() != null) {
            courseId = getIntent().getStringExtra("COURSE_ID");
            String courseTitle = getIntent().getStringExtra("COURSE_TITLE");
            if (courseTitle != null) {
                tvCourseTitle.setText(courseTitle);
            }
        }

        if (courseId == null || userId == null) {
            Toast.makeText(this, R.string.missing_required_information, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chapterRepository = new ChapterRepository(this);
        lessonRepository = new LessonRepository(this);
        progressRepository = new LessonProgressRepository(this);
        commentRepository = new CommentRepository(this);
        quizRepository = new QuizRepository(this);

        loadLearningData();
    }

    private void loadFileUi() {
        if (currentPlayingLesson == null || currentPlayingLesson.getLesson() == null) return;

        String docUrl = currentPlayingLesson.getLesson().getDocumentUrl();

        if (docUrl != null && !docUrl.trim().isEmpty() && !docUrl.trim().equalsIgnoreCase("null")) {
            tvFileName.setText(R.string.document_attached_lesson);
            btnDownloadFile.setVisibility(View.VISIBLE);
            btnDownloadFile.setOnClickListener(v -> downloadFile(docUrl));
        } else {
            tvFileName.setText(R.string.no_files_for_lesson);
            btnDownloadFile.setVisibility(View.GONE);
        }
    }

    private void downloadFile(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = "lesson_document.pdf";
            }
            request.setTitle(getString(R.string.download_course_file));
            request.setDescription(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.download_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadQuizzes() {
        if (currentPlayingLesson == null) return;
        String lessonId = currentPlayingLesson.getLesson().getId();
        quizRepository.getByLessonId(lessonId, new QuizRepository.RepositoryCallback<List<Quiz>>() {
            @Override
            public void onSuccess(List<Quiz> quizzes) {
                if (quizzes != null && !quizzes.isEmpty()) {
                    quizAdapter.submitList(quizzes);
                    layoutQuizControls.setVisibility(View.VISIBLE);
                } else {
                    quizAdapter.submitList(new ArrayList<>());
                    layoutQuizControls.setVisibility(View.GONE);
                    Toast.makeText(LearningActivity.this, R.string.no_quizzes_for_lesson, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LearningActivity.this, getString(R.string.failed_load_quizzes, message), Toast.LENGTH_SHORT).show();
                layoutQuizControls.setVisibility(View.GONE);
            }
        });
    }

    private void loadComments() {
        if (currentPlayingLesson == null) return;
        String lessonId = currentPlayingLesson.getLesson().getId();
        commentRepository.getByLessonId(lessonId, new CommentRepository.RepositoryCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (comments != null) {
                    discussionAdapter.submitList(comments);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LearningActivity.this, R.string.failed_load_comments, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String content = etCommentInput.getText().toString().trim();
        if (content.isEmpty()) return;

        Comment comment = new Comment();
        comment.setLessonId(currentPlayingLesson.getLesson().getId());
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(replyingParentId);

        commentRepository.insert(comment, new CommentRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                etCommentInput.setText("");
                layoutCommentInput.setVisibility(View.GONE);
                hideKeyboard();
                replyingParentId = null;
                loadComments();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LearningActivity.this, R.string.failed_post_comment, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                layoutCommentInput.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                    layoutCommentInput.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onReplyClick(Comment parentComment) {
        layoutCommentInput.setVisibility(View.VISIBLE);
        replyingParentId = parentComment.getId();
        etCommentInput.setHint(getString(R.string.reply_to_format, parentComment.getUsers().getFullName()));
        etCommentInput.requestFocus();
        showKeyboard(etCommentInput);
    }

    private void trackVideoProgress() {
        if (vvCourse.isPlaying() && currentPlayingLesson != null) {
            int currentPositionSeconds = vvCourse.getCurrentPosition() / 1000;
            LessonProgress progress = currentPlayingLesson.getProgress();

            if (progress != null && currentPositionSeconds > progress.getWatchTimeSeconds()) {
                progress.setWatchTimeSeconds(currentPositionSeconds);
                hasPendingSave = true;

                int duration = currentPlayingLesson.getLesson().getDurationSeconds();

                if (duration > 0 && ((double) currentPositionSeconds / duration) >= 0.8) {
                    if (!progress.isCompleted()) {
                        progress.setCompleted(true);
                        saveProgressToDatabase(progress);
                        hasPendingSave = false;
                        chapterAdapter.setData(chapterList);
                    }
                }
            }
        }
    }

    private void saveProgressToDatabase(LessonProgress progress) {
        if (progress.getId() == null) {
            progressRepository.insertAndReturn(progress, new LessonProgressRepository.RepositoryCallback<LessonProgress>() {
                @Override
                public void onSuccess(LessonProgress data) {
                    if (data != null && data.getId() != null) {
                        progress.setId(data.getId());
                    }
                }
                @Override
                public void onError(String message) {}
            });
        } else {
            progressRepository.update(progress.getId(), progress, new LessonProgressRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {}
                @Override
                public void onError(String message) {}
            });
        }
    }

    private void loadLearningData() {
        chapterList = new ArrayList<>();

        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    Toast.makeText(LearningActivity.this, R.string.course_has_no_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.sort(chapters, (c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()));
                AtomicInteger pendingChapters = new AtomicInteger(chapters.size());
                for (Chapter chapter : chapters) {
                    loadLessonsForChapter(chapter, pendingChapters);
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(LearningActivity.this, getString(R.string.error_with_message, message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLessonsForChapter(Chapter chapter, AtomicInteger pendingChapters) {
        lessonRepository.getByChapterId(chapter.getId(), new LessonRepository.RepositoryCallback<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> lessons) {
                if (lessons == null) lessons = new ArrayList<>();
                Collections.sort(lessons, (l1, l2) -> Integer.compare(l1.getOrderIndex(), l2.getOrderIndex()));

                List<LearningLesson> learningLessons = new ArrayList<>();
                if (lessons.isEmpty()) {
                    addChapterToAdapter(chapter, learningLessons, pendingChapters);
                    return;
                }
                AtomicInteger pendingLessons = new AtomicInteger(lessons.size());
                for (Lesson lesson : lessons) {
                    loadProgressForLesson(lesson, learningLessons, chapter, pendingLessons, pendingChapters);
                }
            }
            @Override
            public void onError(String message) {
                addChapterToAdapter(chapter, new ArrayList<>(), pendingChapters);
            }
        });
    }

    private void loadProgressForLesson(Lesson lesson, List<LearningLesson> learningLessons, Chapter chapter, AtomicInteger pendingLessons, AtomicInteger pendingChapters) {
        progressRepository.getByUserIdAndLessonId(userId, lesson.getId(), new LessonProgressRepository.RepositoryCallback<LessonProgress>() {
            @Override
            public void onSuccess(LessonProgress progress) {
                processLessonData(lesson, progress, learningLessons, chapter, pendingLessons, pendingChapters);
            }
            @Override
            public void onError(String message) {
                LessonProgress emptyProgress = new LessonProgress();
                emptyProgress.setLessonId(lesson.getId());
                emptyProgress.setUserId(userId);
                emptyProgress.setWatchTimeSeconds(0);
                emptyProgress.setCompleted(false);
                processLessonData(lesson, emptyProgress, learningLessons, chapter, pendingLessons, pendingChapters);
            }
        });
    }

    private void processLessonData(Lesson lesson, LessonProgress progress, List<LearningLesson> learningLessons, Chapter chapter, AtomicInteger pendingLessons, AtomicInteger pendingChapters) {
        learningLessons.add(new LearningLesson(lesson, progress, true));
        if (pendingLessons.decrementAndGet() == 0) {
            Collections.sort(learningLessons, (l1, l2) -> Integer.compare(l1.getLesson().getOrderIndex(), l2.getLesson().getOrderIndex()));
            addChapterToAdapter(chapter, learningLessons, pendingChapters);
        }
    }

    private void addChapterToAdapter(Chapter chapter, List<LearningLesson> learningLessons, AtomicInteger pendingChapters) {
        chapterList.add(new LearningChapter(chapter, learningLessons));
        if (pendingChapters.decrementAndGet() == 0) {
            Collections.sort(chapterList, (c1, c2) -> Integer.compare(c1.getChapter().getOrderIndex(), c2.getChapter().getOrderIndex()));
            runOnUiThread(() -> chapterAdapter.setData(chapterList));
        }
    }

    @Override
    public void onLessonValueClick(LearningLesson learningLesson) {
        if (hasPendingSave && currentPlayingLesson != null && currentPlayingLesson.getProgress() != null) {
            saveProgressToDatabase(currentPlayingLesson.getProgress());
            hasPendingSave = false;
        }

        currentPlayingLesson = learningLesson;
        lastSavedSeconds = currentPlayingLesson.getProgress() != null ? currentPlayingLesson.getProgress().getWatchTimeSeconds() : 0;

        replyingParentId = null;
        etCommentInput.setHint(R.string.add_comment_hint);

        String videoUrl = learningLesson.getLesson().getVideoUrl();

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            vvCourse.stopPlayback();
            Toast.makeText(this, R.string.no_video_for_lesson, Toast.LENGTH_SHORT).show();
        } else {
            vvCourse.setVideoURI(Uri.parse(videoUrl));
            vvCourse.setOnPreparedListener(mp -> {
                if (lastSavedSeconds > 0) {
                    vvCourse.seekTo(lastSavedSeconds * 1000);
                }
                vvCourse.start();
            });
        }

        if (rvContent.getVisibility() == View.VISIBLE && rvContent.getAdapter() instanceof DiscussionAdapter) {
            loadComments();
        } else if (rvContent.getVisibility() == View.VISIBLE && rvContent.getAdapter() instanceof QuizAdapter) {
            loadQuizzes();
        } else if (layoutFiles.getVisibility() == View.VISIBLE) {
            loadFileUi();
        }
    }

    private void updateTabUI(TabState state) {
        btnListLess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));
        btnDiscuss.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));
        btnQuizz.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));
        btnFiles.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));

        layoutCommentInput.setVisibility(View.GONE);
        layoutQuizControls.setVisibility(View.GONE);
        btnAddDiscussion.setVisibility(View.GONE);
        rvContent.setVisibility(View.GONE);
        layoutFiles.setVisibility(View.GONE);

        switch (state) {
            case LESSONS:
                btnListLess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
                rvContent.setVisibility(View.VISIBLE);
                rvContent.setAdapter(chapterAdapter);
                break;
            case DISCUSSIONS:
                btnDiscuss.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
                rvContent.setVisibility(View.VISIBLE);
                rvContent.setAdapter(discussionAdapter);
                btnAddDiscussion.setVisibility(View.VISIBLE);
                break;
            case QUIZZES:
                btnQuizz.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
                rvContent.setVisibility(View.VISIBLE);
                rvContent.setAdapter(quizAdapter);
                break;
            case FILES:
                btnFiles.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
                layoutFiles.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressHandler.post(progressRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(progressRunnable);

        if (hasPendingSave && currentPlayingLesson != null && currentPlayingLesson.getProgress() != null) {
            saveProgressToDatabase(currentPlayingLesson.getProgress());
            hasPendingSave = false;
        }
    }
}
