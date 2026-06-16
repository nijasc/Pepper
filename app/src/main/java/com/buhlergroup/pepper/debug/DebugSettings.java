package com.buhlergroup.pepper.debug;

import android.content.Context;
import android.content.SharedPreferences;

public final class DebugSettings {

    public static final boolean DEFAULT_ENABLED = false;

    private static final String PREFS = "debug_prefs";
    private static final String KEY_ENABLED = "enabled";

    private DebugSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}
