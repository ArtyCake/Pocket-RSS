package com.artycake.pocketrss.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by artycake on 2/27/17.
 */

public class UserPrefs {
    private static UserPrefs instance = null;
    private SharedPreferences preferences;

    public static final String USE_DARK_THEME = "use_dark_theme";
    public static final String FIRST_LAUNCH = "first_launch";
    public static final String IMAGE_LOADING = "image_loading";
    public static final String IL_WIFI_ONLY = "il_wifi_only";
    public static final String IL_ALWAYS = "il_always";
    public static final String IL_NEVER = "il_never";
    public static final String USE_IN_APP_BROWSER = "use_in_app_browser";
    public static final String UPDATE_PERIOD = "update_period";
    public static final String UPDATE_WIFI_ONLY = "update_wifi_only";
    public static final String SHOULD_UPLOAD_NEWS = "should_upload_news";
    public static final String STORAGE_TIME = "storage_time";
    public static final String ASK_FOR_RATE = "ask_for_rate";
    public static final String THEME_CHANGED = "theme_changed";
    public static final int SOURCES_UNTIL_RATE = 3;

    public static UserPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new UserPrefs(context);
        }
        return instance;
    }

    private UserPrefs(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void putPreferences(String name, String value) {
        preferences.edit().putString(name, value).apply();
    }

    public void putPreferences(String name, int value) {
        preferences.edit().putInt(name, value).apply();
    }

    public void putPreferences(String name, long value) {
        preferences.edit().putLong(name, value).apply();
    }

    public void putPreferences(String name, boolean value) {
        preferences.edit().putBoolean(name, value).apply();
    }

    public String getStringPref(String name, @Nullable String defaultValue) {
        return preferences.getString(name, defaultValue);
    }

    public int getIntPref(String name, int defaultValue) {
        return preferences.getInt(name, defaultValue);
    }

    public long getLongPref(String name, long defaultValue) {
        return preferences.getLong(name, defaultValue);
    }

    public boolean getBoolPref(String name, boolean defaultValue) {
        return preferences.getBoolean(name, defaultValue);
    }
}
