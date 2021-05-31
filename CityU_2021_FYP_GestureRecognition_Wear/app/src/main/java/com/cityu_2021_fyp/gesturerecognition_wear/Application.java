package com.cityu_2021_fyp.gesturerecognition_wear;

import android.os.Environment;

import androidx.preference.PreferenceManager;

import java.io.File;

import settings.AppSettings;

public class Application extends android.app.Application{
    private AppSettings settings;

    public AppSettings getSettings() {
        return settings;
    }

    @Override
    public void onCreate() {
        //import "android.preference.PreferenceManager" change to "androidx.preference.PreferenceManager";
        settings = new AppSettings(PreferenceManager.getDefaultSharedPreferences(this));
        settings.load();

        if (settings.getWorkingDir() == null) {
            File extDir = Environment.getDataDirectory();
            if (extDir != null) {
                settings.setWorkingDir(extDir.getAbsolutePath());
                settings.saveDeferred();
            }
        }

        super.onCreate();
    }
}
