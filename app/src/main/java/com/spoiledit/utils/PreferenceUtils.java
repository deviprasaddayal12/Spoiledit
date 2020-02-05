package com.spoiledit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spoiledit.BuildConfig;
import com.spoiledit.activities.SplashActivity;
import com.spoiledit.models.UserModel;

import java.lang.reflect.Type;

public final class PreferenceUtils {
    public static final String TAG = PreferenceUtils.class.getCanonicalName();

    private static final String PREF_NAME = BuildConfig.APPLICATION_ID;

    public static final String KEY_LOGIN_STATUS = PREF_NAME + ".login_status";

    public static final String KEY_USERNAME = PREF_NAME + ".username";
    public static final String KEY_PASSWORD = PREF_NAME + ".password";

    public static final String KEY_USER_MODEL = PREF_NAME + ".user_model";

    private PreferenceUtils() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveString(Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static void deleteString(Context context, String key) {
        getSharedPreferences(context).edit().remove(key).apply();
    }

    public static String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key, null);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }

    public static void saveInt(Context context, String key, int value) {
        getSharedPreferences(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key) {
        return getSharedPreferences(context).getInt(key, 0);
    }

    public static void clearPreferences(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

    public static void saveCredentials(Context context, String[] credentials) {
        saveString(context, KEY_USERNAME, credentials[0]);
        saveString(context, KEY_PASSWORD, credentials[1]);
    }

    public static String[] credentials(Context context) {
        return new String[] {
            getString(context, KEY_USERNAME), getString(context, KEY_PASSWORD)
        };
    }

    public static void saveLoginStatus(Context context, int loginStatus) {
        saveInt(context, KEY_LOGIN_STATUS, loginStatus);
    }

    public static int loginStatus(Context context) {
        return getInt(context, KEY_LOGIN_STATUS);
    }

    public static UserModel getUserModel(Context context) {
        return new Gson().fromJson(getString(context, KEY_USER_MODEL), UserModel.class);
    }

    public static void saveUserModel(Context context, UserModel userModel) {
        saveString(context, KEY_USER_MODEL, new Gson().toJson(userModel, UserModel.class));
    }
}
