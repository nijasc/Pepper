package com.buhlergroup.pepper.lang;

import android.util.Log;

import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import java.util.HashMap;
import java.util.Map;

public final class LocaleResolver {

    private static final String TAG = "LocaleResolver";

    private static final Map<String, String[]> MAP = new HashMap<>();

    static {
        MAP.put("de", new String[]{"GERMAN", "GERMANY"});
        MAP.put("en", new String[]{"ENGLISH", "UNITED_STATES"});
        MAP.put("fr", new String[]{"FRENCH", "FRANCE"});
        MAP.put("it", new String[]{"ITALIAN", "ITALY"});
        MAP.put("es", new String[]{"SPANISH", "SPAIN"});
        MAP.put("pt", new String[]{"PORTUGUESE", "BRAZIL"});
        MAP.put("nl", new String[]{"DUTCH", "NETHERLANDS"});
        MAP.put("ja", new String[]{"JAPANESE", "JAPAN"});
        MAP.put("ko", new String[]{"KOREAN", "KOREA"});
        MAP.put("zh", new String[]{"CHINESE", "CHINA"});
        MAP.put("ru", new String[]{"RUSSIAN", "RUSSIA"});
        MAP.put("ar", new String[]{"ARABIC", "SAUDI_ARABIA"});
        MAP.put("tr", new String[]{"TURKISH", "TURKEY"});
        MAP.put("pl", new String[]{"POLISH", "POLAND"});
        MAP.put("sv", new String[]{"SWEDISH", "SWEDEN"});
        MAP.put("da", new String[]{"DANISH", "DENMARK"});
        MAP.put("fi", new String[]{"FINNISH", "FINLAND"});
        MAP.put("nb", new String[]{"NORWEGIAN", "NORWAY"});
        MAP.put("no", new String[]{"NORWEGIAN", "NORWAY"});
        MAP.put("cs", new String[]{"CZECH", "CZECH_REPUBLIC"});
    }

    private LocaleResolver() {
    }

    public static Locale resolve(String tag, SupportedLanguage fallback) {
        Locale fallbackLocale = new Locale(fallback.getQiLang(), fallback.getRegion());
        if (tag == null || tag.trim().isEmpty()) {
            return fallbackLocale;
        }

        String[] parts = tag.trim().toLowerCase().split("[-_]");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return fallbackLocale;
        }
        String[] names = MAP.get(parts[0]);
        if (names == null) {
            Log.i(TAG, "No mapping for language tag '" + tag + "', falling back to " + fallback.name());
            return fallbackLocale;
        }

        try {
            Language language = Language.valueOf(names[0]);
            Region region;
            try {
                region = Region.valueOf(names[1]);
            } catch (IllegalArgumentException e) {
                region = fallback.getRegion();
            }
            return new Locale(language, region);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Language '" + names[0] + "' not supported by this QiSDK, falling back to " + fallback.name());
            return fallbackLocale;
        }
    }
}
