package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.activities.auth.LoginActivity;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        SessionManager session = new SessionManager(this);

        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvEmail    = findViewById(R.id.tvEmail);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout      = findViewById(R.id.btnLogout);

        tvFullName.setText(session.getUserFullName() != null ? session.getUserFullName() : "");
        tvEmail.setText(session.getUserEmail()    != null ? session.getUserEmail()    : "");

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            session.clearSession();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show());
    }
}
