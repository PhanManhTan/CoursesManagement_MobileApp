package com.example.myapplication.activities.common;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapters.NotificationAdapter;
import com.example.myapplication.models.Notification;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> mockNotifications = new ArrayList<>();
        mockNotifications.add(new Notification("n1", "user123", "Payment Successful", "Your payment for 'Advanced Android' was successful.", false, "2023-10-25"));
        mockNotifications.add(new Notification("n2", "user123", "New Course Available", "Check out the new UI/UX Design course by Huy.", true, "2023-10-26"));

        NotificationAdapter adapter = new NotificationAdapter(mockNotifications);
        rvNotifications.setAdapter(adapter);
    }
}
