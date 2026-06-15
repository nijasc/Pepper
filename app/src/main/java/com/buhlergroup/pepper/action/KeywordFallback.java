package com.buhlergroup.pepper.action;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class KeywordFallback {

    private static final Map<String, String> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("high five", "HighFiveAction");
        KEYWORDS.put("highfive", "HighFiveAction");
        KEYWORDS.put("abklatsch", "HighFiveAction");
        KEYWORDS.put("selfie", "SelfieAction");
        KEYWORDS.put("foto", "SelfieAction");
        KEYWORDS.put("photo", "SelfieAction");
        KEYWORDS.put("memory", "MemoryGameAction");
        KEYWORDS.put("gedächtnis", "MemoryGameAction");
        KEYWORDS.put("gedaechtnis", "MemoryGameAction");
        KEYWORDS.put("lautstärke", "ChangeVolumeAction");
        KEYWORDS.put("lautstaerke", "ChangeVolumeAction");
        KEYWORDS.put("lauter", "ChangeVolumeAction");
        KEYWORDS.put("leiser", "ChangeVolumeAction");
        KEYWORDS.put("volume", "ChangeVolumeAction");
        KEYWORDS.put("tanz", "DanceAction");
        KEYWORDS.put("dance", "DanceAction");
    }

    private KeywordFallback() {
    }

    public static String match(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String text = input.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
