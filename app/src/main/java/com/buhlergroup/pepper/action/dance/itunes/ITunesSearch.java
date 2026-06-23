package com.buhlergroup.pepper.action.dance.itunes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class ITunesSearch {

    private static final String TAG = "ITunesSearch";
    private static final String SEARCH_URL = "https://itunes.apple.com/search";
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 500L;

    public Result search(String query) throws IOException {
        Log.i(TAG, "Searching iTunes for: '" + query + "'");
        String response = fetch(SEARCH_URL + "?entity=song&limit=10&term="
                + URLEncoder.encode(query, "UTF-8"));
        try {
            JSONArray items = new JSONObject(response).getJSONArray("results");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String previewUrl = item.optString("previewUrl", "");
                if (previewUrl.isEmpty()) {
                    continue;
                }
                String trackId = String.valueOf(item.optLong("trackId"));
                String artist = item.optString("artistName", "");
                String track = item.optString("trackName", "");
                String title = artist.isEmpty() ? track : artist + " - " + track;
                Log.i(TAG, "Resolved '" + query + "' -> " + title + " (" + trackId + ")");
                return new Result(trackId, title, previewUrl, item.optLong("trackTimeMillis", 0L));
            }
            throw new IOException("Kein Song für die Suche gefunden: '" + query + "'");
        } catch (JSONException e) {
            throw new IOException("iTunes-Antwort nicht lesbar: " + e.getMessage());
        }
    }

    private String fetch(String urlString) throws IOException {
        IOException last = null;
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchOnce(urlString);
            } catch (RetryableException e) {
                last = e;
                Log.w(TAG, "iTunes attempt " + attempt + "/" + MAX_ATTEMPTS
                        + " failed: " + e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    sleep(backoff);
                    backoff *= 2;
                }
            }
        }
        throw last;
    }

    private String fetchOnce(String urlString) throws IOException {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
        } catch (IOException e) {
            throw new RetryableException(e.getMessage(), e);
        }
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", "PepperDance/1.0");
        try {
            int code;
            try {
                code = connection.getResponseCode();
            } catch (IOException e) {
                throw new RetryableException("Verbindung fehlgeschlagen: " + e.getMessage(), e);
            }
            if (code != HttpURLConnection.HTTP_OK) {
                if (code >= 500 || code == 429) {
                    throw new RetryableException("iTunes-Suche fehlgeschlagen (HTTP " + code + ").");
                }
                throw new IOException("iTunes-Suche fehlgeschlagen (HTTP " + code + ").");
            }
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            } catch (IOException e) {
                throw new RetryableException("Lesefehler: " + e.getMessage(), e);
            }
            return content.toString();
        } finally {
            connection.disconnect();
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static final class Result {
        public final String trackId;
        public final String title;
        public final String previewUrl;
        public final long durationMs;

        Result(String trackId, String title, String previewUrl, long durationMs) {
            this.trackId = trackId;
            this.title = title;
            this.previewUrl = previewUrl;
            this.durationMs = durationMs;
        }
    }

    private static final class RetryableException extends IOException {
        RetryableException(String message) {
            super(message);
        }

        RetryableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
