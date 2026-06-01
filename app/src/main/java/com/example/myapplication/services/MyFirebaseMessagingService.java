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
import com.example.myapplication.activities.common.NotificationActivity;
import com.example.myapplication.data.repository.FcmRepository;
import com.example.myapplication.utils.LanguageManager;
import com.example.myapplication.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Handles incoming Firebase Cloud Messaging pushes and token updates.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "app_notifications_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New registration token generated: " + token);
        String userId = new SessionManager(this).getUserId();
        if (userId != null && !userId.isEmpty()) {
            new FcmRepository(this).uploadToken(userId, token);
        }
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
        Context localizedContext = LanguageManager.getLocalizedContext(this);
        if (title == null) title = localizedContext.getString(R.string.fcm_fallback_title);
        if (body == null) body = localizedContext.getString(R.string.fcm_fallback_body);

        sendLocalNotification(title, body, type);
    }

    private void sendLocalNotification(String title, String body, String type) {
        if ("course_pending_approval".equalsIgnoreCase(type)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("TARGET_TAB", "course_pending_approval");
            showNotification(title, body, intent);
        } else if ("course_violation_report".equalsIgnoreCase(type)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("TARGET_TAB", "course_violation_report");
            showNotification(title, body, intent);
        } else {
            showNotification(title, body, new Intent(this, NotificationActivity.class));
        }
    }

    private void showNotification(String title, String body, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Context localizedContext = LanguageManager.getLocalizedContext(this);

        // Define channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    localizedContext.getString(R.string.fcm_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(localizedContext.getString(R.string.fcm_channel_description));
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
