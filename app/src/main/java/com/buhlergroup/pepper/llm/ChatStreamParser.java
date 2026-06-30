package com.buhlergroup.pepper.llm;

import android.util.Log;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatStreamParser {

    private static final String TAG = "ChatStreamParser";

    private static final Pattern LEADING_MARKER =
            Pattern.compile("^\\s*\\[\\[\\s*(lang|action)\\s*:\\s*([^\\]\\s]+)\\s*\\]\\]");
    private static final Pattern LANG_VALUE =
            Pattern.compile("[A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?");
    private static final Pattern ACTION_TAG =
            Pattern.compile("\\[\\[\\s*action\\s*:\\s*([A-Za-z0-9_]+)\\s*\\]\\]");

    private String lastLanguageTag;

    @Nullable
    public String lastLanguageTag() {
        return lastLanguageTag;
    }

    public String parse(BufferedReader reader, OpenAIService.StreamListener listener, long startedMs)
            throws IOException {
        StringBuilder pending = new StringBuilder();
        StringBuilder full = new StringBuilder();
        boolean markerPhase = true;
        boolean firstSentence = true;

        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("data:")) {
                continue;
            }
            String data = line.substring(5).trim();
            if (data.isEmpty() || "[DONE]".equals(data)) {
                if ("[DONE]".equals(data)) {
                    break;
                }
                continue;
            }
            String delta = extractDelta(data);
            if (delta == null || delta.isEmpty()) {
                continue;
            }
            pending.append(delta);
            if (markerPhase) {
                int state = consumeMarkers(pending, listener);
                if (state < 0) {
                    return null;
                }
                if (state == 0) {
                    continue;
                }
                markerPhase = false;
            }
            String sentence;
            while ((sentence = extractSentence(pending, false)) != null) {
                if (firstSentence) {
                    Log.i("LATENCY", "first streamed sentence after "
                            + (System.currentTimeMillis() - startedMs) + "ms");
                    firstSentence = false;
                }
                listener.onSentence(sentence, lastLanguageTag);
                full.append(sentence).append(' ');
            }
        }

        if (markerPhase && consumeMarkers(pending, listener) < 0) {
            return null;
        }
        String tail;
        while ((tail = extractSentence(pending, true)) != null) {
            listener.onSentence(tail, lastLanguageTag);
            full.append(tail).append(' ');
        }
        return ACTION_TAG.matcher(full.toString()).replaceAll("").trim();
    }

    private String extractDelta(String data) {
        try {
            JSONObject choice = new JSONObject(data).getJSONArray("choices").getJSONObject(0);
            JSONObject delta = choice.optJSONObject("delta");
            return delta == null ? "" : delta.optString("content", "");
        } catch (Exception e) {
            Log.d(TAG, "extractDelta failed", e);
            return null;
        }
    }

    private int consumeMarkers(StringBuilder pending, OpenAIService.StreamListener listener) {
        while (true) {
            Matcher matcher = LEADING_MARKER.matcher(pending);
            if (matcher.find()) {
                String kind = matcher.group(1);
                String value = matcher.group(2);
                pending.delete(0, matcher.end());
                if ("lang".equals(kind)) {
                    if (LANG_VALUE.matcher(Objects.requireNonNull(value)).matches()) {
                        lastLanguageTag = value;
                    }
                } else if (!listener.onAction(value)) {
                    return -1;
                }
                continue;
            }
            String current = pending.toString();
            String trimmed = current.trim();
            if (trimmed.isEmpty()) {
                return 0;
            }
            if (trimmed.startsWith("[") && current.length() < 80) {
                return 0;
            }
            return 1;
        }
    }

    private String extractSentence(StringBuilder pending, boolean flush) {
        int length = pending.length();
        for (int i = 0; i < length; i++) {
            char ch = pending.charAt(i);
            if (ch == '.' || ch == '!' || ch == '?' || ch == '…') {
                boolean boundary = i == length - 1
                        ? flush
                        : Character.isWhitespace(pending.charAt(i + 1));
                if (boundary && i >= 2) {
                    String sentence = pending.substring(0, i + 1).trim();
                    pending.delete(0, i + 1);
                    if (!sentence.isEmpty()) {
                        return sentence;
                    }
                }
            }
        }
        if (flush) {
            String rest = pending.toString().trim();
            pending.setLength(0);
            return rest.isEmpty() ? null : rest;
        }
        if (length > 300) {
            String chunk = pending.substring(0, length).trim();
            pending.setLength(0);
            return chunk.isEmpty() ? null : chunk;
        }
        return null;
    }
}
