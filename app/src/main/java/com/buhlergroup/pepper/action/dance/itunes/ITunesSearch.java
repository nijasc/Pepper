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

    public Result search(String query) throws IOException {
        String response = fetch(SEARCH_URL + "?entity=song&limit=5&term="
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
            throw new IOException("Kein Song für die Suche gefunden.");
        } catch (JSONException e) {
            throw new IOException("iTunes-Antwort nicht lesbar: " + e.getMessage());
        }
    }

    private String fetch(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", "PepperDance/1.0");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("iTunes-Suche fehlgeschlagen (HTTP " + code + ").");
            }
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            return content.toString();
        } finally {
            connection.disconnect();
        }
    }
}
