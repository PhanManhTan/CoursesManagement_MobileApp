package com.example.myapplication.activities.instructor;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.InstructorChapterAdapter;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.data.repository.CategoryRepository;
import com.example.myapplication.data.repository.SupabaseStorageRepository;
import com.example.myapplication.models.Category;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.ChapterWithLessons;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.LessonEditorData;
import com.example.myapplication.utils.ApiErrorFormatter;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.viewmodels.EditCourseViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditCourseActivity extends AppCompatActivity {
    private static final String TAG = "EditCourseActivity";

    public static final String EXTRA_COURSE = "COURSE";
    public static final String EXTRA_COURSE_ID = "COURSE_ID";

    private EditText etTitle;
    private EditText etDescription;
    private EditText etPrice;
    private Spinner spCategory;
    private ImageView ivThumbnailPreview;
    private TextView tvEditorTitle;
    private TextView tvChapterCount;
    private MaterialButton btnPickThumbnail;
    private MaterialButton btnAddChapter;
    private TextView btnSave;
    private NestedScrollView courseDetailScroll;
    private RecyclerView rvChapters;
    private InstructorChapterAdapter chapterAdapter;
    private final List<ChapterWithLessons> chapterList = new ArrayList<>();
    private EditCourseViewModel viewModel;
    private CategoryRepository categoryRepository;
    private SupabaseStorageRepository storageRepository;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> editLessonLauncher;
    private String currentThumbnailUrl;
    private String selectedCategoryId;
    private String loadedStructureCourseId;
    private int pendingChapterPosition = RecyclerView.NO_POSITION;
    private int pendingLessonPosition = RecyclerView.NO_POSITION;
    private int pendingUploadCount;
    private boolean isBindingData;
    private boolean isBindingCategory;
    private boolean hasUnsavedChanges;
    private boolean isStructureLoading;
    private boolean isSaving;
    private SessionManager sessionManager;
    private ArrayAdapter<String> categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        registerLaunchers();
        setContentView(R.layout.activity_instructor_course_detail);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupCategorySpinner();
        setupListeners();
        loadCategories();
        loadInitialData();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        spCategory = findViewById(R.id.spCategory);
        ivThumbnailPreview = findViewById(R.id.ivThumbnailPreview);
        tvEditorTitle = findViewById(R.id.tvEditorTitle);
        tvChapterCount = findViewById(R.id.tvChapterCount);
        btnPickThumbnail = findViewById(R.id.btnPickThumbnail);
        btnAddChapter = findViewById(R.id.btnAddChapter);
        btnSave = findViewById(R.id.btnSave);
        courseDetailScroll = findViewById(R.id.courseDetailScroll);
        rvChapters = findViewById(R.id.rvChapters);
        categoryRepository = new CategoryRepository(this);
        storageRepository = new SupabaseStorageRepository(this);
    }

    private void setupRecyclerView() {
        chapterAdapter = new InstructorChapterAdapter(createChapterActionListener());
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setItemAnimator(null);
        rvChapters.setNestedScrollingEnabled(false);
        rvChapters.setAdapter(chapterAdapter);
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
        spCategory.setAdapter(categoryAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isBindingCategory) {
                    return;
                }
                selectedCategoryId = resolveCategoryId(position);
                if (!isBindingData) {
                    markDirty();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (isBindingCategory) {
                    return;
                }
                selectedCategoryId = null;
            }
        });
        renderCategoryOptions();
    }

    private InstructorChapterAdapter.OnChapterActionListener createChapterActionListener() {
        return new InstructorChapterAdapter.OnChapterActionListener() {
            @Override
            public void onEditChapter(int chapterPosition) {
                showChapterDialog(chapterPosition);
            }

            @Override
            public void onDeleteChapter(int chapterPosition) {
                deleteChapter(chapterPosition);
            }

            @Override
            public void onAddLesson(int chapterPosition) {
                openLessonEditor(chapterPosition, RecyclerView.NO_POSITION);
            }

            @Override
            public void onEditLesson(int chapterPosition, int lessonPosition) {
                openLessonEditor(chapterPosition, lessonPosition);
            }

            @Override
            public void onDeleteLesson(int chapterPosition, int lessonPosition) {
                deleteLesson(chapterPosition, lessonPosition);
            }
        };
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(EditCourseViewModel.class);
        viewModel.resetSaveState();

        viewModel.getCourse().observe(this, this::populateCourse);
        viewModel.getChapterDrafts().observe(this, drafts -> {
            isBindingData = true;
            chapterList.clear();
            if (drafts != null) {
                chapterList.addAll(drafts);
            }
            refreshChapterList();
            isBindingData = false;
        });
        viewModel.getSaveSuccess().observe(this, saved -> {
            if (Boolean.TRUE.equals(saved)) {
                hasUnsavedChanges = false;
                Toast.makeText(this, R.string.course_saved_success, Toast.LENGTH_SHORT).show();
                viewModel.resetSaveState();
                setResult(RESULT_OK);
                finish();
            }
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showEditorError(getString(R.string.course_editor_error), message);
                viewModel.resetSaveState();
            }
        });
        viewModel.getIsStructureLoading().observe(this, loading -> {
            isStructureLoading = Boolean.TRUE.equals(loading);
            updateEditorLockState();
        });
        viewModel.getIsSaving().observe(this, saving -> {
            isSaving = Boolean.TRUE.equals(saving);
            updateEditorLockState();
        });
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> confirmExitIfDirty(this::finish));
        }
        btnAddChapter.setOnClickListener(v -> showChapterDialog(RecyclerView.NO_POSITION));
        btnPickThumbnail.setOnClickListener(v -> launchImagePicker());
        btnSave.setOnClickListener(v -> attemptSave());
        setupDirtyWatchers();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExitIfDirty(EditCourseActivity.this::finish);
            }
        });
    }

    private void loadInitialData() {
        if (getIntent() == null) {
            viewModel.setCourse(new Course());
            return;
        }

        Course course = readCourseExtra();
        String courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        if (course != null) {
            viewModel.setCourse(course);
        } else if (hasValue(courseId)) {
            viewModel.loadCourse(courseId);
        } else {
            viewModel.setCourse(new Course());
        }
    }

    private void loadCategories() {
        categoryRepository.getAll(new CategoryRepository.RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                runIfActive(() -> {
                    categoryList.clear();
                    if (data != null) {
                        categoryList.addAll(data);
                    }
                    renderCategoryOptions();
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> Toast.makeText(
                        EditCourseActivity.this,
                        getString(R.string.error_loading_categories, ApiErrorFormatter.fromMessage(message)),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private Course readCourseExtra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getSerializableExtra(EXTRA_COURSE, Course.class);
        }
        return (Course) getIntent().getSerializableExtra(EXTRA_COURSE);
    }

    private void populateCourse(Course course) {
        if (course == null) return;

        isBindingData = true;
        tvEditorTitle.setText(hasValue(course.getId()) ? getString(R.string.edit_course_title) : getString(R.string.create_course_title));
        etTitle.setText(valueOrEmpty(course.getTitle()));
        etDescription.setText(valueOrEmpty(course.getDescription()));
        etPrice.setText(course.getPrice() > 0 ? String.format(Locale.US, "%.0f", course.getPrice()) : "");
        selectedCategoryId = course.getCategoryId();
        syncCategorySelection();

        currentThumbnailUrl = course.getThumbnailUrl();
        if (hasValue(currentThumbnailUrl)) {
            Glide.with(this)
                    .load(currentThumbnailUrl)
                    .placeholder(R.drawable.image_courses)
                    .into(ivThumbnailPreview);
        } else {
            ivThumbnailPreview.setImageResource(R.drawable.image_courses);
        }
        isBindingData = false;

        if (hasValue(course.getId()) && !course.getId().equals(loadedStructureCourseId)) {
            loadedStructureCourseId = course.getId();
            viewModel.loadCourseStructure(course.getId());
        } else if (!hasValue(course.getId())) {
            loadedStructureCourseId = null;
            chapterList.clear();
            refreshChapterList();
        }
    }

    private void renderCategoryOptions() {
        if (categoryAdapter == null) return;

        isBindingCategory = true;
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.select_category));
        for (Category category : categoryList) {
            names.add(getCategoryDisplayName(category));
        }
        categoryAdapter.clear();
        categoryAdapter.addAll(names);
        categoryAdapter.notifyDataSetChanged();
        syncCategorySelection();
        updateEditorLockState();
    }

    private void syncCategorySelection() {
        if (spCategory == null || categoryAdapter == null) {
            return;
        }

        int selectedPosition = 0;
        if (hasValue(selectedCategoryId)) {
            for (int i = 0; i < categoryList.size(); i++) {
                Category category = categoryList.get(i);
                if (category != null && selectedCategoryId.equals(category.getId())) {
                    selectedPosition = i + 1;
                    break;
                }
            }
        }
        spCategory.setSelection(selectedPosition, false);
        spCategory.post(() -> isBindingCategory = false);
    }

    private String resolveCategoryId(int spinnerPosition) {
        int categoryIndex = spinnerPosition - 1;
        if (categoryIndex < 0 || categoryIndex >= categoryList.size()) {
            return null;
        }

        Category category = categoryList.get(categoryIndex);
        return category != null ? category.getId() : null;
    }

    private String getCategoryDisplayName(Category category) {
        if (category == null) {
            return getString(R.string.category);
        }
        if (hasValue(category.getName())) {
            return category.getName();
        }
        if (hasValue(category.getSlug())) {
            return category.getSlug();
        }
        return getString(R.string.category);
    }

    private void showChapterDialog(int chapterPosition) {
        boolean editing = isValidChapterPosition(chapterPosition);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(getString(R.string.chapter_title_hint));
        input.setTextColor(getColor(R.color.text_primary));
        input.setHintTextColor(getColor(R.color.text_hint));
        input.setPadding(24, 16, 24, 16);
        input.setBackgroundResource(R.drawable.bg_input_square);
        if (editing) {
            input.setText(chapterList.get(chapterPosition).getChapter().getTitle());
            input.setSelection(input.getText().length());
        }

        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        androidx.appcompat.widget.LinearLayoutCompat wrapper = new androidx.appcompat.widget.LinearLayoutCompat(this);
        wrapper.setPadding(padding, padding / 2, padding, 0);
        wrapper.addView(input, new androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        new AlertDialog.Builder(this)
                .setTitle(editing ? getString(R.string.edit_chapter_dialog) : getString(R.string.add_chapter_dialog))
                .setView(wrapper)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(editing ? getString(R.string.save) : getString(R.string.add), (dialog, which) -> {
                    String title = input.getText().toString().trim();
                    if (!hasValue(title)) {
                        title = getString(R.string.default_chapter_title, editing ? chapterPosition + 1 : chapterList.size() + 1);
                    }
                    if (editing) {
                        chapterList.get(chapterPosition).getChapter().setTitle(title);
                        chapterAdapter.notifyItemChanged(chapterPosition);
                        markDirty();
                    } else {
                        Chapter chapter = new Chapter(null, null, title, chapterList.size() + 1);
                        chapterList.add(new ChapterWithLessons(chapter, new ArrayList<>()));
                        refreshChapterList();
                        scrollToBottom();
                        markDirty();
                    }
                })
                .show();
    }

    private void deleteChapter(int chapterPosition) {
        if (!isValidChapterPosition(chapterPosition)) return;

        Chapter chapter = chapterList.get(chapterPosition).getChapter();
        String title = chapter != null && hasValue(chapter.getTitle())
                ? chapter.getTitle()
                : getString(R.string.this_chapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_chapter)
                .setMessage(getString(R.string.delete_chapter_confirm, title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    chapterList.remove(chapterPosition);
                    refreshChapterList();
                    markDirty();
                })
                .show();
    }

    private void openLessonEditor(int chapterPosition, int lessonPosition) {
        if (!isValidChapterPosition(chapterPosition)) return;

        Chapter chapter = chapterList.get(chapterPosition).getChapter();
        if (chapter != null && !hasValue(chapter.getId()) && hasValue(getCurrentCourseId())) {
            saveChapterThenOpenLesson(chapterPosition, lessonPosition);
            return;
        }

        launchLessonEditor(chapterPosition, lessonPosition);
    }

    private void saveChapterThenOpenLesson(int chapterPosition, int lessonPosition) {
        if (!isValidChapterPosition(chapterPosition)) return;

        Chapter chapter = chapterList.get(chapterPosition).getChapter();
        Toast.makeText(this, R.string.saving_chapter_before_lesson, Toast.LENGTH_SHORT).show();
        viewModel.saveChapterDraft(chapter, chapterPosition + 1, new EditCourseViewModel.ChapterSaveCallback() {
            @Override
            public void onSuccess(Chapter savedChapter) {
                runIfActive(() -> {
                    chapterAdapter.notifyItemChanged(chapterPosition);
                    launchLessonEditor(chapterPosition, lessonPosition);
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> showEditorError(getString(R.string.chapter_save_failed), message));
            }
        });
    }

    private void launchLessonEditor(int chapterPosition, int lessonPosition) {
        if (!isValidChapterPosition(chapterPosition)) return;

        pendingChapterPosition = chapterPosition;
        pendingLessonPosition = lessonPosition;

        Lesson lesson;
        if (isValidLessonPosition(chapterPosition, lessonPosition)) {
            lesson = chapterList.get(chapterPosition).getLessons().get(lessonPosition);
        } else {
            lesson = new Lesson();
            lesson.setTitle(getString(R.string.default_lesson_title, chapterList.get(chapterPosition).getLessons().size() + 1));
            lesson.setOrderIndex(chapterList.get(chapterPosition).getLessons().size() + 1);
        }

        Chapter chapter = chapterList.get(chapterPosition).getChapter();
        if (chapter != null) {
            lesson.setChapterId(chapter.getId());
        }

        LessonEditorData editorData = LessonEditorData.fromLesson(lesson);
        editorData.setCourseId(resolveCourseStorageId());
        editorData.setCourseTitle(resolveCourseTitle());

        Intent intent = new Intent(this, EditLessonActivity.class);
        intent.putExtra(EditLessonActivity.EXTRA_LESSON_DATA, editorData);
        editLessonLauncher.launch(intent);
    }

    private void handleLessonResult(Intent data) {
        if (!isValidChapterPosition(pendingChapterPosition)) {
            clearPendingLessonTarget();
            return;
        }

        LessonEditorData editorData;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            editorData = data.getSerializableExtra(EditLessonActivity.EXTRA_LESSON_DATA, LessonEditorData.class);
        } else {
            editorData = (LessonEditorData) data.getSerializableExtra(EditLessonActivity.EXTRA_LESSON_DATA);
        }
        if (editorData == null) {
            clearPendingLessonTarget();
            return;
        }

        boolean lessonSynced = data.getBooleanExtra(EditLessonActivity.EXTRA_LESSON_SYNCED, false);
        ChapterWithLessons chapterDraft = chapterList.get(pendingChapterPosition);
        Chapter chapter = chapterDraft.getChapter();
        editorData.setChapterId(chapter != null ? chapter.getId() : null);

        Lesson lesson;
        if (isValidLessonPosition(pendingChapterPosition, pendingLessonPosition)) {
            lesson = chapterDraft.getLessons().get(pendingLessonPosition);
            editorData.applyToLesson(lesson);
            chapterAdapter.notifyItemChanged(pendingChapterPosition);
            if (!lessonSynced) {
                markDirty();
            }
        } else {
            lesson = new Lesson();
            editorData.applyToLesson(lesson);
            chapterDraft.getLessons().add(lesson);
            refreshChapterList();
            scrollToBottom();
            if (!lessonSynced) {
                markDirty();
            }
        }
        clearPendingLessonTarget();
    }

    private void deleteLesson(int chapterPosition, int lessonPosition) {
        if (!isValidLessonPosition(chapterPosition, lessonPosition)) return;

        Lesson lesson = chapterList.get(chapterPosition).getLessons().get(lessonPosition);
        String title = lesson != null && hasValue(lesson.getTitle())
                ? lesson.getTitle()
                : getString(R.string.this_lesson);

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_lesson)
                .setMessage(getString(R.string.delete_lesson_confirm, title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    chapterList.get(chapterPosition).getLessons().remove(lessonPosition);
                    chapterAdapter.notifyItemChanged(chapterPosition);
                    updateChapterCount();
                    markDirty();
                })
                .show();
    }

    private void attemptSave() {
        if (isStructureLoading) {
            Toast.makeText(this, R.string.course_structure_loading, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSaving) {
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError(getString(R.string.course_title_required));
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError(getString(R.string.description_required));
            etDescription.requestFocus();
            return;
        }
        if (!hasValue(selectedCategoryId)) {
            Toast.makeText(this, R.string.course_category_required, Toast.LENGTH_SHORT).show();
            if (spCategory != null) {
                spCategory.requestFocus();
            }
            return;
        }

        double price;
        try {
            price = TextUtils.isEmpty(priceText) ? 0 : Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            etPrice.setError(getString(R.string.invalid_price));
            etPrice.requestFocus();
            return;
        }

        if (pendingUploadCount > 0) {
            Toast.makeText(this, R.string.wait_uploads_finish, Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasLocalMediaUrl(currentThumbnailUrl) || hasUnuploadedLessonMedia()) {
            Toast.makeText(this, R.string.media_not_uploaded, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateLessonVideos()) {
            return;
        }

        normalizeOrderIndexes();
        isSaving = true;
        updateEditorLockState();
        viewModel.saveCourseStructure(title, description, price, currentThumbnailUrl, selectedCategoryId, createChapterSnapshot());
    }

    private void registerLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadCourseThumbnail(uri);
                    }
                }
        );
        editLessonLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleLessonResult(result.getData());
                    } else {
                        clearPendingLessonTarget();
                    }
                }
        );
    }

    private void launchImagePicker() {
        try {
            pickImageLauncher.launch("image/*");
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_open_image_picker, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCourseThumbnail(Uri imageUri) {
        String fileName = getDisplayName(imageUri);
        beginMediaUpload();
        currentThumbnailUrl = imageUri.toString();
        setThumbnailUploadState(true);
        storageRepository.uploadToFolder(imageUri, fileName, buildCourseMediaFolder("thumbnails"), new SupabaseStorageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String url) {
                runIfActive(() -> {
                    currentThumbnailUrl = url;
                    Glide.with(EditCourseActivity.this)
                            .load(currentThumbnailUrl)
                            .placeholder(R.drawable.image_courses)
                            .into(ivThumbnailPreview);
                    Toast.makeText(EditCourseActivity.this, R.string.thumbnail_uploaded, Toast.LENGTH_SHORT).show();
                    setThumbnailUploadState(false);
                    markDirty();
                    finishMediaUpload();
                });
            }

            @Override
            public void onError(String message) {
                runIfActive(() -> {
                    currentThumbnailUrl = null;
                    ivThumbnailPreview.setImageResource(R.drawable.image_courses);
                    showUploadError(message);
                    setThumbnailUploadState(false);
                    finishMediaUpload();
                });
            }
        });
    }

    private void refreshChapterList() {
        updateChapterCount();
        chapterAdapter.submitList(new ArrayList<>(chapterList));
    }

    private void updateChapterCount() {
        int lessonCount = 0;
        for (ChapterWithLessons draft : chapterList) {
            lessonCount += draft.getLessons().size();
        }
        tvChapterCount.setText(getString(R.string.chapter_lesson_count_format, chapterList.size(), lessonCount));
    }

    private void normalizeOrderIndexes() {
        for (int chapterIndex = 0; chapterIndex < chapterList.size(); chapterIndex++) {
            ChapterWithLessons draft = chapterList.get(chapterIndex);
            Chapter chapter = draft.getChapter();
            if (chapter != null) {
                chapter.setOrderIndex(chapterIndex + 1);
            }
            List<Lesson> lessons = draft.getLessons();
            for (int lessonIndex = 0; lessonIndex < lessons.size(); lessonIndex++) {
                lessons.get(lessonIndex).setOrderIndex(lessonIndex + 1);
            }
        }
    }

    private boolean hasUnuploadedLessonMedia() {
        for (ChapterWithLessons draft : chapterList) {
            for (Lesson lesson : draft.getLessons()) {
                if (hasLocalMediaUrl(lesson.getLocalVideoUri())) {
                    return true;
                }
                for (String fileUri : lesson.getLocalFileUris()) {
                    if (hasLocalMediaUrl(fileUri)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean validateLessonVideos() {
        for (ChapterWithLessons draft : chapterList) {
            if (draft == null) continue;

            for (Lesson lesson : draft.getLessons()) {
                if (lesson == null) continue;

                if (!hasValue(lesson.getLocalVideoUri()) && !hasValue(lesson.getVideoUrl())) {
                    String lessonName = hasValue(lesson.getTitle()) ? lesson.getTitle().trim() : getString(R.string.this_lesson);
                    showEditorError(
                            getString(R.string.lesson_video_required),
                            getString(R.string.lesson_video_required_message, lessonName)
                    );
                    return false;
                }
            }
        }
        return true;
    }

    private void setThumbnailUploadState(boolean uploading) {
        updateEditorLockState();
    }

    private void beginMediaUpload() {
        pendingUploadCount++;
        updateSaveState();
    }

    private void finishMediaUpload() {
        if (pendingUploadCount > 0) {
            pendingUploadCount--;
        }
        updateSaveState();
    }

    private void updateSaveState() {
        updateEditorLockState();
    }

    private void updateEditorLockState() {
        boolean uploadsIdle = pendingUploadCount == 0;
        boolean canEdit = uploadsIdle && !isStructureLoading && !isSaving;
        if (btnSave != null) {
            btnSave.setEnabled(canEdit);
            btnSave.setAlpha(canEdit ? 1.0f : 0.5f);
            btnSave.setText(isSaving ? getString(R.string.saving) : getString(R.string.save));
        }
        if (btnAddChapter != null) {
            btnAddChapter.setEnabled(canEdit);
        }
        if (btnPickThumbnail != null) {
            btnPickThumbnail.setEnabled(canEdit);
        }
        if (spCategory != null) {
            spCategory.setEnabled(canEdit && !categoryList.isEmpty());
        }
        if (chapterAdapter != null) {
            chapterAdapter.setActionsEnabled(canEdit);
        }
    }

    private List<ChapterWithLessons> createChapterSnapshot() {
        List<ChapterWithLessons> snapshot = new ArrayList<>();
        for (ChapterWithLessons draft : chapterList) {
            if (draft != null) {
                snapshot.add(draft.deepCopy());
            }
        }
        return snapshot;
    }

    private void scrollToBottom() {
        if (courseDetailScroll != null) {
            courseDetailScroll.post(() -> courseDetailScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private boolean isValidChapterPosition(int position) {
        return position >= 0 && position < chapterList.size();
    }

    private boolean isValidLessonPosition(int chapterPosition, int lessonPosition) {
        return isValidChapterPosition(chapterPosition)
                && lessonPosition >= 0
                && lessonPosition < chapterList.get(chapterPosition).getLessons().size();
    }

    private void clearPendingLessonTarget() {
        pendingChapterPosition = RecyclerView.NO_POSITION;
        pendingLessonPosition = RecyclerView.NO_POSITION;
    }

    private boolean hasLocalMediaUrl(String value) {
        return hasValue(value)
                && !value.startsWith("http://")
                && !value.startsWith("https://");
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

    private void runIfActive(Runnable action) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            action.run();
        });
    }

    private void setupDirtyWatchers() {
        TextWatcher watcher = new TextWatcher() {
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
        etTitle.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        etPrice.addTextChangedListener(watcher);
    }

    private void markDirty() {
        if (!isBindingData) {
            hasUnsavedChanges = true;
        }
    }

    private void confirmExitIfDirty(Runnable exitAction) {
        if (!hasUnsavedChanges) {
            exitAction.run();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.save_changes_before_leaving)
                .setPositiveButton(R.string.save, (dialog, which) -> attemptSave())
                .setNegativeButton(R.string.discard, (dialog, which) -> {
                    hasUnsavedChanges = false;
                    exitAction.run();
                })
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private String getCurrentCourseId() {
        Course current = viewModel != null && viewModel.getCourse() != null
                ? viewModel.getCourse().getValue()
                : null;
        return current != null ? current.getId() : null;
    }

    private String resolveCourseStorageId() {
        String courseId = getCurrentCourseId();
        return hasValue(courseId) ? courseId : "draft";
    }

    private String resolveCourseTitle() {
        Course current = viewModel != null && viewModel.getCourse() != null
                ? viewModel.getCourse().getValue()
                : null;
        if (current != null && hasValue(current.getTitle())) {
            return current.getTitle();
        }

        return etTitle != null ? etTitle.getText().toString().trim() : "";
    }

    private String buildCourseMediaFolder(String mediaType) {
        return "courses/" + resolveCourseStorageId() + "/" + mediaType;
    }

    private void showEditorError(String title, String message) {
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

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
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
