package com.example.myapplication.activities.student;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.LearningChapterAdapter;
import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.data.repository.LessonProgressRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.LessonProgress;
import com.example.myapplication.models.LearningDataWrapper.LearningChapter;
import com.example.myapplication.models.LearningDataWrapper.LearningLesson;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LearningActivity extends AppCompatActivity implements LearningChapterAdapter.OnLessonClickListener {

    private VideoView vvCourse;
    private RecyclerView rvContent;
    private ImageView btnBack;
    private TextView tvCourseTitle;
    private LearningChapterAdapter adapter;
    private List<LearningChapter> chapterList;
    private LearningLesson currentPlayingLesson;
    private Handler progressHandler;
    private Runnable progressRunnable;

    private String courseId;
    private String userId;
    private int lastSavedSeconds = 0;

    private ChapterRepository chapterRepository;
    private LessonRepository lessonRepository;
    private LessonProgressRepository progressRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        vvCourse = findViewById(R.id.vvCourse);
        rvContent = findViewById(R.id.rvContent);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvContent.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LearningChapterAdapter(this, this);
        rvContent.setAdapter(adapter);

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

        initData();
    }

    private void trackVideoProgress() {
        if (vvCourse.isPlaying() && currentPlayingLesson != null) {
            int currentPositionSeconds = vvCourse.getCurrentPosition() / 1000;
            LessonProgress progress = currentPlayingLesson.getProgress();

            if (progress != null && currentPositionSeconds > progress.getWatchTimeSeconds()) {
                progress.setWatchTimeSeconds(currentPositionSeconds);

                int duration = currentPlayingLesson.getLesson().getDurationSeconds();
                boolean justCompleted = false;

                if (duration > 0 && ((double) currentPositionSeconds / duration) >= 0.8) {
                    if (!progress.isCompleted()) {
                        progress.setCompleted(true);
                        justCompleted = true;
                    }
                }

                if (currentPositionSeconds - lastSavedSeconds >= 5 || justCompleted) {
                    lastSavedSeconds = currentPositionSeconds;
                    saveProgressToDatabase(progress);

                    if (justCompleted) {
                        adapter.setData(chapterList);
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

    private void initData() {
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (getIntent() != null) {
            courseId = getIntent().getStringExtra("COURSE_ID");
        }

        if (courseId == null || userId == null) {
            Toast.makeText(this, "Missing required information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chapterRepository = new ChapterRepository(this);
        lessonRepository = new LessonRepository(this);
        progressRepository = new LessonProgressRepository(this);

        loadLearningData();
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
            runOnUiThread(() -> adapter.setData(chapterList));
        }
    }

    @Override
    public void onLessonValueClick(LearningLesson learningLesson) {
        currentPlayingLesson = learningLesson;
        lastSavedSeconds = currentPlayingLesson.getProgress() != null ? currentPlayingLesson.getProgress().getWatchTimeSeconds() : 0;

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

        if (currentPlayingLesson != null && currentPlayingLesson.getProgress() != null) {
            saveProgressToDatabase(currentPlayingLesson.getProgress());
        }
    }
}