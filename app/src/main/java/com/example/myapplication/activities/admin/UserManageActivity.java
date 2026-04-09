package com.example.myapplication.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.UserAdapter;
import com.example.myapplication.viewmodels.UserManageViewModel;

public class UserManageActivity extends AppCompatActivity {

    private UserManageViewModel viewModel;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter();
        rvUsers.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(UserManageViewModel.class);
        viewModel.getUsers().observe(this, users -> adapter.setUsers(users));

        // Search Logic
        EditText etSearchUser = findViewById(R.id.etSearchUser);
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Role Filter Listeners
        findViewById(R.id.tvFilterAll).setOnClickListener(v -> viewModel.filterUsers("All"));
        findViewById(R.id.tvFilterStudent).setOnClickListener(v -> viewModel.filterUsers("Student"));
        findViewById(R.id.tvFilterInstructor).setOnClickListener(v -> viewModel.filterUsers("Instructor"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
