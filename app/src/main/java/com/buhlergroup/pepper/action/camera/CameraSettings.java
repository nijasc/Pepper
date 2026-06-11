package com.buhlergroup.pepper.action.camera;

import android.content.Context;
import android.content.SharedPreferences;

public final class CameraSettings {

    public static final int DEFAULT_PORT = 15740;

    private static final String PREFS = "camera_prefs";
    private static final String KEY_IP = "ip";
    private static final String KEY_PORT = "port";
    private static final String KEY_ENABLED = "enabled";

    private CameraSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getIp(Context context) {
        return prefs(context).getString(KEY_IP, "");
    }

    public static int getPort(Context context) {
        return prefs(context).getInt(KEY_PORT, DEFAULT_PORT);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void save(Context context, String ip, int port, boolean enabled) {
        prefs(context).edit()
                .putString(KEY_IP, ip == null ? "" : ip.trim())
                .putInt(KEY_PORT, port > 0 ? port : DEFAULT_PORT)
                .putBoolean(KEY_ENABLED, enabled)
                .apply();
    }

    public static boolean isActive(Context context) {
        return isEnabled(context) && !getIp(context).isEmpty();
    }
}
