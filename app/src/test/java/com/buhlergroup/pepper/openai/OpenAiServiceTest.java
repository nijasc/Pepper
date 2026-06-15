package com.buhlergroup.pepper.openai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;

public class OpenAiServiceTest {

    @Test
    public void extractsLanguageTagAndStripsIt() {
        OpenAIService service = new OpenAIService(new ArrayList<>());
        String cleaned = service.extractLanguageTag("[[lang:de]] Hallo Welt.");
        assertEquals("Hallo Welt.", cleaned);
        assertEquals("de", service.lastLanguageTag());
    }

    @Test
    public void stripsActionMarkerToo() {
        OpenAIService service = new OpenAIService(new ArrayList<>());
        String cleaned = service.extractLanguageTag("[[lang:en]][[action:DanceAction]]Let us dance.");
        assertEquals("Let us dance.", cleaned);
        assertEquals("en", service.lastLanguageTag());
    }

    @Test
    public void noTagLeavesTextAndClearsLanguage() {
        OpenAIService service = new OpenAIService(new ArrayList<>());
        String cleaned = service.extractLanguageTag("Plain answer.");
        assertEquals("Plain answer.", cleaned);
        assertNull(service.lastLanguageTag());
    }
}
