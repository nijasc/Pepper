package com.buhlergroup.pepper.lang;

import static org.junit.Assert.assertNotNull;

import com.aldebaran.qi.sdk.object.locale.Locale;

import org.junit.Test;

public class LocaleResolverTest {

    @Test
    public void mappedTagResolvesToLocale() {
        Locale locale = LocaleResolver.resolve("de", SupportedLanguage.GERMAN);
        assertNotNull(locale);
    }

    @Test
    public void unknownTagFallsBack() {
        Locale locale = LocaleResolver.resolve("xx", SupportedLanguage.GERMAN);
        assertNotNull(locale);
    }

    @Test
    public void nullTagFallsBack() {
        Locale locale = LocaleResolver.resolve(null, SupportedLanguage.ENGLISH);
        assertNotNull(locale);
    }
}
