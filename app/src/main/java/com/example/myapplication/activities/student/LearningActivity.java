package com.example.myapplication.activities.student;

import static com.google.android.material.internal.ViewUtils.showKeyboard;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CommentRepository;
import com.example.myapplication.data.repository.LessonProgressRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Comment;
import com.example.myapplication.models.LearningDataWrapper.LearningChapter;
import com.example.myapplication.models.LearningDataWrapper.LearningLesson;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.LessonProgress;
import com.example.myapplication.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

public class LearningActivity extends AppCompatActivity implements LearningChapterAdapter.OnLessonClickListener, DiscussionAdapter.OnReplyClickListener {

    private VideoView vvCourse;
    private RecyclerView rvContent;
    private ImageView btnBack;
    private TextView tvCourseTitle;
    private Button btnListLess, btnDiscuss;
    private LinearLayout layoutCommentInput;
    private EditText etCommentInput;
    private ImageView btnSendComment;
    private Button btnAddDiscussion;

    private LearningChapterAdapter chapterAdapter;
    private DiscussionAdapter discussionAdapter;
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
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        vvCourse = findViewById(R.id.vvCourse);
        rvContent = findViewById(R.id.rvContent);
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        btnListLess = findViewById(R.id.btnListLess);
        btnDiscuss = findViewById(R.id.btnDiscuss);
        btnAddDiscussion = findViewById(R.id.btnAddDiscussion);
        layoutCommentInput = findViewById(R.id.layoutCommentInput);
        etCommentInput = findViewById(R.id.etCommentInput);
        btnSendComment = findViewById(R.id.btnSendComment);

        btnBack.setOnClickListener(v -> finish());

        rvContent.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new LearningChapterAdapter(this, this);
        discussionAdapter = new DiscussionAdapter(this, this);
        rvContent.setAdapter(chapterAdapter);

        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(vvCourse);
        vvCourse.setMediaController(mediaController);

        vvCourse.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(LearningActivity.this, "Video not found or network error", Toast.LENGTH_SHORT).show();
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
        updateTabUI(true);

        btnListLess.setOnClickListener(v -> updateTabUI(true));

        btnDiscuss.setOnClickListener(v -> {
            if (currentPlayingLesson == null) {
                Toast.makeText(this, "Please select a lesson first", Toast.LENGTH_SHORT).show();
                return;
            }
            updateTabUI(false);
            loadComments();
        });

        btnAddDiscussion.setOnClickListener(v -> {
            layoutCommentInput.setVisibility(View.VISIBLE);
            replyingParentId = null;
            etCommentInput.setHint("Add a discussion...");
            etCommentInput.requestFocus();
            showKeyboard(etCommentInput);
        });

        btnSendComment.setOnClickListener(v -> postComment());
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
            Toast.makeText(this, "Missing required information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chapterRepository = new ChapterRepository(this);
        lessonRepository = new LessonRepository(this);
        progressRepository = new LessonProgressRepository(this);
        commentRepository = new CommentRepository(this);

        loadLearningData();
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
                Toast.makeText(LearningActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LearningActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
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
        etCommentInput.setHint("Reply to " + parentComment.getUsers().getFullName() + "...");
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
                public void onError(String message) {
                }
            });
        } else {
            progressRepository.update(progress.getId(), progress, new LessonProgressRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }

                @Override
                public void onError(String message) {
                }
            });
        }
    }

    private void loadLearningData() {
        chapterList = new ArrayList<>();

        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    Toast.makeText(LearningActivity.this, "Course has no content", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LearningActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
        etCommentInput.setHint("Add a comment...");

        String videoUrl = learningLesson.getLesson().getVideoUrl();

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            vvCourse.stopPlayback();
            Toast.makeText(this, "No video available for this lesson", Toast.LENGTH_SHORT).show();
        } else {
            vvCourse.setVideoURI(Uri.parse(videoUrl));
            vvCourse.setOnPreparedListener(mp -> {
                if (lastSavedSeconds > 0) {
                    vvCourse.seekTo(lastSavedSeconds * 1000);
                }
                vvCourse.start();
            });
        }

        if (rvContent.getAdapter() instanceof DiscussionAdapter) {
            loadComments();
        }
    }

    private void updateTabUI(boolean isListLessonActive) {
        if (isListLessonActive) {
            btnListLess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
            btnDiscuss.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));

            rvContent.setAdapter(chapterAdapter);
            layoutCommentInput.setVisibility(View.GONE);
            btnAddDiscussion.setVisibility(View.GONE);
        } else {
            btnListLess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bg_secondary));
            btnDiscuss.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));

            rvContent.setAdapter(discussionAdapter);
            layoutCommentInput.setVisibility(View.GONE);
            btnAddDiscussion.setVisibility(View.VISIBLE);
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