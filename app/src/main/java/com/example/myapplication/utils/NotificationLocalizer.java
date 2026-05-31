package com.example.myapplication.utils;

import android.content.Context;
import android.widget.TextView;
import com.example.myapplication.R;
import com.example.myapplication.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationLocalizer {

    public static void localize(Context context, Notification notification, TextView tvTitle, TextView tvMessage) {
        if (notification == null) return;

        Context localizedContext = LanguageManager.getLocalizedContext(context);

        String title = notification.getTitle();
        String message = notification.getMessage();
        if (title == null || message == null) {
            if (tvTitle != null) tvTitle.setText(title);
            if (tvMessage != null) tvMessage.setText(message);
            return;
        }

        String titleLower = title.toLowerCase();

        // New chapter notification
        if (titleLower.contains("chapter") || titleLower.contains("chương")) {
            if (tvTitle != null) {
                tvTitle.setText(localizedContext.getString(R.string.notification_new_chapter_title));
            }
            if (tvMessage != null) {
                List<String> values = extractQuotedValues(message);
                if (values.size() >= 2) {
                    tvMessage.setText(localizedContext.getString(
                            R.string.notification_new_chapter_body,
                            values.get(0),
                            values.get(1)
                    ));
                } else {
                    tvMessage.setText(message);
                }
            }
            return;
        }

        // New lesson notification
        if (titleLower.contains("lesson") || titleLower.contains("bài học")) {
            if (tvTitle != null) {
                tvTitle.setText(localizedContext.getString(R.string.notification_new_lesson_title));
            }
            if (tvMessage != null) {
                List<String> values = extractQuotedValues(message);
                if (values.size() >= 2) {
                    tvMessage.setText(localizedContext.getString(
                            R.string.notification_new_lesson_body,
                            values.get(0),
                            values.get(1)
                    ));
                } else {
                    tvMessage.setText(message);
                }
            }
            return;
        }

        // Course purchased notification
        if (titleLower.contains("purchase") || titleLower.contains("purchased") || titleLower.contains("mua")) {
            if (tvTitle != null) {
                tvTitle.setText(localizedContext.getString(R.string.notification_course_purchased_title));
            }
            if (tvMessage != null) {
                try {
                    int firstQuote = message.indexOf("\"");
                    int lastQuote = message.lastIndexOf("\"");
                    if (firstQuote != -1 && lastQuote != -1 && lastQuote > firstQuote) {
                        String courseTitle = message.substring(firstQuote + 1, lastQuote);
                        String amountPart = "";
                        if (message.toLowerCase().contains("for ")) {
                            int index = message.toLowerCase().lastIndexOf("for ");
                            amountPart = message.substring(index + 4).trim();
                        } else if (message.toLowerCase().contains("giá ")) {
                            int index = message.toLowerCase().lastIndexOf("giá ");
                            amountPart = message.substring(index + 4).trim();
                        }
                        if (amountPart.endsWith(".")) {
                            amountPart = amountPart.substring(0, amountPart.length() - 1);
                        }
                        if (!courseTitle.isEmpty() && !amountPart.isEmpty()) {
                            tvMessage.setText(localizedContext.getString(R.string.notification_course_purchased_body, courseTitle, amountPart));
                            return;
                        }
                    }
                } catch (Exception ignored) {}
                tvMessage.setText(message);
            }
            return;
        }

        // Course rejected notification
        if (titleLower.contains("rejected") || titleLower.contains("reject") || titleLower.contains("từ chối")) {
            if (tvTitle != null) {
                tvTitle.setText(localizedContext.getString(R.string.notification_course_rejected_title));
            }
            if (tvMessage != null) {
                try {
                    int firstQuote = message.indexOf("\"");
                    int lastQuote = message.lastIndexOf("\"");
                    if (firstQuote != -1 && lastQuote != -1 && lastQuote > firstQuote) {
                        String courseTitle = message.substring(firstQuote + 1, lastQuote);
                        if (!courseTitle.isEmpty()) {
                            tvMessage.setText(localizedContext.getString(R.string.notification_course_rejected_body, courseTitle));
                            return;
                        }
                    }
                } catch (Exception ignored) {}
                tvMessage.setText(message);
            }
            return;
        }

        // Course approved notification
        if (titleLower.contains("approved") || titleLower.contains("duyệt")) {
            if (tvTitle != null) {
                tvTitle.setText(localizedContext.getString(R.string.notification_course_approved_title));
            }
            if (tvMessage != null) {
                try {
                    int firstQuote = message.indexOf("\"");
                    int lastQuote = message.lastIndexOf("\"");
                    if (firstQuote != -1 && lastQuote != -1 && lastQuote > firstQuote) {
                        String courseTitle = message.substring(firstQuote + 1, lastQuote);
                        if (!courseTitle.isEmpty()) {
                            tvMessage.setText(localizedContext.getString(R.string.notification_course_approved_body, courseTitle));
                            return;
                        }
                    }
                } catch (Exception ignored) {}
                tvMessage.setText(message);
            }
            return;
        }

        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);
    }

    private static List<String> extractQuotedValues(String message) {
        List<String> values = new ArrayList<>();
        if (message == null) {
            return values;
        }

        int start = message.indexOf("\"");
        while (start != -1 && start < message.length() - 1) {
            int end = message.indexOf("\"", start + 1);
            if (end == -1) {
                break;
            }
            values.add(message.substring(start + 1, end));
            start = message.indexOf("\"", end + 1);
        }
        return values;
    }
}
