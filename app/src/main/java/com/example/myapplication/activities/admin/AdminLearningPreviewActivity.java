package com.example.myapplication.activities.admin;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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

public class AdminLearningPreviewActivity extends AppCompatActivity implements LearningChapterAdapter.OnLessonClickListener, DiscussionAdapter.OnReplyClickListener {
    public static final String EXTRA_COURSE_ID = "COURSE_ID";
    public static final String EXTRA_COURSE_TITLE = "COURSE_TITLE";

    private enum TabState { LESSONS, DISCUSSIONS, QUIZZES, FILES }

    private VideoView vvCourse;
    private RecyclerView rvContent;
    private ImageView btnBack;
    private TextView tvCourseTitle;
    private Button btnListLess, btnDiscuss, btnQuizz, btnFiles, btnAddDiscussion, btnDownloadFile;
    private LinearLayout layoutCommentInput, layoutQuizControls, layoutFiles;
    private EditText etCommentInput;
    private TextView tvFileName;

    private LearningChapterAdapter chapterAdapter;
    private DiscussionAdapter discussionAdapter;
    private QuizAdapter quizAdapter;
    private final List<LearningChapter> chapterList = new ArrayList<>();
    private LearningLesson currentPlayingLesson;

    private String courseId;
    private String adminUserId;

    private ChapterRepository chapterRepository;
    private LessonRepository lessonRepository;
    private CommentRepository commentRepository;
    private QuizRepository quizRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        sessionManager = new SessionManager(this);
        if (!"admin".equalsIgnoreCase(sessionManager.getRole())) {
            Toast.makeText(this, R.string.course_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupAdapters();
        setupMediaController();
        setupTabButtons();
        initData();
    }

    private void bindViews() {
        vvCourse = findViewById(R.id.vvCourse);
        rvContent = findViewById(R.id.rvContent);
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        btnListLess = findViewById(R.id.btnListLess);
        btnDiscuss = findViewById(R.id.btnDiscuss);
        btnQuizz = findViewById(R.id.btnQuizz);
        btnFiles = findViewById(R.id.btnFiles);
        btnAddDiscussion = findViewById(R.id.btnAddDiscussion);
        btnDownloadFile = findViewById(R.id.btnDownloadFile);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);
        btnResetQuiz = findViewById(R.id.btnResetQuiz);
        layoutCommentInput = findViewById(R.id.layoutCommentInput);
        layoutQuizControls = findViewById(R.id.layoutQuizControls);
        layoutFiles = findViewById(R.id.layoutFiles);
        etCommentInput = findViewById(R.id.etCommentInput);
        tvFileName = findViewById(R.id.tvFileName);

        btnBack.setOnClickListener(v -> finish());
        btnAddDiscussion.setVisibility(View.GONE);
        layoutCommentInput.setVisibility(View.GONE);
    }

    private void setupAdapters() {
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new LearningChapterAdapter(this, this);
        chapterAdapter.setShowCompletionStatus(false);
        discussionAdapter = new DiscussionAdapter(this, this);
        quizAdapter = new QuizAdapter(this);
        rvContent.setAdapter(chapterAdapter);
    }

    private void setupMediaController() {
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(vvCourse);
        vvCourse.setMediaController(mediaController);
        vvCourse.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(AdminLearningPreviewActivity.this, R.string.video_not_found_network, Toast.LENGTH_SHORT).show();
            return true;
        });
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

    private Button btnSubmitQuiz;
    private Button btnResetQuiz;

    private void initData() {
        adminUserId = sessionManager.getUserId();
        courseId = getIntent() != null ? getIntent().getStringExtra(EXTRA_COURSE_ID) : null;
        String title = getIntent() != null ? getIntent().getStringExtra(EXTRA_COURSE_TITLE) : null;
        if (title != null && !title.trim().isEmpty()) {
            tvCourseTitle.setText(title);
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            Toast.makeText(this, R.string.course_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chapterRepository = new ChapterRepository(this);
        lessonRepository = new LessonRepository(this);
        commentRepository = new CommentRepository(this);
        quizRepository = new QuizRepository(this);
        loadLearningData();
    }

    private void loadLearningData() {
        chapterList.clear();
        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    Toast.makeText(AdminLearningPreviewActivity.this, R.string.course_has_no_content, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AdminLearningPreviewActivity.this, getString(R.string.error_with_message, message), Toast.LENGTH_SHORT).show();
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
                for (Lesson lesson : lessons) {
                    learningLessons.add(new LearningLesson(lesson, createUnlockedPreviewProgress(lesson), false));
                }
                addChapterToAdapter(chapter, learningLessons, pendingChapters);
            }

            @Override
            public void onError(String message) {
                addChapterToAdapter(chapter, new ArrayList<>(), pendingChapters);
            }
        });
    }

    private LessonProgress createUnlockedPreviewProgress(Lesson lesson) {
        LessonProgress progress = new LessonProgress();
        progress.setLessonId(lesson != null ? lesson.getId() : null);
        progress.setUserId(adminUserId);
        progress.setWatchTimeSeconds(lesson != null ? Math.max(lesson.getDurationSeconds(), 0) : 0);
        progress.setCompleted(true);
        return progress;
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
        currentPlayingLesson = learningLesson;
        String videoUrl = learningLesson.getLesson().getVideoUrl();
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            vvCourse.stopPlayback();
            Toast.makeText(this, R.string.no_video_for_lesson, Toast.LENGTH_SHORT).show();
        } else {
            vvCourse.setVideoURI(Uri.parse(videoUrl));
            vvCourse.setOnPreparedListener(mp -> vvCourse.start());
        }

        if (rvContent.getVisibility() == View.VISIBLE && rvContent.getAdapter() instanceof DiscussionAdapter) {
            loadComments();
        } else if (rvContent.getVisibility() == View.VISIBLE && rvContent.getAdapter() instanceof QuizAdapter) {
            loadQuizzes();
        } else if (layoutFiles.getVisibility() == View.VISIBLE) {
            loadFileUi();
        }
    }

    private void loadComments() {
        if (currentPlayingLesson == null) return;
        commentRepository.getByLessonId(currentPlayingLesson.getLesson().getId(), new CommentRepository.RepositoryCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                discussionAdapter.submitList(comments != null ? comments : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminLearningPreviewActivity.this, R.string.failed_load_comments, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuizzes() {
        if (currentPlayingLesson == null) return;
        quizRepository.getByLessonId(currentPlayingLesson.getLesson().getId(), new QuizRepository.RepositoryCallback<List<Quiz>>() {
            @Override
            public void onSuccess(List<Quiz> quizzes) {
                quizAdapter.submitList(quizzes != null ? quizzes : new ArrayList<>());
                layoutQuizControls.setVisibility(quizzes != null && !quizzes.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminLearningPreviewActivity.this, getString(R.string.failed_load_quizzes, message), Toast.LENGTH_SHORT).show();
                layoutQuizControls.setVisibility(View.GONE);
            }
        });
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

    @Override
    public void onReplyClick(Comment parentComment) {
        // Admin preview is read-only for discussions.
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
}
