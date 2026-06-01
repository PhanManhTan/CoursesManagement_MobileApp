package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.data.repository.QuizRepository;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.data.repository.SupabaseStorageRepository;
import com.example.myapplication.models.LessonEditorData;
import com.example.myapplication.models.Quiz;
import com.example.myapplication.utils.ApiErrorFormatter;
import com.example.myapplication.utils.CourseContentNotifier;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditLessonActivity extends AppCompatActivity {
    private static final String TAG = "EditLessonActivity";
    public static final String EXTRA_LESSON_DATA = "LESSON_DATA";
    public static final String EXTRA_LESSON_SYNCED = "LESSON_SYNCED";

    private EditText etLessonTitle;
    private EditText etLessonDescription;
    private TextView tvVideoFileName;
    private TextView btnSaveLesson;
    private Button btnChooseVideo;
    private Button btnDeleteVideo;
    private Button btnAddAttachment;
    private Button btnAddQuiz;
    private LinearLayout llAttachments;
    private LinearLayout llQuizContainer;
    private SupabaseStorageRepository storageRepository;
    private LessonRepository lessonRepository;
    private QuizRepository quizRepository;
    private CourseContentNotifier courseContentNotifier;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<String[]> pickAttachmentLauncher;
    private LessonEditorData lessonData;
    private int pendingUploadCount;
    private boolean isBindingData;
    private boolean hasUnsavedChanges;
    private boolean isSaving;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        registerFilePickers();
        setContentView(R.layout.activity_edit_lesson);

        lessonData = readLessonData();
        storageRepository = new SupabaseStorageRepository(this);
        lessonRepository = new LessonRepository(this);
        quizRepository = new QuizRepository(this);
        courseContentNotifier = new CourseContentNotifier(this);

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        etLessonTitle = findViewById(R.id.etLessonTitle);
        etLessonDescription = findViewById(R.id.etLessonDescription);
        tvVideoFileName = findViewById(R.id.tvVideoFileName);
        btnSaveLesson = findViewById(R.id.btnSaveLesson);
        btnChooseVideo = findViewById(R.id.btnChooseVideo);
        btnDeleteVideo = findViewById(R.id.btnDeleteVideo);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnAddQuiz = findViewById(R.id.btnAddQuiz);
        llAttachments = findViewById(R.id.llAttachments);
        llQuizContainer = findViewById(R.id.llQuizContainer);
    }

    private void setupListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> confirmExitIfDirty(this::finish));
        }
        btnSaveLesson.setOnClickListener(v -> saveLesson());
        btnChooseVideo.setOnClickListener(v -> launchVideoPicker());
        btnDeleteVideo.setOnClickListener(v -> confirmDeleteVideo());
        btnAddAttachment.setOnClickListener(v -> launchAttachmentPicker());
        btnAddQuiz.setOnClickListener(v -> {
            addQuizView(null);
            refreshQuizIndexes();
            markDirty();
        });
        setupDirtyWatchers();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExitIfDirty(EditLessonActivity.this::finish);
            }
        });
    }

    private void bindData() {
        isBindingData = true;
        etLessonTitle.setText(valueOrEmpty(lessonData.getTitle()));
        etLessonDescription.setText(valueOrEmpty(lessonData.getDescription()));
        renderVideoName();
        renderAttachments();
        renderQuizzes();
        isBindingData = false;
    }

    private LessonEditorData readLessonData() {
        Intent intent = getIntent();
        LessonEditorData data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data = intent.getSerializableExtra(EXTRA_LESSON_DATA, LessonEditorData.class);
        } else {
            data = (LessonEditorData) intent.getSerializableExtra(EXTRA_LESSON_DATA);
        }
        return data != null ? data : new LessonEditorData();
    }

    private void registerFilePickers() {
        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        setVideoFile(uri);
                    }
                }
        );
        pickAttachmentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        addAttachments(uris);
                    }
                }
        );
    }

    private void launchVideoPicker() {
        try {
            pickVideoLauncher.launch("video/*");
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_video_picker, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchAttachmentPicker() {
        try {
            pickAttachmentLauncher.launch(new String[]{"*/*"});
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_file_picker, Toast.LENGTH_SHORT).show();
        }
    }

    private void setVideoFile(Uri uri) {
        String fileName = getDisplayName(uri);
        String localUri = uri.toString();
        int durationSeconds = readVideoDurationSeconds(uri);
        lessonData.setLocalVideoName(fileName);
        lessonData.setLocalVideoUri(localUri);
        lessonData.setVideoUrl(localUri);
        lessonData.setDurationSeconds(durationSeconds);
        renderVideoName();
        markDirty();

        beginUpload();
        storageRepository.uploadToFolder(uri, fileName, buildLessonMediaFolder("videos"), new SupabaseStorageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String url) {
                runIfActive(() -> {
                    lessonData.setLocalVideoName(fileName);
                    lessonData.setLocalVideoUri(url);
                    lessonData.setVideoUrl(url);
                    lessonData.setDurationSeconds(durationSeconds);
                    renderVideoName();
                    finishUpload();
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> {
                    lessonData.setLocalVideoUri("");
                    lessonData.setVideoUrl("");
                    lessonData.setDurationSeconds(0);
                    renderVideoName();
                    showUploadError(message);
                    finishUpload();
                });
            }
        });
    }

    private void clearVideoFile() {
        lessonData.setLocalVideoName("");
        lessonData.setLocalVideoUri("");
        lessonData.setVideoUrl("");
        lessonData.setDurationSeconds(0);
        renderVideoName();
        markDirty();
    }

    private void addAttachments(List<Uri> uris) {
        for (Uri uri : uris) {
            String fileName = getDisplayName(uri);
            String fileSize = getDisplaySize(uri);
            String localUri = uri.toString();
            int fileIndex = lessonData.getLocalFileUris().size();
            lessonData.addFile(fileName, localUri, fileSize);
            uploadAttachment(fileIndex, uri, fileName, fileSize, localUri);
        }
        updateDocumentUrlFromFiles();
        renderAttachments();
        markDirty();
    }

    private void uploadAttachment(int fileIndex, Uri uri, String fileName, String fileSize, String localUri) {
        beginUpload();
        storageRepository.uploadToFolder(uri, fileName, buildLessonMediaFolder("files"), new SupabaseStorageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String url) {
                runIfActive(() -> {
                    int currentIndex = findFileIndex(localUri);
                    if (currentIndex != -1) {
                        lessonData.setFile(currentIndex, fileName, url, fileSize);
                    }
                    updateDocumentUrlFromFiles();
                    renderAttachments();
                    finishUpload();
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> {
                    int currentIndex = findFileIndex(localUri);
                    if (currentIndex != -1) {
                        lessonData.removeFile(currentIndex);
                    }
                    updateDocumentUrlFromFiles();
                    renderAttachments();
                    markDirty();
                    showUploadError(message);
                    finishUpload();
                });
            }
        });
    }

    private void saveLesson() {
        if (isSaving) {
            return;
        }

        String title = etLessonTitle.getText().toString().trim();
        String description = etLessonDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etLessonTitle.setError(getString(R.string.lesson_title_required));
            etLessonTitle.requestFocus();
            return;
        }
        if (pendingUploadCount > 0) {
            Toast.makeText(this, R.string.wait_uploads_finish, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hasSelectedVideo()) {
            showSaveError(
                    getString(R.string.video_required),
                    getString(R.string.video_required_message)
            );
            return;
        }

        ArrayList<Quiz> quizzes = collectQuizzesFromViews();
        if (quizzes == null) {
            return;
        }

        lessonData.setTitle(title);
        lessonData.setDescription(description);
        lessonData.setQuizzes(quizzes);
        String videoUrl = hasValue(lessonData.getLocalVideoUri())
                ? lessonData.getLocalVideoUri()
                : lessonData.getVideoUrl();
        lessonData.setVideoUrl(valueOrEmpty(videoUrl));
        if (hasValue(videoUrl)) {
            if (!hasValue(lessonData.getLocalVideoUri())) {
                lessonData.setLocalVideoUri(videoUrl);
            }
            if (!hasValue(lessonData.getLocalVideoName())) {
                lessonData.setLocalVideoName(extractFileName(videoUrl));
            }
        } else {
            lessonData.setLocalVideoUri("");
            lessonData.setLocalVideoName("");
        }
        updateDocumentUrlFromFiles();

        if (hasLocalMediaUrl(lessonData.getLocalVideoUri()) || hasLocalAttachment()) {
            Toast.makeText(this, R.string.media_not_uploaded, Toast.LENGTH_SHORT).show();
            return;
        }

        saveLessonToRemoteOrReturn();
    }

    private boolean hasSelectedVideo() {
        return hasValue(lessonData.getLocalVideoUri()) || hasValue(lessonData.getVideoUrl());
    }

    private void saveLessonToRemoteOrReturn() {
        if (!hasValue(lessonData.getChapterId())) {
            returnLessonResult(false);
            return;
        }

        setSavingState(true);
        Lesson lesson = buildLessonForWrite();
        if (hasValue(lesson.getId())) {
            lessonRepository.update(lesson.getId(), lesson, new LessonRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveQuizzesForLesson(lesson.getId(), false);
                }

                @Override
                public void onError(String message) {
                    runIfActive(() -> {
                        setSavingState(false);
                        showSaveError(getString(R.string.lesson_save_failed), message);
                    });
                }
            });
            return;
        }

        lessonRepository.insertAndReturn(lesson, new LessonRepository.RepositoryCallback<Lesson>() {
            @Override
            public void onSuccess(Lesson savedLesson) {
                if (savedLesson == null || !hasValue(savedLesson.getId())) {
                    runIfActive(() -> {
                        setSavingState(false);
                        showSaveError(getString(R.string.lesson_save_failed), getString(R.string.lesson_created_no_id));
                    });
                    return;
                }

                lessonData.setId(savedLesson.getId());
                Lesson updatedLesson = buildLessonForWrite();
                lessonRepository.update(savedLesson.getId(), updatedLesson, new LessonRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        saveQuizzesForLesson(savedLesson.getId(), true);
                    }

                    @Override
                    public void onError(String message) {
                        runIfActive(() -> {
                            setSavingState(false);
                            showSaveError(getString(R.string.lesson_save_failed), message);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> {
                    setSavingState(false);
                    showSaveError(getString(R.string.lesson_save_failed), message);
                });
            }
        });
    }

    private Lesson buildLessonForWrite() {
        normalizeQuizLessonIds(lessonData.getId());
        Lesson lesson = new Lesson();
        lessonData.applyToLesson(lesson);
        if (hasValue(lesson.getLocalVideoUri())) {
            lesson.setVideoUrl(lesson.getLocalVideoUri());
        }
        if (!lesson.getLocalFileUris().isEmpty()) {
            lesson.setDocumentUrl(joinValues(lesson.getLocalFileUris()));
        }
        return lesson;
    }

    private void saveQuizzesForLesson(String lessonId, boolean notifyStudents) {
        normalizeQuizLessonIds(lessonId);
        quizRepository.replaceForLesson(lessonId, lessonData.getQuizzes(), new QuizRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runIfActive(() -> {
                    setSavingState(false);
                    if (notifyStudents) {
                        notifyStudentsAboutNewLesson();
                    }
                    Toast.makeText(EditLessonActivity.this, R.string.lesson_saved_success, Toast.LENGTH_SHORT).show();
                    returnLessonResult(true);
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> {
                    setSavingState(false);
                    showSaveError(getString(R.string.quiz_save_failed), message);
                });
            }
        });
    }

    private void notifyStudentsAboutNewLesson() {
        if (courseContentNotifier == null) {
            return;
        }
        courseContentNotifier.notifyLessonAdded(
                lessonData.getCourseId(),
                lessonData.getCourseTitle(),
                lessonData.getTitle()
        );
    }

    private void showSaveError(String title, String message) {
        String rawDetail = hasValue(message) ? message : getString(R.string.unknown_error);
        String detail = ApiErrorFormatter.fromMessage(rawDetail);
        Log.e(TAG, title + ": " + rawDetail);
        Toast.makeText(this, detail, Toast.LENGTH_LONG).show();
        if (isFinishing() || isDestroyed()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(detail)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showUploadError(String message) {
        String rawDetail = hasValue(message) ? message : getString(R.string.unknown_error);
        String detail = ApiErrorFormatter.fromMessage(rawDetail);
        Log.e(TAG, "Upload failed: " + rawDetail);
        Toast.makeText(this, detail, Toast.LENGTH_LONG).show();
        if (isFinishing() || isDestroyed()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.upload_failed_title)
                .setMessage(detail)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void normalizeQuizLessonIds(String lessonId) {
        if (!hasValue(lessonId)) {
            return;
        }
        for (Quiz quiz : lessonData.getQuizzes()) {
            if (quiz != null) {
                quiz.setLessonId(lessonId);
            }
        }
    }

    private void returnLessonResult(boolean synced) {
        Intent result = new Intent();
        result.putExtra(EXTRA_LESSON_DATA, lessonData);
        result.putExtra(EXTRA_LESSON_SYNCED, synced);
        setResult(RESULT_OK, result);
        hasUnsavedChanges = false;
        finish();
    }

    private void renderVideoName() {
        if (tvVideoFileName == null) return;

        String name = lessonData.getLocalVideoName();
        if (hasValue(name)) {
            tvVideoFileName.setText(name);
        } else if (hasValue(lessonData.getLocalVideoUri())) {
            tvVideoFileName.setText(extractFileName(lessonData.getLocalVideoUri()));
        } else if (hasValue(lessonData.getVideoUrl())) {
            tvVideoFileName.setText(extractFileName(lessonData.getVideoUrl()));
        } else {
            tvVideoFileName.setText(getString(R.string.no_video_selected));
        }

        if (btnDeleteVideo != null) {
            btnDeleteVideo.setVisibility(
                    hasValue(lessonData.getLocalVideoUri()) || hasValue(lessonData.getVideoUrl())
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    private void renderAttachments() {
        if (llAttachments == null) return;

        llAttachments.removeAllViews();
        List<String> names = lessonData.getLocalFileNames();
        List<String> uris = lessonData.getLocalFileUris();
        List<String> sizes = lessonData.getLocalFileSizes();

        if (uris.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.no_attachments));
            empty.setTextColor(getColor(R.color.text_secondary));
            empty.setTextSize(14);
            empty.setPadding(0, dp(6), 0, dp(6));
            llAttachments.addView(empty);
            return;
        }

        for (int i = 0; i < uris.size(); i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(10), dp(8), dp(4), dp(8));
            row.setBackgroundResource(R.drawable.bg_file_chip);

            LinearLayout textContainer = new LinearLayout(this);
            textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            textContainer.setOrientation(LinearLayout.VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setEllipsize(TextUtils.TruncateAt.END);
            tvName.setMaxLines(1);
            tvName.setText(i < names.size() && hasValue(names.get(i)) ? names.get(i) : extractFileName(uris.get(i)));
            tvName.setTextColor(getColor(R.color.text_primary));
            tvName.setTextSize(14);

            TextView tvSize = new TextView(this);
            tvSize.setText(i < sizes.size() ? valueOrEmpty(sizes.get(i)) : "");
            tvSize.setTextColor(getColor(R.color.text_secondary));
            tvSize.setTextSize(13);

            TextView btnDelete = new TextView(this);
            btnDelete.setLayoutParams(new LinearLayout.LayoutParams(dp(72), dp(36)));
            btnDelete.setGravity(Gravity.CENTER);
            btnDelete.setText(R.string.delete_upper);
            btnDelete.setTextColor(getColor(R.color.status_error));
            btnDelete.setTextSize(12);
            btnDelete.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
            btnDelete.setOnClickListener(v -> confirmDeleteAttachment(index));

            textContainer.addView(tvName);
            textContainer.addView(tvSize);
            row.addView(textContainer);
            row.addView(btnDelete);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) row.getLayoutParams();
            params.setMargins(0, 0, 0, dp(8));
            row.setLayoutParams(params);
            llAttachments.addView(row);
        }
    }

    private void renderQuizzes() {
        if (llQuizContainer == null) return;

        llQuizContainer.removeAllViews();
        for (Quiz quiz : lessonData.getQuizzes()) {
            addQuizView(quiz);
        }
        refreshQuizIndexes();
    }

    private void addQuizView(Quiz quiz) {
        if (llQuizContainer == null) return;

        ViewGroup row = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.item_intructor_quizz_question, llQuizContainer, false);
        bindQuizView(row, quiz);
        setupQuizView(row);
        llQuizContainer.addView(row);
    }

    private void bindQuizView(ViewGroup row, Quiz quiz) {
        if (quiz == null) return;

        EditText etQuestion = row.findViewById(R.id.etQuestionContent);
        EditText etAnswerA = row.findViewById(R.id.etAnswerA);
        EditText etAnswerB = row.findViewById(R.id.etAnswerB);
        EditText etAnswerC = row.findViewById(R.id.etAnswerC);
        EditText etAnswerD = row.findViewById(R.id.etAnswerD);

        etQuestion.setText(valueOrEmpty(quiz.getQuestion()));
        ArrayList<String> options = normalizeOptions(quiz.getOptions());
        etAnswerA.setText(options.size() > 0 ? valueOrEmpty(options.get(0)) : "");
        etAnswerB.setText(options.size() > 1 ? valueOrEmpty(options.get(1)) : "");
        etAnswerC.setText(options.size() > 2 ? valueOrEmpty(options.get(2)) : "");
        etAnswerD.setText(options.size() > 3 ? valueOrEmpty(options.get(3)) : "");

        selectCorrectAnswer(row, quiz.getCorrectAnswer(), options);
    }

    private void setupQuizView(ViewGroup row) {
        View btnRemove = row.findViewById(R.id.btnRemoveQuiz);
        if (btnRemove != null) {
            btnRemove.setOnClickListener(v -> confirmDeleteQuiz(row));
        }

        RadioButton rbA = row.findViewById(R.id.rbA);
        RadioButton rbB = row.findViewById(R.id.rbB);
        RadioButton rbC = row.findViewById(R.id.rbC);
        RadioButton rbD = row.findViewById(R.id.rbD);
        View.OnClickListener radioClickListener = clicked -> {
            if (rbA != null) rbA.setChecked(clicked == rbA);
            if (rbB != null) rbB.setChecked(clicked == rbB);
            if (rbC != null) rbC.setChecked(clicked == rbC);
            if (rbD != null) rbD.setChecked(clicked == rbD);
            markDirty();
        };
        if (rbA != null) rbA.setOnClickListener(radioClickListener);
        if (rbB != null) rbB.setOnClickListener(radioClickListener);
        if (rbC != null) rbC.setOnClickListener(radioClickListener);
        if (rbD != null) rbD.setOnClickListener(radioClickListener);

        TextWatcher watcher = createDirtyWatcher();
        addQuizTextWatcher(row, R.id.etQuestionContent, watcher);
        addQuizTextWatcher(row, R.id.etAnswerA, watcher);
        addQuizTextWatcher(row, R.id.etAnswerB, watcher);
        addQuizTextWatcher(row, R.id.etAnswerC, watcher);
        addQuizTextWatcher(row, R.id.etAnswerD, watcher);
    }

    private void addQuizTextWatcher(ViewGroup row, int viewId, TextWatcher watcher) {
        EditText editText = row.findViewById(viewId);
        if (editText != null) {
            editText.addTextChangedListener(watcher);
        }
    }

    private void refreshQuizIndexes() {
        if (llQuizContainer == null) return;

        for (int i = 0; i < llQuizContainer.getChildCount(); i++) {
            TextView tvQuizIndex = llQuizContainer.getChildAt(i).findViewById(R.id.tvQuizIndex);
            if (tvQuizIndex != null) {
                tvQuizIndex.setText(getString(R.string.question_count_format, i + 1));
            }
        }
    }

    private ArrayList<Quiz> collectQuizzesFromViews() {
        ArrayList<Quiz> quizzes = new ArrayList<>();
        if (llQuizContainer == null) {
            return quizzes;
        }

        for (int i = 0; i < llQuizContainer.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) llQuizContainer.getChildAt(i);
            EditText etQuestion = row.findViewById(R.id.etQuestionContent);
            EditText etAnswerA = row.findViewById(R.id.etAnswerA);
            EditText etAnswerB = row.findViewById(R.id.etAnswerB);
            EditText etAnswerC = row.findViewById(R.id.etAnswerC);
            EditText etAnswerD = row.findViewById(R.id.etAnswerD);

            String question = textValue(etQuestion);
            ArrayList<String> options = new ArrayList<>();
            options.add(textValue(etAnswerA));
            options.add(textValue(etAnswerB));
            options.add(textValue(etAnswerC));
            options.add(textValue(etAnswerD));
            boolean blankQuestion = TextUtils.isEmpty(question);
            boolean blankOptions = true;
            for (String option : options) {
                blankOptions = blankOptions && TextUtils.isEmpty(option);
            }
            if (blankQuestion && blankOptions) {
                continue;
            }
            if (blankQuestion) {
                etQuestion.setError(getString(R.string.question_required));
                etQuestion.requestFocus();
                return null;
            }
            for (int optionIndex = 0; optionIndex < options.size(); optionIndex++) {
                if (TextUtils.isEmpty(options.get(optionIndex))) {
                    EditText target = optionIndex == 0 ? etAnswerA
                            : optionIndex == 1 ? etAnswerB
                            : optionIndex == 2 ? etAnswerC
                            : etAnswerD;
                    target.setError(getString(R.string.answer_required));
                    target.requestFocus();
                    return null;
                }
            }

            String correctAnswer = getSelectedAnswerKey(row);
            if (TextUtils.isEmpty(correctAnswer)) {
                Toast.makeText(this, getString(R.string.choose_correct_answer_format, i + 1), Toast.LENGTH_SHORT).show();
                return null;
            }

            Quiz quiz = new Quiz();
            quiz.setQuestion(question);
            quiz.setOptions(options);
            quiz.setCorrectAnswer(correctAnswer);
            quizzes.add(quiz);
        }
        return quizzes;
    }

    private String getSelectedAnswerKey(ViewGroup row) {
        RadioButton rbA = row.findViewById(R.id.rbA);
        RadioButton rbB = row.findViewById(R.id.rbB);
        RadioButton rbC = row.findViewById(R.id.rbC);
        RadioButton rbD = row.findViewById(R.id.rbD);
        if (rbA != null && rbA.isChecked()) return "A";
        if (rbB != null && rbB.isChecked()) return "B";
        if (rbC != null && rbC.isChecked()) return "C";
        if (rbD != null && rbD.isChecked()) return "D";
        return "";
    }

    private void selectCorrectAnswer(ViewGroup row, String answer, ArrayList<String> options) {
        RadioButton rbA = row.findViewById(R.id.rbA);
        RadioButton rbB = row.findViewById(R.id.rbB);
        RadioButton rbC = row.findViewById(R.id.rbC);
        RadioButton rbD = row.findViewById(R.id.rbD);
        String key = valueOrEmpty(answer);
        if (!hasValue(key) && options.size() > 0) {
            key = "A";
        }
        if (!"A".equalsIgnoreCase(key)
                && !"B".equalsIgnoreCase(key)
                && !"C".equalsIgnoreCase(key)
                && !"D".equalsIgnoreCase(key)) {
            for (int i = 0; i < options.size(); i++) {
                if (key.equals(options.get(i))) {
                    key = String.valueOf((char) ('A' + i));
                    break;
                }
            }
        }
        if (rbA != null) rbA.setChecked("A".equalsIgnoreCase(key));
        if (rbB != null) rbB.setChecked("B".equalsIgnoreCase(key));
        if (rbC != null) rbC.setChecked("C".equalsIgnoreCase(key));
        if (rbD != null) rbD.setChecked("D".equalsIgnoreCase(key));
    }

    private ArrayList<String> normalizeOptions(Object optionsValue) {
        ArrayList<String> options = new ArrayList<>();
        if (optionsValue instanceof List) {
            for (Object value : (List<?>) optionsValue) {
                options.add(value != null ? String.valueOf(value) : "");
            }
        }
        return options;
    }

    private String textValue(EditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }

    private void updateDocumentUrlFromFiles() {
        StringBuilder builder = new StringBuilder();
        for (String uri : lessonData.getLocalFileUris()) {
            if (!hasValue(uri)) continue;

            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(uri);
        }
        lessonData.setDocumentUrl(builder.toString());
    }

    private boolean hasLocalAttachment() {
        for (String uri : lessonData.getLocalFileUris()) {
            if (hasLocalMediaUrl(uri)) {
                return true;
            }
        }
        return false;
    }

    private int findFileIndex(String uri) {
        List<String> uris = lessonData.getLocalFileUris();
        for (int i = 0; i < uris.size(); i++) {
            if (uri.equals(uris.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void beginUpload() {
        pendingUploadCount++;
        updateUploadState();
    }

    private void finishUpload() {
        if (pendingUploadCount > 0) {
            pendingUploadCount--;
        }
        updateUploadState();
    }

    private void updateUploadState() {
        boolean idle = pendingUploadCount == 0;
        boolean controlsEnabled = idle && !isSaving;
        btnSaveLesson.setEnabled(controlsEnabled);
        btnChooseVideo.setEnabled(controlsEnabled);
        if (btnDeleteVideo != null) {
            btnDeleteVideo.setEnabled(controlsEnabled);
        }
        btnAddAttachment.setEnabled(controlsEnabled);
        btnAddQuiz.setEnabled(controlsEnabled);
        btnSaveLesson.setAlpha(controlsEnabled ? 1.0f : 0.5f);
        btnChooseVideo.setAlpha(controlsEnabled ? 1.0f : 0.5f);
        btnAddAttachment.setAlpha(controlsEnabled ? 1.0f : 0.5f);
        btnAddQuiz.setAlpha(controlsEnabled ? 1.0f : 0.5f);
        if (btnChooseVideo != null) {
            btnChooseVideo.setText(idle ? getString(R.string.choose) : getString(R.string.uploading));
        }
        if (btnSaveLesson != null) {
            btnSaveLesson.setText(isSaving ? getString(R.string.saving) : getString(R.string.save));
        }
    }

    private void setSavingState(boolean saving) {
        isSaving = saving;
        updateUploadState();
    }

    private boolean hasLocalMediaUrl(String value) {
        return hasValue(value)
                && !value.startsWith("http://")
                && !value.startsWith("https://");
    }

    private int readVideoDurationSeconds(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            String durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (!hasValue(durationMs)) {
                return 0;
            }

            long millis = Long.parseLong(durationMs);
            return millis > 0 ? (int) Math.max(1, (millis + 999) / 1000) : 0;
        } catch (Exception e) {
            Log.w(TAG, "Cannot read video duration", e);
            return 0;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private String getDisplayName(Uri uri) {
        String displayName = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex);
                }
            }
        }

        if (TextUtils.isEmpty(displayName)) {
            displayName = uri.getLastPathSegment();
        }
        return TextUtils.isEmpty(displayName) ? getString(R.string.selected_file) : displayName;
    }

    private String getDisplaySize(Uri uri) {
        long size = -1;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        }
        if (size <= 0) {
            return "";
        }
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", size / 1024f);
        }
        return String.format(Locale.US, "%.1f MB", size / (1024f * 1024f));
    }

    private void runIfActive(Runnable action) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            action.run();
        });
    }

    private String extractFileName(String value) {
        if (!hasValue(value)) return getString(R.string.selected_file);

        int slashIndex = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < value.length() - 1) {
            return value.substring(slashIndex + 1);
        }
        return value;
    }

    private void setupDirtyWatchers() {
        TextWatcher watcher = createDirtyWatcher();
        etLessonTitle.addTextChangedListener(watcher);
        etLessonDescription.addTextChangedListener(watcher);
    }

    private TextWatcher createDirtyWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                markDirty();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void markDirty() {
        if (!isBindingData) {
            hasUnsavedChanges = true;
        }
    }

    private void confirmDeleteVideo() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_video)
                .setMessage(R.string.delete_video_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> clearVideoFile())
                .show();
    }

    private void confirmDeleteAttachment(int index) {
        if (index < 0 || index >= lessonData.getLocalFileUris().size()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_attachment)
                .setMessage(R.string.delete_attachment_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    lessonData.removeFile(index);
                    updateDocumentUrlFromFiles();
                    renderAttachments();
                    markDirty();
                })
                .show();
    }

    private void confirmDeleteQuiz(View row) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_quiz)
                .setMessage(R.string.delete_quiz_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    llQuizContainer.removeView(row);
                    refreshQuizIndexes();
                    markDirty();
                })
                .show();
    }

    private void confirmExitIfDirty(Runnable exitAction) {
        if (!hasUnsavedChanges) {
            exitAction.run();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.save_lesson_before_leaving)
                .setPositiveButton(R.string.save, (dialog, which) -> saveLesson())
                .setNegativeButton(R.string.discard, (dialog, which) -> {
                    hasUnsavedChanges = false;
                    exitAction.run();
                })
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private String joinValues(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (!hasValue(value)) continue;

            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private String buildLessonMediaFolder(String mediaType) {
        String courseId = hasValue(lessonData.getCourseId()) ? lessonData.getCourseId() : "draft";
        String chapterId = hasValue(lessonData.getChapterId()) ? lessonData.getChapterId() : "draft-chapter";
        String lessonId = hasValue(lessonData.getId()) ? lessonData.getId() : "draft-lesson";
        return "courses/" + courseId + "/chapters/" + chapterId + "/lessons/" + lessonId + "/" + mediaType;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void redirectToLogin() {
        sessionManager.clear();
        Toast.makeText(this, R.string.session_expired_login_again, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
