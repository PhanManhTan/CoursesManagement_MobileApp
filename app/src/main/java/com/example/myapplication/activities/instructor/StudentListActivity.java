package com.example.myapplication.activities.instructor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.StudentAdapter;
import com.example.myapplication.viewmodels.StudentListViewModel;

public class StudentListActivity extends AppCompatActivity {

    private StudentListViewModel viewModel;
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        RecyclerView rvStudents = findViewById(R.id.rvStudents);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new StudentAdapter();
        rvStudents.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(StudentListViewModel.class);
        viewModel.getStudents().observe(this, students -> adapter.setStudents(students));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
