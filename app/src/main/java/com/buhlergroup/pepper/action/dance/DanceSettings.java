package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.content.SharedPreferences;

public final class DanceSettings {

    public static final int DEFAULT_DURATION_SECONDS = 25;

    private static final String PREFS = "dance_prefs";
    private static final String KEY_DEFAULT_DANCE_ID = "default_dance_id";

    private DanceSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getDefaultDanceId(Context context) {
        return prefs(context).getString(KEY_DEFAULT_DANCE_ID, "");
    }

    public static int getDefaultDurationSeconds(Context context) {
        return DEFAULT_DURATION_SECONDS;
    }

    public static long getDefaultDurationMs(Context context) {
        return getDefaultDurationSeconds(context) * 1000L;
    }

    public static void save(Context context, String defaultDanceId) {
        prefs(context).edit()
                .putString(KEY_DEFAULT_DANCE_ID, defaultDanceId == null ? "" : defaultDanceId)
                .apply();
    }
}
