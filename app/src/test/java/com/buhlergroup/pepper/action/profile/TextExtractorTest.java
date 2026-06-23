package com.buhlergroup.pepper.action.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TextExtractorTest {

    @Test
    public void stripsTagsAndKeepsText() {
        String result = TextExtractor.stripHtml("<p>Hallo <b>Welt</b></p>");
        assertEquals("Hallo Welt", result);
    }

    @Test
    public void removesScriptAndStyleContent() {
        String html = "<style>.a{color:red}</style><script>alert('x')</script><p>Inhalt</p>";
        String result = TextExtractor.stripHtml(html);
        assertFalse(result.contains("color"));
        assertFalse(result.contains("alert"));
        assertTrue(result.contains("Inhalt"));
    }

    @Test
    public void decodesCommonEntities() {
        String result = TextExtractor.stripHtml("Bühler &amp; Co &lt;X&gt;");
        assertEquals("Bühler & Co <X>", result);
    }

    @Test
    public void handlesNullInput() {
        assertEquals("", TextExtractor.stripHtml(null));
    }
}
