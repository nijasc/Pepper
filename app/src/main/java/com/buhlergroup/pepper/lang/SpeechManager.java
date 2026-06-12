package com.buhlergroup.pepper.lang;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.buhlergroup.pepper.action.thinking.ThinkingController;
import com.buhlergroup.pepper.openai.SystemSpeechRewriter;

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

    public SupportedLanguage currentLanguage() {
        return languageManager.getCurrent();
    }

    public void say(QiContext context, String toSay) {
        SupportedLanguage current = languageManager.getCurrent();
        speak(context, toSay, new Locale(current.getQiLang(), current.getRegion()), current);
    }

    public void say(QiContext context, String toSay, String outputLangTag) {
        SupportedLanguage fallback = languageManager.getCurrent();
        Locale locale = LocaleResolver.resolve(outputLangTag, fallback);
        speak(context, toSay, locale, fallback);
    }

    public void systemSay(QiContext context, String toSay) {
        SupportedLanguage target = languageManager.getCurrent();
        String text = SystemSpeechRewriter.get().rewrite(context, toSay, target);
        speak(context, text, new Locale(target.getQiLang(), target.getRegion()), target);
    }

    private void speak(QiContext context, String toSay, Locale locale, SupportedLanguage fallback) {
        ThinkingController.get().stop();
        try {
            Say answerSay = SayBuilder.with(context)
                    .withText(toSay)
                    .withLocale(locale)
                    .build();
            answerSay.run();
        } catch (Exception e) {
            Log.w("SAYING", "Say failed for requested locale, falling back to " + fallback.name() + ": " + e.getMessage());
            try {
                Locale fallbackLocale = new Locale(fallback.getQiLang(), fallback.getRegion());
                Say fallbackSay = SayBuilder.with(context)
                        .withText(toSay)
                        .withLocale(fallbackLocale)
                        .build();
                fallbackSay.run();
            } catch (Exception e2) {
                Log.e("SAYING", "Fallback say failed: " + e2.getMessage());
            }
        }
    }
}
