package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        tvFullName.setText("Huy Huynh");
        tvEmail.setText("student@example.com");

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, EditProfileActivity.class));
        });
    }
}