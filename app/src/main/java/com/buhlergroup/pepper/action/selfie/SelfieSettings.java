package com.buhlergroup.pepper.action.selfie;

import android.content.Context;
import android.content.SharedPreferences;

public final class SelfieSettings {

    public static final int DEFAULT_RETENTION_DAYS = 14;

    private static final String PREFS = "selfie_prefs";
    private static final String KEY_RETENTION_DAYS = "retention_days";

    private SelfieSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getRetentionDays(Context context) {
        return prefs(context).getInt(KEY_RETENTION_DAYS, DEFAULT_RETENTION_DAYS);
    }

    public static void setRetentionDays(Context context, int days) {
        prefs(context).edit().putInt(KEY_RETENTION_DAYS, Math.max(0, days)).apply();
    }
}
