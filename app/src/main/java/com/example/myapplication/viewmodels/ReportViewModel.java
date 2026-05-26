package com.example.myapplication.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.repository.EnrollmentRepository;
import com.example.myapplication.data.repository.ReportRepository;
import com.example.myapplication.models.Enrollment;
import com.example.myapplication.models.Report;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportViewModel extends AndroidViewModel {
    private final ReportRepository reportRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();
    private final MutableLiveData<List<Enrollment>> enrollments = new MutableLiveData<>();
    private final MutableLiveData<String> totalAnnualRevenue = new MutableLiveData<>();
    private final MutableLiveData<String> revenueTrend = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ReportViewModel(@NonNull Application application) {
        super(application);
        reportRepository = new ReportRepository(application);
        enrollmentRepository = new EnrollmentRepository(application);
        fetchReports();
        fetchRevenueStats();
    }

    public void fetchReports() {
        reportRepository.getAll(new ReportRepository.RepositoryCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> data) {
                if (data != null) {
                    reports.setValue(data);
                } else {
                    reports.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void fetchRevenueStats() {
        enrollmentRepository.getAll(new EnrollmentRepository.RepositoryCallback<List<Enrollment>>() {
            @Override
            public void onSuccess(List<Enrollment> enrollmentsList) {
                double total = 0;
                if (enrollmentsList != null) {
                    for (Enrollment e : enrollmentsList) {
                        total += e.getPaidAmount();
                    }
                    enrollments.setValue(enrollmentsList);
                } else {
                    enrollments.setValue(new ArrayList<>());
                }
                totalAnnualRevenue.setValue(String.format(Locale.US, "$%,.2f", total));
                revenueTrend.setValue("Total Lifetime Revenue");
            }

            @Override
            public void onError(String message) {
                totalAnnualRevenue.setValue("$0.00");
                revenueTrend.setValue("Error loading revenue");
                enrollments.setValue(new ArrayList<>());
            }
        });
    }

    public LiveData<List<Report>> getReports() { return reports; }
    public LiveData<List<Enrollment>> getEnrollments() { return enrollments; }
    public LiveData<String> getTotalAnnualRevenue() { return totalAnnualRevenue; }
    public LiveData<String> getRevenueTrend() { return revenueTrend; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
