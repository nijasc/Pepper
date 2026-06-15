package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.content.SharedPreferences;

public final class AdminSettings {

    public static final String DEFAULT_PIN = "1019";

    private static final String PREFS = "admin_prefs";
    private static final String KEY_PIN = "pin";

    private AdminSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getPin(Context context) {
        String pin = prefs(context).getString(KEY_PIN, DEFAULT_PIN);
        return pin == null || pin.isEmpty() ? DEFAULT_PIN : pin;
    }

    public static void setPin(Context context, String pin) {
        if (pin == null || pin.isEmpty()) {
            return;
        }
        prefs(context).edit().putString(KEY_PIN, pin).apply();
    }
}
