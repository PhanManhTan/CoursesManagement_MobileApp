package com.example.myapplication.activities.common;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etBio = findViewById(R.id.etBio);
        Button btnSave = findViewById(R.id.btnSave);

        etFullName.setText("Huynh Hoang Huy");
        etBio.setText("I am a software engineering student.");

        btnSave.setOnClickListener(v -> {
            finish();
        });
    }
}