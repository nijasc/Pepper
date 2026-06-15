package com.buhlergroup.pepper.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class KeywordFallbackTest {

    @Test
    public void matchesCoreCommandsGermanAndEnglish() {
        assertEquals("DanceAction", KeywordFallback.match("kannst du tanzen"));
        assertEquals("DanceAction", KeywordFallback.match("let us dance"));
        assertEquals("SelfieAction", KeywordFallback.match("mach ein selfie"));
        assertEquals("MemoryGameAction", KeywordFallback.match("lass uns memory spielen"));
        assertEquals("HighFiveAction", KeywordFallback.match("gib mir high five"));
        assertEquals("ChangeVolumeAction", KeywordFallback.match("mach lauter"));
        assertEquals("ChangeVolumeAction", KeywordFallback.match("set the volume to fifty"));
    }

    @Test
    public void returnsNullForUnknownOrEmpty() {
        assertNull(KeywordFallback.match("erzähl mir einen witz"));
        assertNull(KeywordFallback.match(""));
        assertNull(KeywordFallback.match(null));
    }
}
