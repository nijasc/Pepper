package com.buhlergroup.pepper.action.volume;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class NumberWords {

    private static final Map<String, Integer> TENS = new LinkedHashMap<>();
    private static final Map<String, Integer> TEENS = new LinkedHashMap<>();
    private static final Map<String, Integer> UNITS = new LinkedHashMap<>();

    private static final Pattern COMPOUND = Pattern.compile(
            "(einund|zweiund|dreiund|vierund|fünfund|fuenfund|sechsund|siebenund|achtund|neunund)"
                    + "(zwanzig|dreißig|dreissig|vierzig|fünfzig|fuenfzig|sechzig|siebzig|achtzig|neunzig)");

    static {
        TENS.put("zwanzig", 20);
        TENS.put("dreißig", 30);
        TENS.put("dreissig", 30);
        TENS.put("vierzig", 40);
        TENS.put("fünfzig", 50);
        TENS.put("fuenfzig", 50);
        TENS.put("sechzig", 60);
        TENS.put("siebzig", 70);
        TENS.put("achtzig", 80);
        TENS.put("neunzig", 90);
        TENS.put("twenty", 20);
        TENS.put("thirty", 30);
        TENS.put("forty", 40);
        TENS.put("fifty", 50);
        TENS.put("sixty", 60);
        TENS.put("seventy", 70);
        TENS.put("eighty", 80);
        TENS.put("ninety", 90);

        TEENS.put("dreizehn", 13);
        TEENS.put("vierzehn", 14);
        TEENS.put("fünfzehn", 15);
        TEENS.put("fuenfzehn", 15);
        TEENS.put("sechzehn", 16);
        TEENS.put("siebzehn", 17);
        TEENS.put("achtzehn", 18);
        TEENS.put("neunzehn", 19);
        TEENS.put("zwölf", 12);
        TEENS.put("zwoelf", 12);
        TEENS.put("elf", 11);
        TEENS.put("zehn", 10);
        TEENS.put("thirteen", 13);
        TEENS.put("fourteen", 14);
        TEENS.put("fifteen", 15);
        TEENS.put("sixteen", 16);
        TEENS.put("seventeen", 17);
        TEENS.put("eighteen", 18);
        TEENS.put("nineteen", 19);
        TEENS.put("twelve", 12);
        TEENS.put("eleven", 11);
        TEENS.put("ten", 10);

        UNITS.put("eins", 1);
        UNITS.put("zwei", 2);
        UNITS.put("drei", 3);
        UNITS.put("vier", 4);
        UNITS.put("fünf", 5);
        UNITS.put("fuenf", 5);
        UNITS.put("sechs", 6);
        UNITS.put("sieben", 7);
        UNITS.put("acht", 8);
        UNITS.put("neun", 9);
        UNITS.put("one", 1);
        UNITS.put("two", 2);
        UNITS.put("three", 3);
        UNITS.put("four", 4);
        UNITS.put("five", 5);
        UNITS.put("six", 6);
        UNITS.put("seven", 7);
        UNITS.put("eight", 8);
        UNITS.put("nine", 9);
    }

    private NumberWords() {
    }

    static Integer parse(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.toLowerCase(Locale.ROOT);

        if (containsAny(t, "hundert", "hundred", "maximal", "maximum", "voll", "komplett")) {
            return 100;
        }
        if (containsAny(t, "halb", "hälfte", "haelfte", "half")) {
            return 50;
        }

        Matcher m = COMPOUND.matcher(t);
        if (m.find()) {
            Integer unit = UNITS.get(m.group(1).replace("und", ""));
            Integer tens = TENS.get(m.group(2));
            if (tens != null) {
                return tens + (unit != null ? unit : 0);
            }
        }

        for (Map.Entry<String, Integer> entry : TENS.entrySet()) {
            int idx = t.indexOf(entry.getKey());
            if (idx >= 0) {
                Integer unit = firstUnit(t.substring(idx + entry.getKey().length()));
                return entry.getValue() + (unit != null ? unit : 0);
            }
        }

        for (Map.Entry<String, Integer> entry : TEENS.entrySet()) {
            if (t.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        Integer unit = firstUnit(t);
        if (unit != null) {
            return unit;
        }

        if (containsAny(t, "null", "zero", "stumm", "mute", "leise", "aus")) {
            return 0;
        }
        return null;
    }

    private static Integer firstUnit(String text) {
        for (Map.Entry<String, Integer> entry : UNITS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
