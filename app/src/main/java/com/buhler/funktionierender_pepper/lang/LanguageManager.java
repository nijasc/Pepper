package com.buhler.funktionierender_pepper.lang;

import android.content.Intent;
import android.speech.RecognizerIntent;

public class LanguageManager {

    public interface LanguageChangeListener {
        void onLanguageChanged(SupportedLanguage lang);
    }

    private final Intent intent;
    private SupportedLanguage current;
    private LanguageChangeListener listener;

    public LanguageManager(Intent intent) {
        this.intent = intent;
    }

    public void setLanguageChangeListener(LanguageChangeListener listener) {
        this.listener = listener;
    }

    public void applyLanguage(SupportedLanguage lang) {
        current = lang;
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang.getAbbreviation());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.getAbbreviation());
        if (listener != null) {
            listener.onLanguageChanged(lang);
        }
    }

    public SupportedLanguage getCurrent() {
        if (current == null) {
            applyLanguage(SupportedLanguage.GERMAN);
        }
        return current;
    }
}
