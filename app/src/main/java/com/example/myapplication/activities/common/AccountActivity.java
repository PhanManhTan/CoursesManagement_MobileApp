package com.example.myapplication.activities.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvBio;
    private ImageView ivAvatar;
    private ImageView btnUpdateAvatar;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        userRepository = new UserRepository(this);
        storageRepository = new SupabaseStorageRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBio = findViewById(R.id.tvBio);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnUpdateAvatar = findViewById(R.id.btnUpdateAvatar);

        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnUpdateAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        String userId = sessionManager.getUserId();
        if (!hasValue(userId)) {
            tvFullName.setText("Guest User");
            tvEmail.setText("");
            return;
        }

        userRepository.getById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
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
            tvFullName.setText("Guest User");
            tvEmail.setText("");
            tvBio.setText("");
            return;
        }

        tvFullName.setText(hasValue(user.getFullName()) ? user.getFullName() : "Unnamed User");
        tvEmail.setText(valueOrEmpty(user.getEmail()));
        tvBio.setText(hasValue(user.getBio()) ? user.getBio() : "No bio available");

        if (hasValue(user.getAvatarUrl())) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_camera)
                    .into(ivAvatar);
        }

        // Dynamically show and configure bottomNav if role is admin
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            if ("admin".equalsIgnoreCase(user.getRole())) {
                bottomNav.setVisibility(android.view.View.VISIBLE);
                setupBottomNav(bottomNav);
            } else {
                bottomNav.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void setupBottomNav(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(R.id.nav_admin_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_account) {
                return true;
            }
            Intent intent;
            if (id == R.id.nav_admin_home) {
                intent = new Intent(this, com.example.myapplication.activities.admin.AdminDashboardActivity.class);
            } else if (id == R.id.nav_admin_users) {
                intent = new Intent(this, com.example.myapplication.activities.admin.UserManageActivity.class);
            } else if (id == R.id.nav_admin_approval) {
                intent = new Intent(this, com.example.myapplication.activities.admin.CourseApprovalActivity.class);
            } else if (id == R.id.nav_admin_reports) {
                intent = new Intent(this, com.example.myapplication.activities.admin.ReportActivity.class);
            } else {
                return false;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        });
    }

    private void uploadImageToSupabaseStorage(Uri uri) {
        btnUpdateAvatar.setEnabled(false);
        btnUpdateAvatar.setAlpha(0.5f);
        String userId = sessionManager.getUserId();
        if (!hasValue(userId)) return;

        Toast.makeText(this, "Uploading avatar...", Toast.LENGTH_SHORT).show();

        storageRepository.upload(uri, "avatar_" + userId + ".jpg", "avatars", new SupabaseStorageRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String publicUrl) {
                // Sau khi upload lên Storage thành công, cập nhật URL vào bảng users
                userRepository.updateAvatar(userId, publicUrl, new UserRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        runOnUiThread(() -> {
                            Toast.makeText(AccountActivity.this, "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                            // Load lại ảnh từ URL chính thức
                            Glide.with(AccountActivity.this)
                                    .load(publicUrl)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                    .placeholder(ivAvatar.getDrawable())
                                    .into(ivAvatar);
                            btnUpdateAvatar.setEnabled(true);
                            btnUpdateAvatar.setAlpha(1.0f);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(AccountActivity.this, "Failed to update database: " + message, Toast.LENGTH_SHORT).show();
                            btnUpdateAvatar.setEnabled(true);
                            btnUpdateAvatar.setAlpha(1.0f);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
                    btnUpdateAvatar.setEnabled(true);
                    btnUpdateAvatar.setAlpha(1.0f);
                });
            }
        });
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
