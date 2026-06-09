package com.buhler.funktionierender_pepper.lang;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Locale;

public class SpeechManager {
    private static SpeechManager instance;
    private static LanguageManager languageManager;

    private SpeechManager() {
        /*
         * Singleton
         */
    }

    public static SpeechManager getInstance() {
        if (instance == null) {
            instance = new SpeechManager();
        }
        return instance;
    }

    public void setLanguageManager(LanguageManager lm) {
        languageManager = lm;
    }

    public void say(QiContext context, String toSay) {
        say(context, toSay, languageManager.getCurrent());
    }

    public void systemSay(QiContext context, String toSay) {
        say(context, toSay, SupportedLanguage.GERMAN);
    }

    private void say(QiContext context, String toSay, SupportedLanguage lang) {
        Log.i("SAYING", "Pepper is preparing to say something in " + languageManager.getCurrent().name() + ", content: " + toSay);

        Locale locale = new Locale(lang.getQiLang(), lang.getRegion());
        Say answerSay = SayBuilder.with(context)
                .withText(toSay)
                .withLocale(locale)
                .build();
        Log.i("SAYING", "Pepper is now saying something in " + languageManager.getCurrent().name() + ", content: " + toSay);
        answerSay.run();
    }
}
