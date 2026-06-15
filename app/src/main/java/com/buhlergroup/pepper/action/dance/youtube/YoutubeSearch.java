package com.buhlergroup.pepper.action.dance.youtube;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YoutubeSearch {

    private static final String TAG = "YoutubeSearch";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String RENDERER_MARKER = "\"videoRenderer\":{";
    private static final Pattern VIDEO_ID = Pattern.compile("\"videoId\":\"([\\w-]{11})\"");
    private static final Pattern TITLE =
            Pattern.compile("\"title\":\\{\"runs\":\\[\\{\"text\":\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern LENGTH =
            Pattern.compile("\"simpleText\":\"(\\d{1,2}(?::\\d{2})+)\"");
    private static final long MIN_DURATION_MS = 30_000L;
    private static final long MAX_DURATION_MS = 600_000L;
    private static final int MAX_CANDIDATES = 8;

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

    public List<Result> search(String query) throws IOException {
        String html = fetch("https://www.youtube.com/results?search_query="
                + URLEncoder.encode(query, "UTF-8") + "&sp=EgIQAQ%253D%253D");
        List<Result> candidates = parseCandidates(html);
        if (candidates.isEmpty()) {
            throw new IOException("Kein Video für die Suche gefunden.");
        }
        Log.i(TAG, "Parsed " + candidates.size() + " candidate(s) for '" + query + "'");
        return candidates;
    }

    public boolean exists(String videoId) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://www.youtube.com/oembed?format=json&url=https://www.youtube.com/watch?v="
                            + videoId).openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            try {
                int code = connection.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "Video " + videoId + " not playable (HTTP " + code + ")");
                    return false;
                }
                return true;
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            Log.w(TAG, "Availability check failed for " + videoId + ": " + e.getMessage());
            return false;
        }
    }

    private List<Result> parseCandidates(String html) {
        List<Result> results = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        int from = html.indexOf(RENDERER_MARKER);
        while (from >= 0 && results.size() < MAX_CANDIDATES) {
            int start = from + RENDERER_MARKER.length();
            int next = html.indexOf(RENDERER_MARKER, start);
            String chunk = next >= 0 ? html.substring(start, next) : html.substring(start);
            from = next;

            Matcher idMatcher = VIDEO_ID.matcher(chunk);
            if (!idMatcher.find()) {
                continue;
            }
            String videoId = idMatcher.group(1);
            if (!seen.add(videoId)) {
                continue;
            }
            long durationMs = parseDuration(chunk);
            if (durationMs < MIN_DURATION_MS || durationMs > MAX_DURATION_MS) {
                continue;
            }
            results.add(new Result(videoId, parseTitle(chunk), durationMs));
        }
        return results;
    }

    private long parseDuration(String chunk) {
        int idx = chunk.indexOf("\"lengthText\"");
        if (idx < 0) {
            return 0L;
        }
        Matcher matcher = LENGTH.matcher(chunk.substring(idx));
        if (!matcher.find()) {
            return 0L;
        }
        long seconds = 0L;
        for (String part : matcher.group(1).split(":")) {
            try {
                seconds = seconds * 60 + Long.parseLong(part);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return seconds * 1000L;
    }

    private String parseTitle(String chunk) {
        Matcher matcher = TITLE.matcher(chunk);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String fetch(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", USER_AGENT);
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
