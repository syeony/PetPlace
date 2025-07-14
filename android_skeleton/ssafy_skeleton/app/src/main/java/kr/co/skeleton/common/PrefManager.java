package kr.co.skeleton.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PrefManager {

    public static void setToken(Context context, String token) {
        if (context != null && !TextUtils.isEmpty(token)) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            pref.edit().putString(Constant.TOKEN, token).apply();
        }
    }
    public static String getToken(Context context) {
        String token = "";
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            token = pref.getString(Constant.TOKEN, "");
        }

        return token;
    }
    public static void setFbToken(Context context, String token) {
        if (context != null && !TextUtils.isEmpty(token)) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            pref.edit().putString(Constant.FBTOKEN, token).apply();
        }
    }

    public static String getFbToken(Context context) {
        String token = "";
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            token = pref.getString(Constant.FBTOKEN, "");
        }

        return token;
    }

    public static void setAutoLogin(Context context, Boolean autologin) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            pref.edit().putBoolean(Constant.AUTO_LOGIN, autologin).apply();
        }
    }

    public static Boolean getAutoLogin(Context context) {
        Boolean autoLogin = false;
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            autoLogin = pref.getBoolean(Constant.AUTO_LOGIN, false);
        }

        return autoLogin;
    }

    public static void setUUID(Context context, String uuid) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            pref.edit().putString(Constant.UUID, uuid).apply();
        }
    }

    public static String getUUID(Context context) {
        String uuid = "";
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            uuid = pref.getString(Constant.AUTO_LOGIN, "");
        }

        return uuid;
    }

    public static void setUserKey(Context context, Integer key) {
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            pref.edit().putInt(Constant.USER_KEY, key).apply();
        }
    }

    public static Integer getUserKey(Context context) {
        Integer key = 0;
        if (context != null) {
            SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            key = pref.getInt(Constant.USER_KEY,0);
        }

        return key;
    }

    public static void logout(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Constant.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        pref.edit().remove(Constant.FBTOKEN).apply();
        pref.edit().remove(Constant.TOKEN).apply();
        pref.edit().remove(Constant.AUTO_LOGIN).apply();
        pref.edit().remove(Constant.UUID).apply();
        pref.edit().remove(Constant.USER_KEY).apply();
    }



}
