package com.buhlergroup.pepper.action.dance.youtube;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YoutubeSearch {

    private static final String TAG = "YoutubeSearch";
    private static final Pattern VIDEO_ID = Pattern.compile("\"videoId\":\"([\\w-]{11})\"");
    private static final Pattern LENGTH = Pattern.compile("\"lengthSeconds\":\"(\\d+)\"");

    public static final class Result {
        public final String videoId;
        public final String title;
        public final long durationMs;

        Result(String videoId, String title, long durationMs) {
            this.videoId = videoId;
            this.title = title;
            this.durationMs = durationMs;
        }
    }

    public Result search(String query) throws IOException {
        String html = fetch("https://www.youtube.com/results?search_query="
                + URLEncoder.encode(query, "UTF-8") + "&sp=EgIQAQ%253D%253D");

        Matcher idMatcher = VIDEO_ID.matcher(html);
        if (!idMatcher.find()) {
            throw new IOException("Kein Video für die Suche gefunden.");
        }
        String videoId = idMatcher.group(1);

        long durationMs = 0L;
        Matcher lengthMatcher = LENGTH.matcher(html);
        if (lengthMatcher.find()) {
            try {
                durationMs = Long.parseLong(lengthMatcher.group(1)) * 1000L;
            } catch (NumberFormatException ignored) {
            }
        }
        Log.i(TAG, "Resolved '" + query + "' -> " + videoId);
        return new Result(videoId, query, durationMs);
    }

    private String fetch(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("YouTube-Suche fehlgeschlagen (HTTP " + code + ").");
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
