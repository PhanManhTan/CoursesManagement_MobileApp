package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.utils.LanguageManager;

public class CoursesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LanguageManager.applySavedLanguage(this);
    }
}
