package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.content.SharedPreferences;

public final class NavigationSettings {

    public static final boolean DEFAULT_AUTO_LOCALIZE = false;
    public static final int DEFAULT_LOCALIZE_TIMEOUT_SECONDS = 40;

    private static final String PREFS = "navigation_prefs";
    private static final String KEY_AUTO_LOCALIZE = "auto_localize";
    private static final String KEY_DEFAULT_SCAN_ID = "default_scan_id";

    private NavigationSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isAutoLocalize(Context context) {
        return prefs(context).getBoolean(KEY_AUTO_LOCALIZE, DEFAULT_AUTO_LOCALIZE);
    }

    public static String getDefaultScanId(Context context) {
        return prefs(context).getString(KEY_DEFAULT_SCAN_ID, "");
    }

    public static int getLocalizeTimeoutSeconds(Context context) {
        return DEFAULT_LOCALIZE_TIMEOUT_SECONDS;
    }

    public static long getLocalizeTimeoutMs(Context context) {
        return getLocalizeTimeoutSeconds(context) * 1000L;
    }

    public static void save(Context context, boolean autoLocalize, String defaultScanId) {
        prefs(context).edit()
                .putBoolean(KEY_AUTO_LOCALIZE, autoLocalize)
                .putString(KEY_DEFAULT_SCAN_ID, defaultScanId == null ? "" : defaultScanId)
                .apply();
    }
}
