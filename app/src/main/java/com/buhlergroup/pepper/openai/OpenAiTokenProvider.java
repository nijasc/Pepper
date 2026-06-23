package com.buhlergroup.pepper.openai;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class OpenAiTokenProvider {

    private String cachedToken;

    public synchronized String getToken(Context context) {
        if (cachedToken != null) {
            return cachedToken;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("env"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eq = line.indexOf('=');
                if (eq < 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();

                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }

                if ("OPENAI_API_TOKEN".equals(key)) {
                    cachedToken = value;
                }
            }
        } catch (IOException e) {
            Log.e("TOKENAUTH", "Failed to read env asset", e);
        }
        if (cachedToken == null) {
            Log.i("TOKENAUTH", "no token");
        } else if (BuildConfig.DEBUG) {
            Log.i("TOKENAUTH", "token loaded (" + mask(cachedToken) + ")");
        } else {
            Log.i("TOKENAUTH", "token loaded");
        }
        return cachedToken;
    }

    private String mask(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }
}
