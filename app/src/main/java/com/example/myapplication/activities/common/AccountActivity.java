package com.example.myapplication.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

import com.example.myapplication.models.User;
import com.example.myapplication.utils.MockData;
import java.util.List;

public class AccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        // Fetch dynamic data
        String email = getIntent().getStringExtra("email");
        if (email == null) {
            // Fallback for demo if intent was empty
            email = "student@gmail.com";
        }
        
        List<User> users = MockData.getUsers();
        User currentUser = null;
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                currentUser = u;
                break;
            }
        }

        if (currentUser != null) {
            tvFullName.setText(currentUser.getFullName());
            tvEmail.setText(currentUser.getEmail());
        } else {
            tvFullName.setText("Guest User");
            tvEmail.setText(email);
        }

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.myapplication.activities.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}