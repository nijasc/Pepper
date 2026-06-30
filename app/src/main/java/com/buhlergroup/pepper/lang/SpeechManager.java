package com.buhlergroup.pepper.lang;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.buhlergroup.pepper.action.audio.AudioCoordinator;
import com.buhlergroup.pepper.action.dialogue.DialogueController;
import com.buhlergroup.pepper.action.thinking.ThinkingController;
import com.buhlergroup.pepper.openai.SystemSpeechRewriter;

public class SpeechManager {
    private volatile LanguageManager languageManager;

    private final Object speechLock = new Object();

    private SpeechManager() {
    }

    public static SpeechManager getInstance() {
        return Holder.INSTANCE;
    }

    public void setLanguageManager(LanguageManager lm) {
        languageManager = lm;
    }

    private SupportedLanguage currentOrDefault() {
        LanguageManager lm = languageManager;
        return lm != null ? lm.getCurrent() : SupportedLanguage.GERMAN;
    }

    public SupportedLanguage currentLanguage() {
        return currentOrDefault();
    }

    public void say(QiContext context, String toSay) {
        SupportedLanguage current = currentOrDefault();
        speak(context, toSay, new Locale(current.getQiLang(), current.getRegion()), current);
    }

    public void say(QiContext context, String toSay, String outputLangTag) {
        SupportedLanguage fallback = currentOrDefault();
        Locale locale = LocaleResolver.resolve(outputLangTag, fallback);
        speak(context, toSay, locale, fallback);
    }

    public void systemSay(QiContext context, String toSay) {
        SupportedLanguage target = currentOrDefault();
        String text = SystemSpeechRewriter.get().rewrite(context, toSay, target);
        speak(context, text, new Locale(target.getQiLang(), target.getRegion()), target);
    }

    public void sayStill(QiContext context, String toSay) {
        SupportedLanguage current = currentOrDefault();
        speak(context, toSay, new Locale(current.getQiLang(), current.getRegion()), current,
                BodyLanguageOption.DISABLED);
    }

    private void speak(QiContext context, String toSay, Locale locale, SupportedLanguage fallback) {
        speak(context, toSay, locale, fallback, BodyLanguageOption.NEUTRAL);
    }

    private void speak(QiContext context, String toSay, Locale locale, SupportedLanguage fallback,
                       BodyLanguageOption bodyLanguage) {
        synchronized (speechLock) {
            ThinkingController.get().stop();
            DialogueController.get().beginUtterance(toSay);
            AudioCoordinator.get().onSpeechStart();
            try {
                Say answerSay = SayBuilder.with(context)
                        .withText(toSay)
                        .withLocale(locale)
                        .withBodyLanguageOption(bodyLanguage)
                        .build();
                answerSay.run();
            } catch (Exception e) {
                Log.w("SAYING", "Say failed for requested locale, falling back to " + fallback.name() + ": " + e.getMessage());
                try {
                    Locale fallbackLocale = new Locale(fallback.getQiLang(), fallback.getRegion());
                    Say fallbackSay = SayBuilder.with(context)
                            .withText(toSay)
                            .withLocale(fallbackLocale)
                            .withBodyLanguageOption(bodyLanguage)
                            .build();
                    fallbackSay.run();
                } catch (Exception e2) {
                    Log.e("SAYING", "Fallback say failed: " + e2.getMessage());
                }
            } finally {
                AudioCoordinator.get().onSpeechEnd();
                DialogueController.get().endUtterance();
            }
        }
    }

    private static final class Holder {
        private static final SpeechManager INSTANCE = new SpeechManager();
    }
}
