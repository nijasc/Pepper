package com.buhlergroup.pepper.action.attract;

import android.content.Context;
import android.content.SharedPreferences;

public final class AttractSettings {

    public static final boolean DEFAULT_ENABLED = false;
    public static final int DEFAULT_IDLE_MINUTES = 2;
    public static final int DEFAULT_GREET_SECONDS = 45;

    private static final String PREFS = "attract_prefs";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_IDLE_MINUTES = "idle_minutes";
    private static final String KEY_GREET_SECONDS = "greet_seconds";

    private AttractSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static int getIdleMinutes(Context context) {
        return Math.max(1, prefs(context).getInt(KEY_IDLE_MINUTES, DEFAULT_IDLE_MINUTES));
    }

    public static int getGreetSeconds(Context context) {
        return Math.max(5, prefs(context).getInt(KEY_GREET_SECONDS, DEFAULT_GREET_SECONDS));
    }

    public static void save(Context context, boolean enabled, int idleMinutes, int greetSeconds) {
        prefs(context).edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_IDLE_MINUTES, Math.max(1, idleMinutes))
                .putInt(KEY_GREET_SECONDS, Math.max(5, greetSeconds))
                .apply();
    }
}
