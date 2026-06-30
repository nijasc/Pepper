package com.buhlergroup.pepper.action.hold;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persistence for the lifetime "drinks held" counter.
 * Pure SharedPreferences read/write; no session or UI state.
 */
final class HeldCountStore {

    private static final String PREFS = "pepper_hold";
    private static final String KEY_HELD_COUNT = "beers_held";

    private HeldCountStore() {
    }

    static int increment(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int count = prefs.getInt(KEY_HELD_COUNT, 0) + 1;
            prefs.edit().putInt(KEY_HELD_COUNT, count).apply();
            return count;
        } catch (Exception e) {
            return 1;
        }
    }
}
