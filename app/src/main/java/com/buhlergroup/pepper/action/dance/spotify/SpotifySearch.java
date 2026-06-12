package com.buhlergroup.pepper.action.dance.spotify;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.buhlergroup.pepper.config.Env;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class SpotifySearch {

    private static final String TAG = "SpotifySearch";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String SEARCH_URL = "https://api.spotify.com/v1/search";
    private static final long TOKEN_SAFETY_MS = 60000L;

    private static volatile String cachedToken;
    private static volatile long tokenExpiresAt;

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

    public boolean isConfigured(Context context) {
        return clientId(context) != null && clientSecret(context) != null;
    }

    public Result search(Context context, String query) throws IOException {
        String token = token(context);
        String url = SEARCH_URL + "?type=track&limit=1&q=" + URLEncoder.encode(query, "UTF-8");
        String response = request(url, "GET", null, "Bearer " + token);
        try {
            JSONObject tracks = new JSONObject(response).getJSONObject("tracks");
            JSONArray items = tracks.getJSONArray("items");
            if (items.length() == 0) {
                throw new IOException("Kein Spotify-Track für die Suche gefunden.");
            }
            JSONObject track = items.getJSONObject(0);
            String title = track.getString("name");
            JSONArray artists = track.optJSONArray("artists");
            if (artists != null && artists.length() > 0) {
                String artist = artists.getJSONObject(0).optString("name", "");
                if (!artist.isEmpty()) {
                    title = artist + " - " + title;
                }
            }
            String previewUrl = track.isNull("preview_url")
                    ? null : track.getString("preview_url");
            Result result = new Result(track.getString("id"), title,
                    previewUrl, track.optLong("duration_ms", 0L));
            Log.i(TAG, "Resolved '" + query + "' -> " + result.trackId
                    + (previewUrl == null ? " (no preview)" : " (preview available)"));
            return result;
        } catch (JSONException e) {
            throw new IOException("Spotify-Antwort nicht lesbar: " + e.getMessage());
        }
    }

    private synchronized String token(Context context) throws IOException {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedToken;
        }
        String clientId = clientId(context);
        String clientSecret = clientSecret(context);
        if (clientId == null || clientSecret == null) {
            throw new IOException("Spotify-Zugangsdaten fehlen (SPOTIFY_CLIENT_ID/SPOTIFY_CLIENT_SECRET).");
        }
        String basic = Base64.encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String response = request(TOKEN_URL, "POST",
                "grant_type=client_credentials", "Basic " + basic);
        try {
            JSONObject json = new JSONObject(response);
            cachedToken = json.getString("access_token");
            long expiresInMs = json.optLong("expires_in", 3600L) * 1000L;
            tokenExpiresAt = System.currentTimeMillis() + expiresInMs - TOKEN_SAFETY_MS;
            return cachedToken;
        } catch (JSONException e) {
            throw new IOException("Spotify-Token-Antwort nicht lesbar: " + e.getMessage());
        }
    }

    private String request(String urlString, String method, String formBody, String authorization)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", authorization);
        try {
            if (formBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(formBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            int code = connection.getResponseCode();
            InputStream stream = code >= 400
                    ? connection.getErrorStream() : connection.getInputStream();
            String body = readAll(stream);
            if (code >= 400) {
                throw new IOException("Spotify-Anfrage fehlgeschlagen (HTTP " + code + "): " + body);
            }
            return body;
        } finally {
            connection.disconnect();
        }
    }

    private String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private String clientId(Context context) {
        return emptyToNull(Env.get(context, "SPOTIFY_CLIENT_ID", null));
    }

    private String clientSecret(Context context) {
        return emptyToNull(Env.get(context, "SPOTIFY_CLIENT_SECRET", null));
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
