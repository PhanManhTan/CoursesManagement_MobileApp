package com.example.myapplication.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import retrofit2.Response;

public final class ApiErrorFormatter {
    private static final String DEFAULT_ERROR = "Something went wrong. Please try again.";

    private ApiErrorFormatter() {
    }

    public static String fromResponse(Response<?> response) {
        if (response == null) {
            return DEFAULT_ERROR;
        }

        String body = "";
        try {
            if (response.errorBody() != null) {
                body = response.errorBody().string();
            }
        } catch (IOException ignored) {
        }

        if (!hasValue(body)) {
            return httpFallback(response.code());
        }
        return fromMessage(body);
    }

    public static String fromMessage(String message) {
        if (!hasValue(message)) {
            return DEFAULT_ERROR;
        }

        String raw = message.trim();
        String body = stripErrorPrefix(raw);
        if (!hasValue(body)) {
            return httpFallback(extractHttpCode(raw));
        }
        String lower = body.toLowerCase(Locale.US);

        String known = formatKnownDatabaseError(lower);
        if (known != null) {
            return known;
        }

        String parsed = parseJsonError(body);
        if (hasValue(parsed)) {
            return parsed;
        }

        if (isNetworkError(lower)) {
            return "Cannot connect to the server. Please check your internet connection and try again.";
        }

        return body;
    }

    private static String formatKnownDatabaseError(String lowerMessage) {
        if (lowerMessage.contains("lessons_content_type_check")) {
            return "Cannot save this lesson because the database rejected its old content type. Empty lessons now use a valid draft type, so please try saving again.";
        }
        if (lowerMessage.contains("foreign key constraint") || lowerMessage.contains("violates foreign key")) {
            if (lowerMessage.contains("chapter_id") || lowerMessage.contains("lesson_id")) {
                return "Cannot save this item because its parent record has not been saved on the server yet. Save the course or chapter first, then try again.";
            }
            return "Cannot save this item because it is linked to another record that does not exist yet.";
        }
        if (lowerMessage.contains("duplicate key value") || lowerMessage.contains("unique constraint")) {
            return "Cannot save because another item already uses this value.";
        }
        if (lowerMessage.contains("permission denied") || lowerMessage.contains("not have permission")) {
            return "You do not have permission to make this change.";
        }
        return null;
    }

    private static String parseJsonError(String body) {
        String json = extractJson(body);
        if (!hasValue(json)) {
            return null;
        }

        try {
            JSONObject object = new JSONObject(json);
            String code = object.optString("code");
            String message = object.optString("message");
            String details = object.optString("details");
            String hint = object.optString("hint");

            String combined = (code + " " + message + " " + details + " " + hint).toLowerCase(Locale.US);
            String known = formatKnownDatabaseError(combined);
            if (known != null) {
                return known;
            }

            if ("23514".equals(code)) {
                return "Cannot save because one of the values does not match the database rules.";
            }
            if ("23503".equals(code)) {
                return "Cannot save because a related course, chapter, or lesson has not been saved yet.";
            }
            if (hasValue(message)) {
                return message;
            }
            if (hasValue(details)) {
                return details;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String stripErrorPrefix(String message) {
        String result = message;
        if (result.matches("(?is)^error\\s+\\d+\\s*:\\s*.*")) {
            result = result.replaceFirst("(?is)^error\\s+\\d+\\s*:\\s*", "");
        } else if (result.matches("(?is)^error\\s*:\\s*\\d+\\s*$")) {
            result = "";
        }
        return result.trim();
    }

    private static int extractHttpCode(String message) {
        if (!hasValue(message)) {
            return 0;
        }
        for (int i = 0; i <= message.length() - 3; i++) {
            char first = message.charAt(i);
            char second = message.charAt(i + 1);
            char third = message.charAt(i + 2);
            if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
                try {
                    return Integer.parseInt(message.substring(i, i + 3));
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private static String extractJson(String message) {
        int start = message.indexOf('{');
        int end = message.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return message.substring(start, end + 1);
        }
        return null;
    }

    private static boolean isNetworkError(String lowerMessage) {
        return lowerMessage.contains("unable to resolve host")
                || lowerMessage.contains("failed to connect")
                || lowerMessage.contains("timeout")
                || lowerMessage.contains("timed out")
                || lowerMessage.contains("connection reset");
    }

    private static String httpFallback(int code) {
        if (code == 401 || code == 403) {
            return "You do not have permission to make this change.";
        }
        if (code == 404) {
            return "The requested item was not found.";
        }
        if (code >= 500) {
            return "The server could not complete the request. Please try again later.";
        }
        if (code > 0) {
            return "Request failed with HTTP " + code + ". Please try again.";
        }
        return DEFAULT_ERROR;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
