package com.buhler.funktionierender_pepper.lang;

import android.content.Intent;
import android.speech.RecognizerIntent;

public class LanguageManager {
    private final Intent intent;
    private SupportedLanguage current;

    public LanguageManager(Intent intent) {
        this.intent = intent;
    }

    public void applyLanguage(SupportedLanguage lang) {
        current = lang;
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang.getAbbreviation());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.getAbbreviation());
    }

    public SupportedLanguage getCurrent() {
        if (current == null) {
            applyLanguage(SupportedLanguage.GERMAN);
        }
        return current;
    }
}
