package com.buhlergroup.pepper.lang;


import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Region;

public enum SupportedLanguage {
    GERMAN(
            "de-CH",
            "Deutsch",
            Language.GERMAN,
            new String[]{"deutsch", "german", "germany"},
            Region.GERMANY
    ),
    ENGLISH(
            "en-US",
            "English",
            Language.ENGLISH,
            new String[]{"englisch", "english"},
            Region.UNITED_STATES
    );

    private final String abbreviation;
    private final String displayName;
    private final Language qiLang;
    private final String[] triggerNames;
    private final Region region;

    SupportedLanguage(String abbreviation, String displayName, Language qiLang, String[] triggerNames, Region region) {
        this.abbreviation = abbreviation;
        this.displayName = displayName;
        this.qiLang = qiLang;
        this.triggerNames = triggerNames;
        this.region = region;
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
}
