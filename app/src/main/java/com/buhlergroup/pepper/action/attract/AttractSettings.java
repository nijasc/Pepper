package com.buhlergroup.pepper.action.attract;

import android.content.Context;
import android.content.SharedPreferences;

public final class AttractSettings {

    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_IDLE_MINUTES = 2;
    public static final int DEFAULT_GREET_SECONDS = 45;

    private static final String PREFS = "attract_prefs";
    private static final String KEY_ENABLED = "enabled";

    private AttractSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static int getIdleMinutes() {
        return DEFAULT_IDLE_MINUTES;
    }

    public static int getGreetSeconds() {
        return DEFAULT_GREET_SECONDS;
    }

    public static void save(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}
