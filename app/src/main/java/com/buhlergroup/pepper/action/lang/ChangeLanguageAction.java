package com.buhlergroup.pepper.action.lang;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

public class ChangeLanguageAction extends Action {
    private final LanguageManager languageManager;

    public ChangeLanguageAction(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    @Override
    public void execute(QiContext context, String input) {
        SupportedLanguage lang = parseLang(input);

        if (lang == null) {
            return;
        }

        languageManager.applyLanguage(lang);
        SpeechManager.getInstance().systemSay(context, lang.getSwitchConfirmation());
    }

    private SupportedLanguage parseLang(String input) {
        String[] langSplit = input.split(" ");

        for (SupportedLanguage lang : SupportedLanguage.values()) {
            for (String trigger : lang.getTriggerNames()) {
                for (String langStr : langSplit) {
                    if (trigger.equalsIgnoreCase(langStr)) {
                        return lang;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Switches Pepper's spoken language to the requested language.";
    }
}
