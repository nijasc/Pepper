package com.buhlergroup.pepper.action.volume;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NumberWordsTest {

    @Test
    public void parsesGermanTens() {
        assertEquals(Integer.valueOf(80), NumberWords.parse("stell die lautstärke auf achtzig prozent"));
        assertEquals(Integer.valueOf(50), NumberWords.parse("fünfzig"));
    }

    @Test
    public void parsesGermanCompound() {
        assertEquals(Integer.valueOf(25), NumberWords.parse("fünfundzwanzig prozent bitte"));
    }

    @Test
    public void parsesEnglish() {
        assertEquals(Integer.valueOf(80), NumberWords.parse("set volume to eighty"));
        assertEquals(Integer.valueOf(25), NumberWords.parse("twenty five percent"));
    }

    @Test
    public void parsesKeywords() {
        assertEquals(Integer.valueOf(100), NumberWords.parse("volle lautstärke"));
        assertEquals(Integer.valueOf(50), NumberWords.parse("nur halb so laut"));
        assertEquals(Integer.valueOf(0), NumberWords.parse("mach es leise"));
    }

    @Test
    public void returnsNullWhenNoNumber() {
        assertNull(NumberWords.parse("mach irgendwas"));
        assertNull(NumberWords.parse(""));
        assertNull(NumberWords.parse(null));
    }
}
