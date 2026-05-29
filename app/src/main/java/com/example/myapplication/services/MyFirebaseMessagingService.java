package com.example.myapplication.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.myapplication.R;
import com.example.myapplication.activities.admin.AdminMainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Handles incoming Firebase Cloud Messaging pushes and token updates.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "admin_notifications_channel";
    private static final String CHANNEL_NAME = "Admin Tasks Channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Log token so developer can use it for manual testing/API registration
        Log.d(TAG, "New registration token generated: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = null;
        String body = null;
        String type = null;

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // Check if message contains a data payload
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            type = data.get("type");
            if (title == null) title = data.get("title");
            if (body == null) body = data.get("body");
        }

        // Fallbacks for empty fields
        if (title == null) title = "New Admin Action Needed";
        if (body == null) body = "Please check dashboard details.";

        sendLocalNotification(title, body, type);
    }

    private void sendLocalNotification(String title, String body, String type) {
        Intent intent = new Intent(this, AdminMainActivity.class);
        if ("course_pending_approval".equalsIgnoreCase(type)) {
            intent.putExtra("TARGET_TAB", "course_pending_approval");
        } else if ("course_violation_report".equalsIgnoreCase(type)) {
            intent.putExtra("TARGET_TAB", "course_violation_report");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Define channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications related to administrative actions and approvals");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon_app) // Use application launcher icon as small icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
}
