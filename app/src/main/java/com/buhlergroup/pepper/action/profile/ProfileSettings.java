package com.buhlergroup.pepper.action.profile;

import android.content.Context;
import android.content.SharedPreferences;

public final class ProfileSettings {

    private static final String PREFS = "profile_prefs";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String KEY_SEEDED = "seeded";

    private ProfileSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getActiveProfileId(Context context) {
        return prefs(context).getString(KEY_ACTIVE_PROFILE_ID, "");
    }

    public static void setActiveProfileId(Context context, String profileId) {
        prefs(context).edit().putString(KEY_ACTIVE_PROFILE_ID, profileId == null ? "" : profileId).apply();
    }

    public static boolean isSeeded(Context context) {
        return prefs(context).getBoolean(KEY_SEEDED, false);
    }

    public static void setSeeded(Context context, boolean seeded) {
        prefs(context).edit().putBoolean(KEY_SEEDED, seeded).apply();
    }
}
