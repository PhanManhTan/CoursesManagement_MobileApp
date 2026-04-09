package com.example.myapplication.activities.admin;

import android.os.Bundle;
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
