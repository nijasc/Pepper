package com.buhlergroup.pepper.stats;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class Stats {

    public static final String INTERACTIONS = "interactions";
    public static final String SELFIES = "selfies";
    public static final String RAFFLE_JOINS = "raffle_joins";
    public static final String ERRORS = "errors";
    public static final String ACTION_PREFIX = "action.";

    private static final String PREFS = "stats_prefs";

    private Stats() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static void increment(Context context, String event) {
        if (context == null || event == null) {
            return;
        }
        String key = today() + "." + event;
        SharedPreferences p = prefs(context);
        p.edit().putInt(key, p.getInt(key, 0) + 1).apply();
    }

    public static Map<String, Integer> forDay(Context context, String date) {
        Map<String, Integer> result = new TreeMap<>();
        String prefix = date + ".";
        for (Map.Entry<String, ?> entry : prefs(context).getAll().entrySet()) {
            if (entry.getKey().startsWith(prefix) && entry.getValue() instanceof Integer) {
                result.put(entry.getKey().substring(prefix.length()), (Integer) entry.getValue());
            }
        }
        return result;
    }
}
