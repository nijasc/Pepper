package com.buhlergroup.pepper.llm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.buhlergroup.pepper.openai.OpenAIService;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ChatStreamParserTest {

    private static String sse(String... contents) {
        StringBuilder sb = new StringBuilder();
        for (String c : contents) {
            sb.append("data: {\"choices\":[{\"delta\":{\"content\":\"")
                    .append(c).append("\"}}]}\n");
        }
        sb.append("data: [DONE]\n");
        return sb.toString();
    }

    @Test
    public void extractsMarkersAndSentences() throws Exception {
        List<String> sentences = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        OpenAIService.StreamListener listener = new OpenAIService.StreamListener() {
            @Override
            public boolean onAction(String actionName) {
                actions.add(actionName);
                return true;
            }

            @Override
            public void onSentence(String sentence, String languageTag) {
                sentences.add(sentence);
            }
        };

        String stream = sse("[[lang:de]]", "[[action:SayAction]]", "Hallo Welt. ", "Wie geht es dir?");
        ChatStreamParser parser = new ChatStreamParser();
        parser.parse(new BufferedReader(new StringReader(stream)), listener, 0L);

        assertEquals("de", parser.lastLanguageTag());
        assertTrue(actions.contains("SayAction"));
        assertTrue(sentences.contains("Hallo Welt."));
        assertTrue(sentences.contains("Wie geht es dir?"));
    }

    @Test
    public void nonSayActionAbortsStream() throws Exception {
        OpenAIService.StreamListener listener = new OpenAIService.StreamListener() {
            @Override
            public boolean onAction(String actionName) {
                return false;
            }

            @Override
            public void onSentence(String sentence, String languageTag) {
            }
        };
        String stream = sse("[[lang:en]]", "[[action:DanceAction]]");
        ChatStreamParser parser = new ChatStreamParser();
        assertEquals(null, parser.parse(new BufferedReader(new StringReader(stream)), listener, 0L));
    }
}
