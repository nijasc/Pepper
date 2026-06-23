package com.buhlergroup.pepper.config;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Env {

    private static volatile Map<String, String> cache;

    private Env() {
    }

    public static String get(Context context, String key, String defaultValue) {
        Map<String, String> values = cache;
        if (values == null) {
            synchronized (Env.class) {
                values = cache;
                if (values == null) {
                    values = load(context);
                    cache = values;
                }
            }
        }
        String value = values.get(key);
        return value != null ? value : defaultValue;
    }

    private static Map<String, String> load(Context context) {
        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("env"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String currentKey = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(currentKey, value);
            }
        } catch (IOException e) {
            Log.e("Env", "Failed to read env asset: " + e.getMessage());
        }
        return values;
    }
}
