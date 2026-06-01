package com.example.myapplication.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.repository.ChapterRepository;
import com.example.myapplication.data.repository.CourseRepository;
import com.example.myapplication.data.repository.LessonRepository;
import com.example.myapplication.data.repository.QuizRepository;
import com.example.myapplication.models.Chapter;
import com.example.myapplication.models.ChapterWithLessons;
import com.example.myapplication.models.Course;
import com.example.myapplication.models.Lesson;
import com.example.myapplication.models.Quiz;
import com.example.myapplication.utils.CourseContentNotifier;
import com.example.myapplication.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EditCourseViewModel extends AndroidViewModel {
    public interface ChapterSaveCallback {
        void onSuccess(Chapter chapter);
        void onError(String message);
    }

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final CourseContentNotifier courseContentNotifier;
    private final SessionManager sessionManager;
    private final MutableLiveData<Course> course = new MutableLiveData<>();
    private final MutableLiveData<List<Lesson>> lessons = new MutableLiveData<>();
    private final MutableLiveData<List<ChapterWithLessons>> chapterDrafts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isStructureLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final Set<String> loadedLessonIds = new HashSet<>();
    private final Set<String> loadedChapterIds = new HashSet<>();
    private String defaultChapterId;

    public EditCourseViewModel(@NonNull Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        chapterRepository = new ChapterRepository(application);
        lessonRepository = new LessonRepository(application);
        quizRepository = new QuizRepository(application);
        courseContentNotifier = new CourseContentNotifier(application);
        sessionManager = new SessionManager(application);
        lessons.setValue(new ArrayList<>());
        chapterDrafts.setValue(new ArrayList<>());
    }

    public void setCourse(Course courseData) {
        if (courseData != null && hasValue(courseData.getId())) {
            String instructorId = sessionManager.getUserId();
            if (!hasValue(instructorId)) {
                errorMessage.setValue("Missing instructor session");
                course.setValue(null);
                loadLessons(null);
                return;
            }

            if (!instructorId.equals(courseData.getInstructorId())) {
                loadCourse(courseData.getId());
                return;
            }
        }

        course.setValue(courseData);
        if (courseData == null || !hasValue(courseData.getId())) {
            loadCourseStructure(null);
        }
    }

    public LiveData<Course> getCourse() {
        return course;
    }

    public LiveData<List<Lesson>> getLessons() {
        return lessons;
    }

    public LiveData<List<ChapterWithLessons>> getChapterDrafts() {
        return chapterDrafts;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsStructureLoading() {
        return isStructureLoading;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public void resetSaveState() {
        saveSuccess.setValue(false);
        errorMessage.setValue(null);
    }

    public void loadCourse(String courseId) {
        String instructorId = sessionManager.getUserId();
        if (!hasValue(instructorId)) {
            errorMessage.setValue("Missing instructor session");
            return;
        }

        courseRepository.getByIdForInstructor(courseId, instructorId, new CourseRepository.RepositoryCallback<Course>() {
            @Override
            public void onSuccess(Course data) {
                course.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void loadLessons(String courseId) {
        loadCourseStructure(courseId);
    }

    public void loadCourseStructure(String courseId) {
        isStructureLoading.setValue(true);
        if (!hasValue(courseId)) {
            defaultChapterId = null;
            loadedLessonIds.clear();
            loadedChapterIds.clear();
            lessons.setValue(new ArrayList<>());
            chapterDrafts.setValue(new ArrayList<>());
            isStructureLoading.setValue(false);
            return;
        }

        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters == null || chapters.isEmpty()) {
                    defaultChapterId = null;
                    loadedLessonIds.clear();
                    loadedChapterIds.clear();
                    lessons.setValue(new ArrayList<>());
                    chapterDrafts.setValue(new ArrayList<>());
                    isStructureLoading.setValue(false);
                    return;
                }

                Collections.sort(chapters, Comparator.comparingInt(Chapter::getOrderIndex));
                defaultChapterId = chapters.get(0).getId();
                loadedLessonIds.clear();
                loadedChapterIds.clear();

                List<ChapterWithLessons> drafts = new ArrayList<>();
                for (Chapter chapter : chapters) {
                    if (hasValue(chapter.getId())) {
                        loadedChapterIds.add(chapter.getId());
                    }
                    drafts.add(new ChapterWithLessons(chapter, new ArrayList<>()));
                }
                loadLessonsForChapterDrafts(drafts, 0);
            }

            @Override
            public void onError(String message) {
                failStructureLoad(message);
            }
        });
    }

    public void saveCourse(String title, String description, double price, String thumbnailUrl) {
        saveCourse(title, description, price, price, thumbnailUrl, new ArrayList<>());
    }

    public void saveCourse(String title, String description, double price, String thumbnailUrl, List<Lesson> lessonList) {
        saveCourse(title, description, price, price, thumbnailUrl, lessonList);
    }

    public void saveCourse(String title, String description, double price, double discountPrice, String thumbnailUrl, List<Lesson> lessonList) {
        Course current = course.getValue();
        if (current == null) {
            current = new Course();
        }

        current.setTitle(title);
        current.setDescription(description);
        current.setPrice(price);
        current.setDiscountPrice(discountPrice);
        current.setThumbnailUrl(thumbnailUrl);
        saveSuccess.setValue(false);

        if (hasValue(current.getId()) && !"tmp".equals(current.getId())) {
            String instructorId = sessionManager.getUserId();
            if (!hasValue(instructorId)) {
                errorMessage.setValue("Missing instructor session");
                return;
            }
            if (hasValue(current.getInstructorId()) && !instructorId.equals(current.getInstructorId())) {
                errorMessage.setValue("You can only edit your own courses");
                return;
            }
            current.setInstructorId(instructorId);
            Course savedCourse = current;
            courseRepository.updateForInstructor(current.getId(), instructorId, current, new CourseRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveLessonsForCourse(savedCourse, lessonList);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                }
            });
        } else {
            if (!prepareNewCourse(current)) {
                return;
            }
            Course newCourse = current;
            courseRepository.insertAndReturn(newCourse, new CourseRepository.RepositoryCallback<Course>() {
                @Override
                public void onSuccess(Course savedCourse) {
                    saveLessonsForCourse(savedCourse, lessonList);
                }

                @Override
                public void onError(String message) {
                    errorMessage.setValue(message);
                }
            });
        }
    }

    public void saveCourseStructure(String title, String description, double price, double discountPrice, String thumbnailUrl,
                                    String categoryId, List<ChapterWithLessons> chapterList) {
        if (Boolean.TRUE.equals(isSaving.getValue())) {
            return;
        }
        if (Boolean.TRUE.equals(isStructureLoading.getValue())) {
            errorMessage.setValue("Course structure is still loading");
            return;
        }
        String lessonVideoError = findLessonVideoError(chapterList);
        if (hasValue(lessonVideoError)) {
            errorMessage.setValue(lessonVideoError);
            return;
        }

        isSaving.setValue(true);
        Course current = course.getValue();
        if (current == null) {
            current = new Course();
        }

        current.setTitle(title);
        current.setDescription(description);
        current.setPrice(price);
        current.setDiscountPrice(discountPrice);
        current.setThumbnailUrl(thumbnailUrl);
        current.setCategoryId(categoryId);
        saveSuccess.setValue(false);

        if (hasValue(current.getId()) && !"tmp".equals(current.getId())) {
            String instructorId = sessionManager.getUserId();
            if (!hasValue(instructorId)) {
                failSave("Missing instructor session");
                return;
            }
            if (hasValue(current.getInstructorId()) && !instructorId.equals(current.getInstructorId())) {
                failSave("You can only edit your own courses");
                return;
            }
            current.setInstructorId(instructorId);
            Course savedCourse = current;
            courseRepository.updateForInstructor(current.getId(), instructorId, current, new CourseRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveChapterStructure(savedCourse, chapterList);
                }

                @Override
                public void onError(String message) {
                    failSave(message);
                }
            });
        } else {
            if (!prepareNewCourse(current)) {
                finishSaving();
                return;
            }
            Course newCourse = current;
            courseRepository.insertAndReturn(newCourse, new CourseRepository.RepositoryCallback<Course>() {
                @Override
                public void onSuccess(Course savedCourse) {
                    if (!applySavedCourseIdentity(newCourse, savedCourse)) {
                        failSave("Course was created but no course id was returned");
                        return;
                    }
                    saveChapterStructure(newCourse, chapterList);
                }

                @Override
                public void onError(String message) {
                    failSave(message);
                }
            });
        }
    }

    public void saveChapterDraft(Chapter chapter, int orderIndex, ChapterSaveCallback callback) {
        Course current = course.getValue();
        if (current == null || !hasValue(current.getId())) {
            callback.onError("Save the course before adding lessons to this chapter.");
            return;
        }
        if (chapter == null) {
            callback.onError("Chapter data is missing");
            return;
        }

        chapter.setCourseId(current.getId());
        chapter.setOrderIndex(orderIndex);
        if (!hasValue(chapter.getTitle())) {
            chapter.setTitle("Chapter " + orderIndex);
        }

        if (hasValue(chapter.getId())) {
            chapterRepository.update(chapter.getId(), chapter, new ChapterRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadedChapterIds.add(chapter.getId());
                    callback.onSuccess(chapter);
                }

                @Override
                public void onError(String message) {
                    if (isMissingChapterOnServer(message)) {
                        chapter.setId(null);
                        saveChapterDraft(chapter, orderIndex, callback);
                        return;
                    }
                    callback.onError(message);
                }
            });
            return;
        }

        chapterRepository.insertAndReturn(chapter, new ChapterRepository.RepositoryCallback<Chapter>() {
            @Override
            public void onSuccess(Chapter savedChapter) {
                if (savedChapter == null || !hasValue(savedChapter.getId())) {
                    callback.onError("Chapter was created but no chapter id was returned");
                    return;
                }

                chapter.setId(savedChapter.getId());
                if (hasValue(savedChapter.getCourseId())) {
                    chapter.setCourseId(savedChapter.getCourseId());
                }
                if (hasValue(savedChapter.getTitle())) {
                    chapter.setTitle(savedChapter.getTitle());
                }
                if (savedChapter.getOrderIndex() > 0) {
                    chapter.setOrderIndex(savedChapter.getOrderIndex());
                }
                loadedChapterIds.add(savedChapter.getId());
                courseContentNotifier.notifyChapterAdded(current, chapter);
                callback.onSuccess(chapter);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void loadLessonsForChapterDrafts(List<ChapterWithLessons> drafts, int index) {
        if (index >= drafts.size()) {
            chapterDrafts.setValue(drafts);
            lessons.setValue(flattenLessons(drafts));
            isStructureLoading.setValue(false);
            return;
        }

        ChapterWithLessons draft = drafts.get(index);
        Chapter chapter = draft.getChapter();
        if (chapter == null || !hasValue(chapter.getId())) {
            loadLessonsForChapterDrafts(drafts, index + 1);
            return;
        }

        lessonRepository.getByChapterId(chapter.getId(), new LessonRepository.RepositoryCallback<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> data) {
                List<Lesson> loadedLessons = data != null ? data : new ArrayList<>();
                Collections.sort(loadedLessons, Comparator.comparingInt(Lesson::getOrderIndex));
                loadQuizzesForLessons(loadedLessons, 0, () -> {
                    for (Lesson lesson : loadedLessons) {
                        restoreLessonUiState(lesson);
                        if (hasValue(lesson.getId())) {
                            loadedLessonIds.add(lesson.getId());
                        }
                    }
                    draft.setLessons(loadedLessons);
                    loadLessonsForChapterDrafts(drafts, index + 1);
                });
            }

            @Override
            public void onError(String message) {
                failStructureLoad(message);
            }
        });
    }

    private List<Lesson> flattenLessons(List<ChapterWithLessons> drafts) {
        List<Lesson> flattened = new ArrayList<>();
        if (drafts == null) {
            return flattened;
        }
        for (ChapterWithLessons draft : drafts) {
            if (draft != null) {
                flattened.addAll(draft.getLessons());
            }
        }
        return flattened;
    }

    private void loadLessonsByChapter(String chapterId) {
        lessonRepository.getByChapterId(chapterId, new LessonRepository.RepositoryCallback<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> data) {
                List<Lesson> loadedLessons = data != null ? data : new ArrayList<>();
                Collections.sort(loadedLessons, Comparator.comparingInt(Lesson::getOrderIndex));
                loadedLessonIds.clear();
                loadQuizzesForLessons(loadedLessons, 0, () -> {
                    for (Lesson lesson : loadedLessons) {
                        restoreLessonUiState(lesson);
                        if (hasValue(lesson.getId())) {
                            loadedLessonIds.add(lesson.getId());
                        }
                    }
                    lessons.setValue(loadedLessons);
                });
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private void loadQuizzesForLessons(List<Lesson> loadedLessons, int index, Runnable onComplete) {
        if (loadedLessons == null || index >= loadedLessons.size()) {
            onComplete.run();
            return;
        }

        Lesson lesson = loadedLessons.get(index);
        if (lesson == null || !hasValue(lesson.getId())) {
            loadQuizzesForLessons(loadedLessons, index + 1, onComplete);
            return;
        }

        quizRepository.getByLessonId(lesson.getId(), new QuizRepository.RepositoryCallback<List<Quiz>>() {
            @Override
            public void onSuccess(List<Quiz> data) {
                if (data != null && !data.isEmpty()) {
                    attachQuizzesToLessonContent(lesson, data);
                }
                loadQuizzesForLessons(loadedLessons, index + 1, onComplete);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Quiz load failed: " + message);
                loadQuizzesForLessons(loadedLessons, index + 1, onComplete);
            }
        });
    }

    private boolean prepareNewCourse(Course courseData) {
        String instructorId = sessionManager.getUserId();
        if (!hasValue(instructorId)) {
            errorMessage.setValue("Missing instructor session");
            return false;
        }

        courseData.setId(null);
        courseData.setInstructorId(instructorId);
        if (!hasValue(courseData.getStatus())) {
            courseData.setStatus("pending");
        }
        if (!hasValue(courseData.getCreatedAt())) {
            courseData.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        }
        return true;
    }

    private String findLessonVideoError(List<ChapterWithLessons> chapterList) {
        if (chapterList == null) {
            return "";
        }

        for (ChapterWithLessons draft : chapterList) {
            if (draft == null) continue;

            for (Lesson lesson : draft.getLessons()) {
                if (lesson == null) continue;

                if (!hasValue(lesson.getLocalVideoUri()) && !hasValue(lesson.getVideoUrl())) {
                    String title = hasValue(lesson.getTitle()) ? lesson.getTitle().trim() : "this lesson";
                    return "Add a video before saving \"" + title + "\". Attachments and quizzes are optional.";
                }
            }
        }
        return "";
    }

    private boolean applySavedCourseIdentity(Course target, Course savedCourse) {
        if (target == null || savedCourse == null || !hasValue(savedCourse.getId())) {
            return false;
        }

        target.setId(savedCourse.getId());
        if (hasValue(savedCourse.getInstructorId())) {
            target.setInstructorId(savedCourse.getInstructorId());
        }
        if (hasValue(savedCourse.getStatus())) {
            target.setStatus(savedCourse.getStatus());
        }
        if (hasValue(savedCourse.getCreatedAt())) {
            target.setCreatedAt(savedCourse.getCreatedAt());
        }
        if (hasValue(savedCourse.getCategoryId())) {
            target.setCategoryId(savedCourse.getCategoryId());
        }
        return true;
    }

    private boolean applySavedChapterIdentity(Chapter target, Chapter savedChapter) {
        if (target == null || savedChapter == null || !hasValue(savedChapter.getId())) {
            return false;
        }

        target.setId(savedChapter.getId());
        if (hasValue(savedChapter.getCourseId())) {
            target.setCourseId(savedChapter.getCourseId());
        }
        if (hasValue(savedChapter.getTitle())) {
            target.setTitle(savedChapter.getTitle());
        }
        if (savedChapter.getOrderIndex() > 0) {
            target.setOrderIndex(savedChapter.getOrderIndex());
        }
        return true;
    }

    private boolean applySavedLessonIdentity(Lesson target, Lesson savedLesson) {
        if (target == null || savedLesson == null || !hasValue(savedLesson.getId())) {
            return false;
        }

        target.setId(savedLesson.getId());
        if (hasValue(savedLesson.getChapterId())) {
            target.setChapterId(savedLesson.getChapterId());
        }
        if (savedLesson.getOrderIndex() > 0) {
            target.setOrderIndex(savedLesson.getOrderIndex());
        }
        return true;
    }

    private boolean isMissingChapterOnServer(String message) {
        return hasValue(message)
                && message.toLowerCase(Locale.US).contains("chapter not found on server");
    }

    private boolean isMissingLessonOnServer(String message) {
        return hasValue(message)
                && message.toLowerCase(Locale.US).contains("lesson not found on server");
    }

    private void saveChapterStructure(Course savedCourse, List<ChapterWithLessons> chapterList) {
        if (savedCourse == null || !hasValue(savedCourse.getId())) {
            failSave("Course id is missing, cannot save chapters");
            return;
        }

        List<ChapterWithLessons> chaptersToSave = normalizeChapterDrafts(savedCourse.getId(), chapterList);
        Set<String> keptChapterIds = new HashSet<>();
        Set<String> keptLessonIds = new HashSet<>();
        for (ChapterWithLessons draft : chaptersToSave) {
            Chapter chapter = draft.getChapter();
            if (chapter != null && hasValue(chapter.getId())) {
                keptChapterIds.add(chapter.getId());
            }
            for (Lesson lesson : draft.getLessons()) {
                if (hasValue(lesson.getId())) {
                    keptLessonIds.add(lesson.getId());
                }
            }
        }

        List<String> removedLessonIds = new ArrayList<>();
        for (String loadedId : loadedLessonIds) {
            if (!keptLessonIds.contains(loadedId)) {
                removedLessonIds.add(loadedId);
            }
        }

        List<String> removedChapterIds = new ArrayList<>();
        for (String loadedId : loadedChapterIds) {
            if (!keptChapterIds.contains(loadedId)) {
                removedChapterIds.add(loadedId);
            }
        }

        deleteRemovedLessons(removedLessonIds, 0, () ->
                deleteRemovedChapters(removedChapterIds, 0, () -> {
                    if (chaptersToSave.isEmpty()) {
                        loadedChapterIds.clear();
                        loadedLessonIds.clear();
                        chapterDrafts.setValue(new ArrayList<>());
                        lessons.setValue(new ArrayList<>());
                        completeSave();
                        return;
                    }

                    saveChapterAt(savedCourse, chaptersToSave, 0);
                }));
    }

    private List<ChapterWithLessons> normalizeChapterDrafts(String courseId, List<ChapterWithLessons> chapterList) {
        List<ChapterWithLessons> normalized = new ArrayList<>();
        if (chapterList == null) {
            return normalized;
        }

        for (int i = 0; i < chapterList.size(); i++) {
            ChapterWithLessons draft = chapterList.get(i);
            if (draft == null) continue;

            Chapter chapter = draft.getChapter();
            if (chapter == null) {
                chapter = new Chapter();
                draft.setChapter(chapter);
            }
            chapter.setCourseId(courseId);
            chapter.setOrderIndex(i + 1);
            if (!hasValue(chapter.getTitle())) {
                chapter.setTitle("Chapter " + (i + 1));
            }

            List<Lesson> chapterLessons = new ArrayList<>();
            for (Lesson lesson : draft.getLessons()) {
                if (lesson != null) {
                    chapterLessons.add(lesson);
                }
            }
            draft.setLessons(chapterLessons);
            normalized.add(draft);
        }
        return normalized;
    }

    private void deleteRemovedChapters(List<String> removedChapterIds, int index, Runnable onComplete) {
        if (index >= removedChapterIds.size()) {
            onComplete.run();
            return;
        }

        chapterRepository.delete(removedChapterIds.get(index), new ChapterRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                deleteRemovedChapters(removedChapterIds, index + 1, onComplete);
            }

            @Override
            public void onError(String message) {
                failSave(message);
            }
        });
    }

    private void saveChapterAt(Course savedCourse, List<ChapterWithLessons> chaptersToSave, int index) {
        if (index >= chaptersToSave.size()) {
            loadedChapterIds.clear();
            loadedLessonIds.clear();
            for (ChapterWithLessons draft : chaptersToSave) {
                Chapter chapter = draft.getChapter();
                if (chapter != null && hasValue(chapter.getId())) {
                    loadedChapterIds.add(chapter.getId());
                }
                for (Lesson lesson : draft.getLessons()) {
                    if (hasValue(lesson.getId())) {
                        loadedLessonIds.add(lesson.getId());
                    }
                }
            }
            chapterDrafts.setValue(new ArrayList<>(chaptersToSave));
            lessons.setValue(flattenLessons(chaptersToSave));
            completeSave();
            return;
        }

        ChapterWithLessons draft = chaptersToSave.get(index);
        Chapter chapter = draft.getChapter();
        chapter.setCourseId(savedCourse.getId());
        chapter.setOrderIndex(index + 1);

        if (hasValue(chapter.getId())) {
            chapterRepository.update(chapter.getId(), chapter, new ChapterRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveLessonsForChapter(savedCourse, chapter, draft.getLessons(), 0,
                            () -> publishChapterDrafts(chaptersToSave),
                            () -> saveChapterAt(savedCourse, chaptersToSave, index + 1));
                }

                @Override
                public void onError(String message) {
                    if (isMissingChapterOnServer(message)) {
                        chapter.setId(null);
                        saveChapterAt(savedCourse, chaptersToSave, index);
                        return;
                    }
                    failSave(message);
                }
            });
        } else {
            chapterRepository.insertAndReturn(chapter, new ChapterRepository.RepositoryCallback<Chapter>() {
                @Override
                public void onSuccess(Chapter savedChapter) {
                    if (!applySavedChapterIdentity(chapter, savedChapter)) {
                        failSave("Chapter was created but no chapter id was returned");
                        return;
                    }
                    courseContentNotifier.notifyChapterAdded(savedCourse, chapter);
                    publishChapterDrafts(chaptersToSave);
                    saveLessonsForChapter(savedCourse, chapter, draft.getLessons(), 0,
                            () -> publishChapterDrafts(chaptersToSave),
                            () -> saveChapterAt(savedCourse, chaptersToSave, index + 1));
                }

                @Override
                public void onError(String message) {
                    failSave(message);
                }
            });
        }
    }

    private void saveLessonsForChapter(Course savedCourse, Chapter chapter, List<Lesson> chapterLessons, int lessonIndex,
                                       Runnable onDraftChanged, Runnable onComplete) {
        if (chapterLessons == null || lessonIndex >= chapterLessons.size()) {
            onComplete.run();
            return;
        }

        Lesson lesson = chapterLessons.get(lessonIndex);
        if (lesson != null && hasValue(lesson.getId()) && loadedLessonIds.contains(lesson.getId())) {
            saveLessonsForChapter(savedCourse, chapter, chapterLessons, lessonIndex + 1, onDraftChanged, onComplete);
            return;
        }

        if (!prepareLessonForWrite(lesson, chapter, lessonIndex)) {
            finishSaving();
            return;
        }

        if (hasValue(lesson.getId())) {
            normalizeLessonQuizzes(lesson, lesson.getId());
            lessonRepository.update(lesson.getId(), lesson, new LessonRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveQuizzesForLesson(lesson, () ->
                            saveLessonsForChapter(savedCourse, chapter, chapterLessons, lessonIndex + 1, onDraftChanged, onComplete));
                }

                @Override
                public void onError(String message) {
                    if (isMissingLessonOnServer(message)) {
                        lesson.setId(null);
                        saveLessonsForChapter(savedCourse, chapter, chapterLessons, lessonIndex, onDraftChanged, onComplete);
                        return;
                    }
                    failSave(message);
                }
            });
            return;
        }

        lessonRepository.insertAndReturn(lesson, new LessonRepository.RepositoryCallback<Lesson>() {
            @Override
            public void onSuccess(Lesson savedLesson) {
                if (!applySavedLessonIdentity(lesson, savedLesson)) {
                    failSave("Lesson was created but no lesson id was returned");
                    return;
                }
                onDraftChanged.run();
                updateInsertedLessonContentAndQuizzes(lesson, () -> {
                    courseContentNotifier.notifyLessonAdded(savedCourse, lesson);
                    saveLessonsForChapter(savedCourse, chapter, chapterLessons, lessonIndex + 1, onDraftChanged, onComplete);
                });
            }

            @Override
            public void onError(String message) {
                failSave(message);
            }
        });
    }

    private void publishChapterDrafts(List<ChapterWithLessons> drafts) {
        if (drafts == null) {
            return;
        }
        chapterDrafts.setValue(new ArrayList<>(drafts));
        lessons.setValue(flattenLessons(drafts));
    }

    private void saveLessonsForCourse(Course savedCourse, List<Lesson> lessonList) {
        if (savedCourse == null || !hasValue(savedCourse.getId())) {
            failSave("Course id is missing, cannot save lessons");
            return;
        }

        List<Lesson> lessonsToSave = lessonList != null ? new ArrayList<>(lessonList) : new ArrayList<>();
        Set<String> keptLessonIds = new HashSet<>();
        for (Lesson lesson : lessonsToSave) {
            if (hasValue(lesson.getId())) {
                keptLessonIds.add(lesson.getId());
            }
        }

        List<String> removedLessonIds = new ArrayList<>();
        for (String loadedId : loadedLessonIds) {
            if (!keptLessonIds.contains(loadedId)) {
                removedLessonIds.add(loadedId);
            }
        }

        deleteRemovedLessons(removedLessonIds, 0, () -> {
            if (lessonsToSave.isEmpty()) {
                loadedLessonIds.clear();
                lessons.setValue(new ArrayList<>());
                completeSave();
                return;
            }

            ensureDefaultChapter(savedCourse.getId(), new ChapterRepository.RepositoryCallback<Chapter>() {
                @Override
                public void onSuccess(Chapter chapter) {
                    saveLessonAt(savedCourse, chapter, lessonsToSave, 0);
                }

                @Override
                public void onError(String message) {
                    failSave(message);
                }
            });
        });
    }

    private void ensureDefaultChapter(String courseId, ChapterRepository.RepositoryCallback<Chapter> callback) {
        if (hasValue(defaultChapterId)) {
            callback.onSuccess(new Chapter(defaultChapterId, courseId, "Lessons", 1));
            return;
        }

        chapterRepository.getByCourseId(courseId, new ChapterRepository.RepositoryCallback<List<Chapter>>() {
            @Override
            public void onSuccess(List<Chapter> chapters) {
                if (chapters != null && !chapters.isEmpty()) {
                    Collections.sort(chapters, Comparator.comparingInt(Chapter::getOrderIndex));
                    Chapter chapter = chapters.get(0);
                    defaultChapterId = chapter.getId();
                    callback.onSuccess(chapter);
                    return;
                }

                Chapter chapter = new Chapter(null, courseId, "Lessons", 1);
                chapterRepository.insertAndReturn(chapter, new ChapterRepository.RepositoryCallback<Chapter>() {
                    @Override
                    public void onSuccess(Chapter data) {
                        defaultChapterId = data.getId();
                        callback.onSuccess(data);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void deleteRemovedLessons(List<String> removedLessonIds, int index, Runnable onComplete) {
        if (index >= removedLessonIds.size()) {
            onComplete.run();
            return;
        }

        String lessonId = removedLessonIds.get(index);
        quizRepository.deleteByLessonId(lessonId, new QuizRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                lessonRepository.delete(lessonId, new LessonRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        deleteRemovedLessons(removedLessonIds, index + 1, onComplete);
                    }

                    @Override
                    public void onError(String message) {
                        failSave(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                failSave(message);
            }
        });
    }

    private void saveLessonAt(Course savedCourse, Chapter chapter, List<Lesson> lessonsToSave, int index) {
        if (index >= lessonsToSave.size()) {
            loadedLessonIds.clear();
            for (Lesson lesson : lessonsToSave) {
                if (hasValue(lesson.getId())) {
                    loadedLessonIds.add(lesson.getId());
                }
            }
            lessons.setValue(new ArrayList<>(lessonsToSave));
            completeSave();
            return;
        }

        Lesson lesson = lessonsToSave.get(index);
        if (!prepareLessonForWrite(lesson, chapter, index)) {
            finishSaving();
            return;
        }

        if (hasValue(lesson.getId())) {
            normalizeLessonQuizzes(lesson, lesson.getId());
            lessonRepository.update(lesson.getId(), lesson, new LessonRepository.RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    saveQuizzesForLesson(lesson, () -> saveLessonAt(savedCourse, chapter, lessonsToSave, index + 1));
                }

                @Override
                public void onError(String message) {
                    if (isMissingLessonOnServer(message)) {
                        lesson.setId(null);
                        saveLessonAt(savedCourse, chapter, lessonsToSave, index);
                        return;
                    }
                    failSave(message);
                }
            });
        } else {
            lessonRepository.insertAndReturn(lesson, new LessonRepository.RepositoryCallback<Lesson>() {
                @Override
                public void onSuccess(Lesson savedLesson) {
                    if (!applySavedLessonIdentity(lesson, savedLesson)) {
                        failSave("Lesson was created but no lesson id was returned");
                        return;
                    }
                    updateInsertedLessonContentAndQuizzes(lesson, () -> {
                        courseContentNotifier.notifyLessonAdded(savedCourse, lesson);
                        saveLessonAt(savedCourse, chapter, lessonsToSave, index + 1);
                    });
                }

                @Override
                public void onError(String message) {
                    failSave(message);
                }
            });
        }
    }

    private void updateInsertedLessonContentAndQuizzes(Lesson lesson, Runnable onComplete) {
        if (lesson == null || !hasValue(lesson.getId())) {
            failSave("Lesson id is missing, cannot save quizzes");
            return;
        }

        normalizeLessonQuizzes(lesson, lesson.getId());
        lessonRepository.update(lesson.getId(), lesson, new LessonRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                saveQuizzesForLesson(lesson, onComplete);
            }

            @Override
            public void onError(String message) {
                failSave(message);
            }
        });
    }

    private void saveQuizzesForLesson(Lesson lesson, Runnable onComplete) {
        if (lesson == null || !hasValue(lesson.getId())) {
            onComplete.run();
            return;
        }

        List<Quiz> quizzes = extractQuizzesFromLesson(lesson);
        attachQuizzesToLessonContent(lesson, quizzes);
        quizRepository.replaceForLesson(lesson.getId(), quizzes, new QuizRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                onComplete.run();
            }

            @Override
            public void onError(String message) {
                failSave(message);
            }
        });
    }

    private boolean prepareLessonForWrite(Lesson lesson, Chapter chapter, int index) {
        if (lesson == null) {
            errorMessage.setValue("Lesson data is missing");
            return false;
        }
        if (chapter == null || !hasValue(chapter.getId())) {
            errorMessage.setValue("Chapter id is missing, cannot save lesson");
            return false;
        }

        lesson.setChapterId(chapter.getId());
        if (!hasValue(lesson.getTitle())) {
            lesson.setTitle("Lesson " + (index + 1));
        }
        if (!hasValue(lesson.getLocalVideoUri()) && !hasValue(lesson.getVideoUrl())) {
            errorMessage.setValue("Add a video before saving \"" + lesson.getTitle() + "\". Attachments and quizzes are optional.");
            return false;
        }
        lesson.setOrderIndex(index + 1);
        if (hasValue(lesson.getLocalVideoUri())) {
            lesson.setVideoUrl(lesson.getLocalVideoUri());
        }
        if (!lesson.getLocalFileUris().isEmpty()) {
            lesson.setDocumentUrl(joinValues(lesson.getLocalFileUris()));
        }
        lesson.setContentType(resolveContentType(lesson));
        lesson.setContent(buildLessonContent(lesson));
        return true;
    }

    private String resolveContentType(Lesson lesson) {
        if (hasValue(lesson.getLocalVideoUri()) || hasValue(lesson.getVideoUrl())) {
            return "video";
        }
        if (!lesson.getLocalFileUris().isEmpty() || hasValue(lesson.getDocumentUrl())) {
            return "document";
        }
        return "video";
    }

    private void restoreLessonUiState(Lesson lesson) {
        lesson.clearLocalVideoFile();
        lesson.clearLocalThumbnailFile();
        lesson.getLocalFileNames().clear();
        lesson.getLocalFileUris().clear();

        Map<String, Object> content = getContentMap(lesson.getContent());
        if (content != null) {
            String videoName = getMapString(content, "video_name");
            String videoUrl = getMapString(content, "video_url");
            if (hasValue(videoUrl) || hasValue(videoName)) {
                lesson.setLocalVideoFile(
                        hasValue(videoName) ? videoName : extractFileName(videoUrl),
                        videoUrl
                );
            }

            String thumbnailName = getMapString(content, "thumbnail_name");
            String thumbnailUrl = getMapString(content, "thumbnail_url");
            if (hasValue(thumbnailUrl) || hasValue(thumbnailName)) {
                lesson.setLocalThumbnailFile(
                        hasValue(thumbnailName) ? thumbnailName : extractFileName(thumbnailUrl),
                        thumbnailUrl
                );
            }

            restoreFilesFromContent(lesson, content.get("files"));
        }

        if (!hasValue(lesson.getLocalVideoUri()) && hasValue(lesson.getVideoUrl())) {
            lesson.setLocalVideoFile(extractFileName(lesson.getVideoUrl()), lesson.getVideoUrl());
        }
        if (lesson.getLocalFileNames().isEmpty() && hasValue(lesson.getDocumentUrl())) {
            String[] uris = lesson.getDocumentUrl().split("\\n");
            for (String uri : uris) {
                if (hasValue(uri)) {
                    lesson.addLocalFile(extractFileName(uri), uri);
                }
            }
        }
    }

    private String extractFileName(String value) {
        if (!hasValue(value)) return "Selected file";

        int slashIndex = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < value.length() - 1) {
            return value.substring(slashIndex + 1);
        }
        return value;
    }

    private Map<String, Object> buildLessonContent(Lesson lesson) {
        Map<String, Object> previousContent = getContentMap(lesson.getContent());
        Map<String, Object> content = new HashMap<>();
        content.put("managed_by_instructor_editor", true);
        content.put("description", valueOrEmpty(getMapString(previousContent, "description")));
        content.put("video_name", valueOrEmpty(lesson.getLocalVideoName()));
        content.put("video_url", valueOrEmpty(hasValue(lesson.getLocalVideoUri())
                ? lesson.getLocalVideoUri()
                : lesson.getVideoUrl()));
        content.put("thumbnail_name", valueOrEmpty(lesson.getLocalThumbnailName()));
        content.put("thumbnail_url", valueOrEmpty(lesson.getLocalThumbnailUri()));
        content.put("quizzes", buildQuizPayload(
                extractQuizzesFromContent(previousContent),
                lesson.getId()
        ));

        List<Map<String, String>> files = new ArrayList<>();
        List<String> names = lesson.getLocalFileNames();
        List<String> uris = lesson.getLocalFileUris();
        for (int i = 0; i < uris.size(); i++) {
            String uri = uris.get(i);
            if (!hasValue(uri)) continue;

            Map<String, String> file = new HashMap<>();
            String name = i < names.size() ? names.get(i) : extractFileName(uri);
            file.put("name", hasValue(name) ? name : extractFileName(uri));
            file.put("url", uri);
            files.add(file);
        }
        content.put("files", files);
        return content;
    }

    private void normalizeLessonQuizzes(Lesson lesson, String lessonId) {
        List<Quiz> quizzes = extractQuizzesFromLesson(lesson);
        attachQuizzesToLessonContent(lesson, quizzes, lessonId);
    }

    private void attachQuizzesToLessonContent(Lesson lesson, List<Quiz> quizzes) {
        attachQuizzesToLessonContent(lesson, quizzes, lesson != null ? lesson.getId() : null);
    }

    private void attachQuizzesToLessonContent(Lesson lesson, List<Quiz> quizzes, String lessonId) {
        if (lesson == null) return;

        Map<String, Object> existingContent = getContentMap(lesson.getContent());
        Map<String, Object> content = existingContent != null
                ? new HashMap<>(existingContent)
                : new HashMap<>();
        content.put("managed_by_instructor_editor", true);
        content.put("quizzes", buildQuizPayload(quizzes, lessonId));
        lesson.setContent(content);
    }

    private List<Map<String, Object>> buildQuizPayload(List<Quiz> quizzes, String lessonId) {
        List<Map<String, Object>> payload = new ArrayList<>();
        if (quizzes == null) {
            return payload;
        }

        for (Quiz quiz : quizzes) {
            if (quiz == null || !hasValue(quiz.getQuestion())) {
                continue;
            }

            String resolvedLessonId = hasValue(lessonId) ? lessonId : quiz.getLessonId();
            quiz.setLessonId(resolvedLessonId);

            Map<String, Object> item = new HashMap<>();
            if (hasValue(quiz.getId())) {
                item.put("id", quiz.getId());
            }
            item.put("lesson_id", valueOrEmpty(resolvedLessonId));
            item.put("question", valueOrEmpty(quiz.getQuestion()));
            item.put("options", normalizeOptions(quiz.getOptions()));
            item.put("correct_answer", valueOrEmpty(quiz.getCorrectAnswer()));
            payload.add(item);
        }
        return payload;
    }

    private List<Quiz> extractQuizzesFromLesson(Lesson lesson) {
        if (lesson == null) {
            return new ArrayList<>();
        }
        return extractQuizzesFromContent(getContentMap(lesson.getContent()));
    }

    private List<Quiz> extractQuizzesFromContent(Map<String, Object> content) {
        List<Quiz> quizzes = new ArrayList<>();
        Object quizzesValue = content != null ? content.get("quizzes") : null;
        if (!(quizzesValue instanceof List)) {
            return quizzes;
        }

        for (Object quizValue : (List<?>) quizzesValue) {
            Quiz quiz = toQuiz(quizValue);
            if (quiz != null && hasValue(quiz.getQuestion())) {
                quizzes.add(quiz);
            }
        }
        return quizzes;
    }

    @SuppressWarnings("unchecked")
    private Quiz toQuiz(Object quizValue) {
        if (quizValue instanceof Quiz) {
            Quiz source = (Quiz) quizValue;
            return new Quiz(
                    source.getId(),
                    source.getLessonId(),
                    source.getQuestion(),
                    normalizeOptions(source.getOptions()),
                    source.getCorrectAnswer()
            );
        }
        if (!(quizValue instanceof Map)) {
            return null;
        }

        Map<String, Object> quizMap = (Map<String, Object>) quizValue;
        Quiz quiz = new Quiz();
        quiz.setId(getMapString(quizMap, "id"));
        quiz.setLessonId(getMapString(quizMap, "lesson_id"));
        quiz.setQuestion(getMapString(quizMap, "question"));
        quiz.setOptions(normalizeOptions(quizMap.get("options")));
        quiz.setCorrectAnswer(getMapString(quizMap, "correct_answer"));
        return quiz;
    }

    private List<String> normalizeOptions(Object optionsValue) {
        List<String> options = new ArrayList<>();
        if (optionsValue instanceof List) {
            for (Object value : (List<?>) optionsValue) {
                options.add(value != null ? String.valueOf(value) : "");
            }
        } else if (optionsValue instanceof String && hasValue((String) optionsValue)) {
            options.add((String) optionsValue);
        }
        return options;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getContentMap(Object content) {
        if (content instanceof Map) {
            return (Map<String, Object>) content;
        }
        if (content instanceof String && hasValue((String) content)) {
            return parseContentString((String) content);
        }
        return null;
    }

    private Map<String, Object> parseContentString(String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject root = new JSONObject(content);
            result.put("description", root.optString("description", ""));
            result.put("video_name", root.optString("video_name", ""));
            result.put("video_url", root.optString("video_url", root.optString("video_uri", "")));
            result.put("thumbnail_name", root.optString("thumbnail_name", ""));
            result.put("thumbnail_url", root.optString("thumbnail_url", root.optString("thumbnail_uri", "")));

            JSONArray jsonFiles = root.optJSONArray("files");
            List<Map<String, String>> files = new ArrayList<>();
            if (jsonFiles != null) {
                for (int i = 0; i < jsonFiles.length(); i++) {
                    JSONObject jsonFile = jsonFiles.optJSONObject(i);
                    if (jsonFile == null) continue;

                    Map<String, String> file = new HashMap<>();
                    file.put("name", jsonFile.optString("name", ""));
                    file.put("url", jsonFile.optString("url", jsonFile.optString("uri", "")));
                    files.add(file);
                }
            }
            result.put("files", files);

            JSONArray jsonQuizzes = root.optJSONArray("quizzes");
            List<Map<String, Object>> quizzes = new ArrayList<>();
            if (jsonQuizzes != null) {
                for (int i = 0; i < jsonQuizzes.length(); i++) {
                    JSONObject jsonQuiz = jsonQuizzes.optJSONObject(i);
                    if (jsonQuiz == null) continue;

                    Map<String, Object> quiz = new HashMap<>();
                    quiz.put("id", jsonQuiz.optString("id", ""));
                    quiz.put("lesson_id", jsonQuiz.optString("lesson_id", ""));
                    quiz.put("question", jsonQuiz.optString("question", ""));
                    quiz.put("correct_answer", jsonQuiz.optString("correct_answer", ""));

                    JSONArray jsonOptions = jsonQuiz.optJSONArray("options");
                    List<String> options = new ArrayList<>();
                    if (jsonOptions != null) {
                        for (int optionIndex = 0; optionIndex < jsonOptions.length(); optionIndex++) {
                            options.add(jsonOptions.optString(optionIndex, ""));
                        }
                    }
                    quiz.put("options", options);
                    quizzes.add(quiz);
                }
            }
            result.put("quizzes", quizzes);
        } catch (JSONException ignored) {
            return null;
        }
        return result;
    }

    private void restoreFilesFromContent(Lesson lesson, Object filesValue) {
        if (!(filesValue instanceof List)) {
            return;
        }

        List<?> files = (List<?>) filesValue;
        for (Object fileValue : files) {
            if (!(fileValue instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> file = (Map<String, Object>) fileValue;
            String url = getMapString(file, "url");
            if (!hasValue(url)) {
                url = getMapString(file, "uri");
            }
            String name = getMapString(file, "name");
            if (hasValue(url) || hasValue(name)) {
                lesson.addLocalFile(hasValue(name) ? name : extractFileName(url), url);
            }
        }
    }

    private String getMapString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "";
        }
        return String.valueOf(map.get(key));
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

    private void failStructureLoad(String message) {
        errorMessage.setValue(message);
        isStructureLoading.setValue(false);
    }

    private void completeSave() {
        isSaving.setValue(false);
        saveSuccess.setValue(true);
    }

    private void failSave(String message) {
        errorMessage.setValue(message);
        finishSaving();
    }

    private void finishSaving() {
        isSaving.setValue(false);
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
