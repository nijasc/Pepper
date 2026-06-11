package com.buhler.funktionierender_pepper.config;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Env {

    private Env() {
    }

    public static String get(Context context, String key, String defaultValue) {
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
                if (key.equals(currentKey)) {
                    return value;
                }
            }
        } catch (IOException e) {
            Log.e("Env", "Failed to read env asset: " + e.getMessage());
        }
        return defaultValue;
    }
}
