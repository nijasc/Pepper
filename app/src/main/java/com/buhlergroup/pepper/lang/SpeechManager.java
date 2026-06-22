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
    private static SpeechManager instance;
    private static LanguageManager languageManager;

    private SpeechManager() {
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

    /**
     * Spricht ohne begleitende Körpersprache (Arme/Hände bleiben ruhig). Nötig
     * z. B. während "Hold my beer", damit Pepper das gehaltene Objekt nicht durch
     * Body-Talk-Gesten abwirft.
     */
    public void sayStill(QiContext context, String toSay) {
        SupportedLanguage current = languageManager.getCurrent();
        speak(context, toSay, new Locale(current.getQiLang(), current.getRegion()), current,
                BodyLanguageOption.DISABLED);
    }

    private void speak(QiContext context, String toSay, Locale locale, SupportedLanguage fallback) {
        speak(context, toSay, locale, fallback, BodyLanguageOption.BODY_TALK);
    }

    private void speak(QiContext context, String toSay, Locale locale, SupportedLanguage fallback,
                       BodyLanguageOption bodyLanguage) {
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
