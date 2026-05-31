package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LanguageManager {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_VIETNAMESE = "vi";

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "app_language";

    private LanguageManager() {
    }

    public static void applySavedLanguage(Context context) {
        setAppLocale(getSavedLanguage(context));
    }

    public static void saveLanguage(Context context, String languageCode) {
        String safeCode = LANGUAGE_VIETNAMESE.equals(languageCode)
                ? LANGUAGE_VIETNAMESE
                : LANGUAGE_ENGLISH;
        getPrefs(context).edit().putString(KEY_LANGUAGE, safeCode).apply();
        setAppLocale(safeCode);
    }

    public static String getSavedLanguage(Context context) {
        String languageCode = getPrefs(context).getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
        return LANGUAGE_VIETNAMESE.equals(languageCode) ? LANGUAGE_VIETNAMESE : LANGUAGE_ENGLISH;
    }

    public static boolean isVietnamese(Context context) {
        return LANGUAGE_VIETNAMESE.equals(getSavedLanguage(context));
    }

    public static Context getLocalizedContext(Context context) {
        String languageCode = getSavedLanguage(context);
        Locale locale = Locale.forLanguageTag(languageCode);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static void setAppLocale(String languageCode) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode));
    }
}
