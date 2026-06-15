package com.buhlergroup.pepper.lang;

public final class SystemMessages {

    private SystemMessages() {
    }

    public static String offline(SupportedLanguage lang) {
        if (lang == SupportedLanguage.ENGLISH) {
            return "I have no connection right now. Please try again in a moment.";
        }
        return "Ich habe gerade keine Verbindung. Bitte versuche es gleich noch einmal.";
    }

    public static String notUnderstood(SupportedLanguage lang) {
        if (lang == SupportedLanguage.ENGLISH) {
            return "Sorry, I didn't quite catch that.";
        }
        return "Entschuldige, das habe ich gerade nicht verstanden.";
    }
}
