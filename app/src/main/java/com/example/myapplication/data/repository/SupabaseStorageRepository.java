package com.example.myapplication.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activities.auth.LoginActivity;
import com.example.myapplication.utils.Constants;
import com.example.myapplication.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .callTimeout(10, TimeUnit.MINUTES)
                .build();
        this.sessionManager = new SessionManager(this.context);
    }

    public void upload(Uri fileUri, String displayName, String bucketName, RepositoryCallback<String> callback) {
        if (hasValue(bucketName) && (bucketName.contains("/") || bucketName.contains("\\"))) {
            uploadInternal(fileUri, displayName, getBucketName(), bucketName, callback);
            return;
        }
        uploadInternal(fileUri, displayName, bucketName, "", callback);
    }

    public void uploadToFolder(Uri fileUri, String displayName, String folderPath, RepositoryCallback<String> callback) {
        uploadInternal(fileUri, displayName, getBucketName(), folderPath, callback);
    }

    /**
     * @param bucketName Tên bucket trên Supabase (ví dụ: "avatars" hoặc bucket mặc định "course-media")
     * @param folderPath Thư mục bên trong bucket, ví dụ "courses/{courseId}/lessons/videos"
     */
    private void uploadInternal(Uri fileUri, String displayName, String bucketName, String folderPath, RepositoryCallback<String> callback) {
        if (fileUri == null) {
            callback.onError(context.getString(R.string.no_file_selected));
            return;
        }

        String projectBaseUrl = getProjectBaseUrl();
        if (!hasValue(projectBaseUrl) || !hasValue(Constants.SUPABASE_API_KEY)) {
            callback.onError(context.getString(R.string.supabase_not_configured));
            return;
        }

        String token = sessionManager.getToken();
        if (!hasValue(token)) {
            redirectToLogin();
            callback.onError(context.getString(R.string.session_expired_login_again));
            return;
        }

        String bucket = hasValue(bucketName) ? bucketName : getBucketName();

        String objectPath = buildObjectPath(folderPath, displayName, fileUri);

        String mimeType = context.getContentResolver().getType(fileUri);
        if (!hasValue(mimeType)) {
            mimeType = "application/octet-stream";
        }
        MediaType mediaType = MediaType.parse(mimeType);
        if (mediaType == null) {
            mediaType = MediaType.parse("application/octet-stream");
            mimeType = "application/octet-stream";
        }

        String uploadUrl = projectBaseUrl
                + "/storage/v1/object/"
                + encodePath(bucket)
                + "/"
                + encodePath(objectPath);
                
        Log.d(TAG, "Uploading to Bucket: " + bucket + " | Path: " + objectPath);

        RequestBody fileBody = new UriRequestBody(
                context.getContentResolver(),
                fileUri,
                mediaType
        );

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(fileBody)
                .header("apikey", Constants.SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", mimeType)
                .header("x-upsert", "true")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(formatNetworkUploadError(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean successful = response.isSuccessful();
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";
                response.close();

                if (!successful) {
                    Log.e(TAG, "Upload failed (" + statusCode + "): " + responseBody);
                    if (statusCode == 401) {
                        redirectToLogin();
                        callback.onError(context.getString(R.string.session_expired_login_again));
                        return;
                    }
                    callback.onError(parseUploadError(responseBody, statusCode, bucket));
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
        return uri.getScheme() + "://" + uri.getHost();
    }

    private String getBucketName() {
        return hasValue(Constants.SUPABASE_STORAGE_BUCKET) ? Constants.SUPABASE_STORAGE_BUCKET : DEFAULT_BUCKET;
    }

    private String buildObjectPath(String folder, String displayName, Uri fileUri) {
        String userFolder = sanitizePathPart(sessionManager.getUserId());
        if (!hasValue(userFolder)) userFolder = "anonymous";

        String safeFileName = sanitizeFileName(hasValue(displayName) ? displayName : getDisplayName(fileUri));
        String uniqueName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + safeFileName;

        if (hasValue(folder)) {
            String safeFolder = sanitizeFolderPath(folder);
            if (hasValue(safeFolder)) {
                return userFolder + "/" + safeFolder + "/" + uniqueName;
            }
        }
        return userFolder + "/" + uniqueName;
    }

    private String sanitizeFileName(String name) {
        return sanitizePathPart(name);
    }

    private String sanitizePathPart(String value) {
        if (!hasValue(value)) return "";
        return value.trim().replaceAll("[\\\\/]+", "-").replaceAll("[^A-Za-z0-9._-]", "-");
    }

    private String sanitizeFolderPath(String value) {
        if (!hasValue(value)) return "";
        String[] parts = value.trim().split("[\\\\/]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String safePart = sanitizePathPart(part);
            if (!hasValue(safePart)) continue;
            if (builder.length() > 0) builder.append('/');
            builder.append(safePart);
        }
        return builder.toString();
    }

    private String encodePath(String path) {
        if (!hasValue(path)) return "";
        String[] parts = path.split("/");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) builder.append('/');
            builder.append(Uri.encode(part));
        }
        return builder.toString();
    }

    private String buildPublicUrl(String projectBaseUrl, String bucket, String objectPath) {
        return projectBaseUrl + "/storage/v1/object/public/" + encodePath(bucket) + "/" + encodePath(objectPath);
    }

    private String parseUploadError(String responseBody, int statusCode, String bucket) {
        try {
            JSONObject json = new JSONObject(responseBody);
            String code = json.optString("statusCode", json.optString("code", ""));
            String error = json.optString("error", "");
            String message = json.optString("message", "");
            String combined = (code + " " + error + " " + message).toLowerCase(Locale.US);
            if (isBucketNotFound(statusCode, combined)) {
                return context.getString(R.string.storage_bucket_not_found_format, bucket);
            }
            if (combined.contains("row-level security")) {
                return context.getString(R.string.storage_permission_denied);
            }
            if (hasValue(message)) {
                return message;
            }
            if (hasValue(error)) {
                return error;
            }
        } catch (JSONException e) {
            String lowerBody = responseBody != null ? responseBody.toLowerCase(Locale.US) : "";
            if (isBucketNotFound(statusCode, lowerBody)) {
                return context.getString(R.string.storage_bucket_not_found_format, bucket);
            }
        }
        if (statusCode == 401 || statusCode == 403) {
            return context.getString(R.string.storage_permission_denied);
        }
        if (statusCode == 400 && lower(responseBody).contains("row-level security")) {
            return context.getString(R.string.storage_permission_denied);
        }
        if (statusCode == 413) {
            return context.getString(R.string.upload_file_too_large);
        }
        return "Error " + statusCode;
    }

    private String formatNetworkUploadError(IOException error) {
        if (error instanceof SocketTimeoutException) {
            return context.getString(R.string.upload_timeout);
        }
        String message = error.getMessage();
        return context.getString(
                R.string.upload_network_error_format,
                hasValue(message) ? message : context.getString(R.string.unknown_error)
        );
    }

    private String lower(String value) {
        return value != null ? value.toLowerCase(Locale.US) : "";
    }

    private boolean isBucketNotFound(int statusCode, String lowerMessage) {
        if (lowerMessage == null) {
            lowerMessage = "";
        }
        return lowerMessage.contains("bucket not found")
                || lowerMessage.contains("bucket_not_found")
                || (statusCode == 404 && lowerMessage.contains("bucket"));
    }

    private String getDisplayName(Uri uri) {
        String displayName = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex);
            }
        }
        return TextUtils.isEmpty(displayName) ? "file_" + System.currentTimeMillis() : displayName;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void redirectToLogin() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, R.string.session_expired_login_again, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
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
        public MediaType contentType() { return mediaType; }

        @Override
        public long contentLength() throws IOException {
            try (AssetFileDescriptor descriptor = contentResolver.openAssetFileDescriptor(uri, "r")) {
                if (descriptor != null && descriptor.getLength() >= 0) {
                    return descriptor.getLength();
                }
            } catch (Exception ignored) {
            }

            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0) {
                        long size = cursor.getLong(sizeIndex);
                        if (size >= 0) {
                            return size;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return -1;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                if (inputStream == null) throw new IOException("Uri not found");
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }
            }
        }
    }
}
