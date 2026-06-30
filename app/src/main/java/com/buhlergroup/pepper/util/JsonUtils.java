package com.buhlergroup.pepper.util;

public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Returns the substring from the first '{' to the last '}' (inclusive),
     * or "{}" when no balanced-looking object delimiters are present.
     */
    public static String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            return "{}";
        }
        return content.substring(start, end + 1);
    }
}
