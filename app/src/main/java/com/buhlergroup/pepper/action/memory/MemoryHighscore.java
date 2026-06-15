package com.buhlergroup.pepper.action.memory;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public final class MemoryHighscore {

    private static final String PREFS = "memory_prefs";

    private MemoryHighscore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(String difficulty) {
        String d = difficulty == null ? "default" : difficulty.toLowerCase(Locale.ROOT).trim();
        return "highscore_" + d;
    }

    public static int get(Context context, String difficulty) {
        return prefs(context).getInt(key(difficulty), 0);
    }

    public static boolean submit(Context context, String difficulty, int score) {
        if (score > get(context, difficulty)) {
            prefs(context).edit().putInt(key(difficulty), score).apply();
            return true;
        }
        return false;
    }
}
