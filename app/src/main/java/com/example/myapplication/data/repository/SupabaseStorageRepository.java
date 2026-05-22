package com.example.myapplication.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class SupabaseStorageRepository {
    private static final String TAG = "SupabaseStorageRepo";
    private static final String DEFAULT_BUCKET = "course-media";

    private final Context context;
    private final OkHttpClient client;
    private final SessionManager sessionManager;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public SupabaseStorageRepository(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient();
        this.sessionManager = new SessionManager(this.context);
    }

    public void upload(Uri fileUri, String displayName, String folder, RepositoryCallback<String> callback) {
        if (fileUri == null) {
            callback.onError("No file selected");
            return;
        }

        String projectBaseUrl = getProjectBaseUrl();
        if (!hasValue(projectBaseUrl) || !hasValue(Constants.SUPABASE_API_KEY)) {
            callback.onError("Supabase is not configured");
            return;
        }

        String token = sessionManager.getToken();
        if (!hasValue(token)) {
            callback.onError("Missing Supabase session");
            return;
        }

        String bucket = getBucketName();
        String objectPath = buildObjectPath(folder, displayName, fileUri);
        String mimeType = context.getContentResolver().getType(fileUri);
        if (!hasValue(mimeType)) {
            mimeType = "application/octet-stream";
        }

        String uploadUrl = projectBaseUrl
                + "/storage/v1/object/"
                + encodePath(bucket)
                + "/"
                + encodePath(objectPath);
        RequestBody fileBody = new UriRequestBody(
                context.getContentResolver(),
                fileUri,
                MediaType.parse(mimeType)
        );

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(fileBody)
                .header("apikey", Constants.SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", mimeType)
                .header("cache-control", "3600")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean successful = response.isSuccessful();
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";
                response.close();

                if (!successful) {
                    Log.e(TAG, "Upload failed: " + responseBody);
                    callback.onError(parseUploadError(responseBody, statusCode));
                    return;
                }

                callback.onSuccess(buildPublicUrl(projectBaseUrl, bucket, objectPath));
            }
        });
    }

    private String getProjectBaseUrl() {
        String supabaseUrl = Constants.SUPABASE_URL;
        if (!hasValue(supabaseUrl)) return "";

        Uri uri = Uri.parse(supabaseUrl);
        if (!hasValue(uri.getScheme()) || !hasValue(uri.getHost())) {
            return "";
        }
        return uri.getScheme() + "://" + uri.getHost();
    }

    private String getBucketName() {
        return hasValue(Constants.SUPABASE_STORAGE_BUCKET)
                ? Constants.SUPABASE_STORAGE_BUCKET
                : DEFAULT_BUCKET;
    }

    private String buildObjectPath(String folder, String displayName, Uri fileUri) {
        String userFolder = sanitizePathPart(sessionManager.getUserId());
        if (!hasValue(userFolder)) {
            userFolder = "anonymous";
        }

        String safeFolder = normalizeFolder(folder);
        String safeFileName = sanitizeFileName(hasValue(displayName) ? displayName : getDisplayName(fileUri));
        String uniqueName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + safeFileName;

        if (hasValue(safeFolder)) {
            return userFolder + "/" + safeFolder + "/" + uniqueName;
        }
        return userFolder + "/" + uniqueName;
    }

    private String normalizeFolder(String folder) {
        if (!hasValue(folder)) return "";

        String[] parts = folder.split("/");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String safePart = sanitizePathPart(part);
            if (!hasValue(safePart)) continue;

            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(safePart);
        }
        return builder.toString();
    }

    private String sanitizeFileName(String name) {
        String safeName = sanitizePathPart(name);
        return hasValue(safeName) ? safeName : "selected-file";
    }

    private String sanitizePathPart(String value) {
        if (!hasValue(value)) return "";
        return value.trim()
                .replaceAll("[\\\\/]+", "-")
                .replaceAll("[^A-Za-z0-9._-]", "-")
                .replaceAll("-+", "-");
    }

    private String encodePath(String path) {
        String[] parts = path.split("/");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(Uri.encode(part));
        }
        return builder.toString();
    }

    private String buildPublicUrl(String projectBaseUrl, String bucket, String objectPath) {
        return projectBaseUrl
                + "/storage/v1/object/public/"
                + encodePath(bucket)
                + "/"
                + encodePath(objectPath);
    }

    private String parseUploadError(String responseBody, int statusCode) {
        if (!hasValue(responseBody)) {
            return "Supabase Storage upload failed: " + statusCode;
        }
        try {
            JSONObject json = new JSONObject(responseBody);
            String message = json.optString("message", "");
            if (!hasValue(message)) {
                message = json.optString("error", "");
            }
            if (hasValue(message)) {
                return "Supabase Storage upload failed: " + message;
            }
        } catch (JSONException ignored) {
        }
        return "Supabase Storage upload failed: " + statusCode;
    }

    private String getDisplayName(Uri uri) {
        String displayName = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex);
                }
            }
        }

        if (TextUtils.isEmpty(displayName)) {
            displayName = uri.getLastPathSegment();
        }
        return TextUtils.isEmpty(displayName) ? "selected-file" : displayName;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class UriRequestBody extends RequestBody {
        private final ContentResolver contentResolver;
        private final Uri uri;
        private final MediaType mediaType;

        private UriRequestBody(ContentResolver contentResolver, Uri uri, MediaType mediaType) {
            this.contentResolver = contentResolver;
            this.uri = uri;
            this.mediaType = mediaType;
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                if (inputStream == null) {
                    throw new IOException("Cannot open selected file");
                }

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
