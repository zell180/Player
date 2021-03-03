package com.kingseiya.ilmito;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class Versioner {

    private static Versioner instance = null;
    private static int mainVersion = 120;
    private static int patchVersion = 142;
    private static boolean standaloneMode = true;

    private Versioner() {}

    public static Versioner getInstance() {
        if(instance == null)
            instance = new Versioner();
        return instance;
    }

    public static boolean isStandaloneMode() {
        return standaloneMode;
    }

    public static int getMainVersion() {
        return mainVersion;
    }

    public static int getPatchVersion() {
        return patchVersion;
    }

    public static boolean checkMainVersion(Context appContext) {
        return mainVersion > getCurrentMainVersion(appContext);
    }

    public static boolean checkPatchVersion(Context appContext) {
        return patchVersion > getCurrentPatchVersion(appContext);
    }

    public static void setCurrentMainVersion(Context appContext) throws PackageManager.NameNotFoundException {
        SharedPreferences settings = appContext.getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("mainVersion", mainVersion);
        editor.apply();
    }

    public static int getCurrentMainVersion(Context appContext) {
        SharedPreferences settings = appContext.getSharedPreferences("sharedPref", 0);
        return settings.getInt("mainVersion", 000);
    }

    public static void setCurrentPatchVersion(Context appContext) throws PackageManager.NameNotFoundException {
        SharedPreferences settings = appContext.getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("patchVersion", patchVersion);
        editor.apply();
    }

    public static int getCurrentPatchVersion(Context appContext) {
        SharedPreferences settings = appContext.getSharedPreferences("sharedPref", 0);
        return settings.getInt("patchVersion", 000);
    }

}