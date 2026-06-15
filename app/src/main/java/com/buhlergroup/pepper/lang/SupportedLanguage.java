package com.buhlergroup.pepper.lang;


import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Region;

public enum SupportedLanguage {
    GERMAN(
            "de-CH",
            "Deutsch",
            Language.GERMAN,
            new String[]{"deutsch", "german", "germany"},
            Region.GERMANY,
            "Alles klar, ich spreche jetzt Deutsch. Wie kann ich dir helfen?"
    ),
    ENGLISH(
            "en-US",
            "English",
            Language.ENGLISH,
            new String[]{"englisch", "english"},
            Region.UNITED_STATES,
            "Okay, I will speak English now. How can I help you?"
    );

    private final String abbreviation;
    private final String displayName;
    private final Language qiLang;
    private final String[] triggerNames;
    private final Region region;
    private final String switchConfirmation;

    SupportedLanguage(String abbreviation, String displayName, Language qiLang, String[] triggerNames, Region region, String switchConfirmation) {
        this.abbreviation = abbreviation;
        this.displayName = displayName;
        this.qiLang = qiLang;
        this.triggerNames = triggerNames;
        this.region = region;
        this.switchConfirmation = switchConfirmation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Language getQiLang() {
        return qiLang;
    }

    public String[] getTriggerNames() {
        return triggerNames;
    }

    public Region getRegion() {
        return region;
    }

    public String getSwitchConfirmation() {
        return switchConfirmation;
    }
}
