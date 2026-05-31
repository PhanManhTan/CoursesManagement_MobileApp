package com.example.myapplication.activities.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.data.repository.SupabaseStorageRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.ApiErrorFormatter;
import com.example.myapplication.utils.BottomNavigationHelper;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvBio;
    private TextView tvAccountTitle;
    private TextView tvBioLabel;
    private TextView tvLanguageLabel;
    private TextView tvLanguageHint;
    private ImageView ivAvatar;
    private ImageView btnUpdateAvatar;
    private Button btnEditProfile;
    private Button btnLogout;
    private BottomNavigationView bottomNav;

    private UserRepository userRepository;
    private SupabaseStorageRepository storageRepository;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Hiển thị ảnh tạm thời
                    ivAvatar.setImageURI(uri);
                    // Tiến hành upload
                    uploadImageToSupabaseStorage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        userRepository = new UserRepository(this);
        storageRepository = new SupabaseStorageRepository(this);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBio = findViewById(R.id.tvBio);
        tvAccountTitle = findViewById(R.id.tvAccountTitle);
        tvBioLabel = findViewById(R.id.tvBioLabel);
        tvLanguageLabel = findViewById(R.id.tvLanguageLabel);
        tvLanguageHint = findViewById(R.id.tvLanguageHint);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnUpdateAvatar = findViewById(R.id.btnUpdateAvatar);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnUpdateAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        setupLanguageSpinner();
        applyLanguageText();
        bindCachedProfile();

        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_account);
            BottomNavigationHelper.setupBottomNavigation(this, bottomNav);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        String userId = sessionManager.getUserId();
        if (!hasValue(userId)) {
            redirectToLogin();
            return;
        }

        if (bindCachedProfile()) {
            if (sessionManager.isProfileLoadedMemory()) {
                return;
            }
        } else {
            tvFullName.setText(R.string.loading);
            tvEmail.setText("");
            tvBio.setText(R.string.loading);
        }

        userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    sessionManager.saveProfile(user.getFullName(), user.getEmail(), user.getBio(), user.getAvatarUrl());
                }
                runOnUiThread(() -> bindUser(user));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Profile load failed: " + message);
                    Toast.makeText(AccountActivity.this, ApiErrorFormatter.fromMessage(message), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void bindUser(User user) {
        if (user == null) {
            tvFullName.setText(R.string.unnamed_user);
            tvEmail.setText("");
            tvBio.setText("");
            return;
        }

        bindProfile(user.getFullName(), user.getEmail(), user.getBio(), user.getAvatarUrl());
    }

    private boolean bindCachedProfile() {
        String cachedFullName = sessionManager.getFullName();
        String cachedEmail = sessionManager.getEmail();
        String cachedBio = sessionManager.getBio();
        String cachedAvatar = sessionManager.getAvatarUrl();
        if (!hasValue(cachedFullName) && !hasValue(cachedEmail)
                && !hasValue(cachedBio) && !hasValue(cachedAvatar)) {
            return false;
        }

        bindProfile(cachedFullName, cachedEmail, cachedBio, cachedAvatar);
        return true;
    }

    private void bindProfile(String fullName, String email, String bio, String avatarUrl) {
        tvFullName.setText(hasValue(fullName) ? fullName : getString(R.string.unnamed_user));
        tvEmail.setText(valueOrEmpty(email));
        tvBio.setText(hasValue(bio) ? bio : getNoBioText());

        if (hasValue(avatarUrl)) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_camera_24)
                    .error(R.drawable.ic_camera_24)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_camera_24);
        }
    }

    private void uploadImageToSupabaseStorage(Uri uri) {
        btnUpdateAvatar.setEnabled(false);
        btnUpdateAvatar.setAlpha(0.5f);
        String userId = sessionManager.getUserId();
        if (!hasValue(userId)) {
            btnUpdateAvatar.setEnabled(true);
            btnUpdateAvatar.setAlpha(1.0f);
            redirectToLogin();
            return;
        }

        Toast.makeText(this, R.string.uploading_avatar, Toast.LENGTH_SHORT).show();

        storageRepository.upload(uri, "avatar_" + userId + ".jpg", "avatars", new SupabaseStorageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String publicUrl) {
                // Sau khi upload lên Storage thành công, cập nhật URL vào bảng users
                userRepository.updateAvatar(userId, publicUrl, new UserRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        sessionManager.saveProfile(
                            valueOrEmpty(sessionManager.getFullName()),
                            valueOrEmpty(sessionManager.getEmail()),
                            valueOrEmpty(sessionManager.getBio()),
                            publicUrl
                        );
                        runOnUiThread(() -> {
                            Toast.makeText(AccountActivity.this, R.string.avatar_updated_success, Toast.LENGTH_SHORT).show();
                            btnUpdateAvatar.setEnabled(true);
                            btnUpdateAvatar.setAlpha(1.0f);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(AccountActivity.this, getString(R.string.failed_update_database, message), Toast.LENGTH_SHORT).show();
                            btnUpdateAvatar.setEnabled(true);
                            btnUpdateAvatar.setAlpha(1.0f);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, getString(R.string.upload_failed, message), Toast.LENGTH_SHORT).show();
                    btnUpdateAvatar.setEnabled(true);
                    btnUpdateAvatar.setAlpha(1.0f);
                });
            }
        });
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private void setupLanguageSpinner() {
        Spinner spLanguage = findViewById(R.id.spLanguage);
        if (spLanguage == null) return;

        String[] labels = {getString(R.string.language_english), getString(R.string.language_vietnamese)};
        String[] codes = {LanguageManager.LANGUAGE_ENGLISH, LanguageManager.LANGUAGE_VIETNAMESE};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spLanguage.setAdapter(adapter);
        spLanguage.setSelection(findLanguageIndex(codes, LanguageManager.getSavedLanguage(this)), false);
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= codes.length) return;
                String selectedCode = codes[position];
                if (!selectedCode.equals(LanguageManager.getSavedLanguage(AccountActivity.this))) {
                    LanguageManager.saveLanguage(AccountActivity.this, selectedCode);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int findLanguageIndex(String[] codes, String selectedCode) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(selectedCode)) {
                return i;
            }
        }
        return 0;
    }

    private void applyLanguageText() {
        tvAccountTitle.setText(R.string.my_account);
        tvBioLabel.setText(R.string.bio_label);
        tvLanguageLabel.setText(R.string.app_language);
        tvLanguageHint.setText(R.string.choose_display_language);
        btnEditProfile.setText(R.string.edit_profile_upper);
        btnLogout.setText(R.string.logout_upper);
    }

    private String getNoBioText() {
        return getString(R.string.no_bio_available);
    }

    private void redirectToLogin() {
        sessionManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
