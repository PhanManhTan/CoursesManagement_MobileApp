package com.example.myapplication.activities.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.ApiErrorFormatter;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";

    private EditText etFullName;
    private EditText etBio;
    private Button btnSave;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private String currentUserId;
    private boolean isSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        initViews();
        setupListeners();
        loadCurrentUser();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentUser() {
        if (!hasValue(currentUserId)) {
            showProfileError(getString(R.string.profile_unavailable), getString(R.string.missing_user_session_sign_in));
            setFormEnabled(false);
            return;
        }

        setFormEnabled(false);
        userRepository.getById(currentUserId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    bindUser(user);
                    setFormEnabled(true);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setFormEnabled(true);
                    showProfileError(getString(R.string.profile_load_failed), message);
                });
            }
        });
    }

    private void bindUser(User user) {
        if (user == null) {
            showProfileError(getString(R.string.profile_load_failed), getString(R.string.user_not_found));
            return;
        }

        etFullName.setText(valueOrEmpty(user.getFullName()));
        etBio.setText(valueOrEmpty(user.getBio()));
    }

    private void saveProfile() {
        if (isSaving) {
            return;
        }
        if (!hasValue(currentUserId)) {
            showProfileError(getString(R.string.profile_save_failed), getString(R.string.missing_user_session_sign_in));
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.full_name_required));
            etFullName.requestFocus();
            return;
        }

        setSaving(true);
        userRepository.updateProfile(currentUserId, fullName, bio, new UserRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                sessionManager.saveProfile(
                    fullName,
                    valueOrEmpty(sessionManager.getEmail()),
                    valueOrEmpty(bio),
                    valueOrEmpty(sessionManager.getAvatarUrl())
                );
                runOnUiThread(() -> {
                    setSaving(false);
                    Toast.makeText(EditProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setSaving(false);
                    showProfileError(getString(R.string.profile_save_failed), message);
                });
            }
        });
    }

    private void setSaving(boolean saving) {
        isSaving = saving;
        btnSave.setEnabled(!saving);
        btnSave.setAlpha(saving ? 0.5f : 1.0f);
        btnSave.setText(saving ? R.string.saving_upper : R.string.save_changes_upper);
    }

    private void setFormEnabled(boolean enabled) {
        etFullName.setEnabled(enabled);
        etBio.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnSave.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void showProfileError(String title, String message) {
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

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
