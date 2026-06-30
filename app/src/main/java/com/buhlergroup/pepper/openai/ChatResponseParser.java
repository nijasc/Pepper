package com.buhlergroup.pepper.openai;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure parsing for chat-completion responses: extracting the message content
 * and stripping the internal {@code [[lang:..]]} / {@code [[action:..]]}
 * machine markers. Extracted from {@code OpenAIService}; all methods are
 * static and free of instance state.
 */
final class ChatResponseParser {

    private static final Pattern LANG_TAG =
            Pattern.compile("\\[\\[\\s*lang\\s*:\\s*([A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?)\\s*\\]\\]");
    private static final Pattern ACTION_TAG =
            Pattern.compile("\\[\\[\\s*action\\s*:\\s*([A-Za-z0-9_]+)\\s*\\]\\]");

    private ChatResponseParser() {
    }

    /**
     * Result of {@link #extractLanguageTag(String)}: the cleaned text with all
     * markers removed, and the detected language tag (or {@code null}).
     */
    static final class LanguageTagResult {
        @Nullable
        final String text;
        @Nullable
        final String languageTag;

        LanguageTagResult(@Nullable String text, @Nullable String languageTag) {
            this.text = text;
            this.languageTag = languageTag;
        }
    }

    static String parseChatContent(String responseJson) throws IOException {
        try {
            return new JSONObject(responseJson)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            throw new IOException("Antwort ohne Inhalt: " + snippet(responseJson));
        }
    }

    /**
     * Strips the language and action markers from {@code text} and reports the
     * detected language tag. When {@code text} is {@code null} both fields of
     * the result are {@code null}.
     */
    static LanguageTagResult extractLanguageTag(@Nullable String text) {
        if (text == null) {
            return new LanguageTagResult(null, null);
        }
        Matcher matcher = LANG_TAG.matcher(text);
        String languageTag = matcher.find() ? matcher.group(1) : null;
        String cleaned = LANG_TAG.matcher(text).replaceAll("");
        String result = ACTION_TAG.matcher(cleaned).replaceAll("").trim();
        return new LanguageTagResult(result, languageTag);
    }

    private static String snippet(String json) {
        if (json == null) {
            return "null";
        }
        return json.length() <= 300 ? json : json.substring(0, 300) + "…";
    }
}
