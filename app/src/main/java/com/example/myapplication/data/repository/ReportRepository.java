package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.ReportApi;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.models.Report;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportRepository {
    private final ReportApi reportApi;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public ReportRepository(Context context) {
        // Initialize ReportApi using RetrofitClient
        this.reportApi = RetrofitClient.getClient(context).create(ReportApi.class);
    }

    // Get all Reports from remote
    public void getAll(RepositoryCallback<List<Report>> callback) {
        reportApi.getAll().enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Get Report by ID and handle Supabase List response
    public void getById(String id, RepositoryCallback<Report> callback) {
        reportApi.getById(id).enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Report not found");
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Insert new Report to remote
    public void insert(Report report, RepositoryCallback<Void> callback) {
        reportApi.insert(report).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Update Report by ID on remote
    public void update(String id, Report report, RepositoryCallback<Void> callback) {
        reportApi.update(id, report).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Delete Report by ID from remote
    public void delete(String id, RepositoryCallback<Void> callback) {
        reportApi.delete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
