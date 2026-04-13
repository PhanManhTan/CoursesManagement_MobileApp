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

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> mockNotifications = new ArrayList<>();
        mockNotifications.add(new Notification("Payment Successful", "Your payment for 'Advanced Android' was successful.", false));
        mockNotifications.add(new Notification("New Course Available", "Check out the new UI/UX Design course by Huy.", true));

        NotificationAdapter adapter = new NotificationAdapter(mockNotifications);
        rvNotifications.setAdapter(adapter);
    }
}