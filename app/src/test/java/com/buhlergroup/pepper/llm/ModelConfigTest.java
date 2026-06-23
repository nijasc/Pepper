package com.buhlergroup.pepper.llm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;

import org.junit.Test;

public class ModelConfigTest {

    @Test
    public void defaultModelsPerTask() {
        assertEquals(ModelSettings.DEFAULT_FAST, ModelSettings.defaultModel(ModelTask.CLASSIFICATION));
        assertEquals(ModelSettings.DEFAULT_FAST, ModelSettings.defaultModel(ModelTask.REWRITE));
        assertEquals(ModelSettings.DEFAULT_GENERATION, ModelSettings.defaultModel(ModelTask.GENERATION));
        assertEquals(ModelSettings.DEFAULT_STRONG, ModelSettings.defaultModel(ModelTask.CONVERSATION));
        assertEquals(ModelSettings.DEFAULT_STRONG, ModelSettings.defaultModel(ModelTask.DOCUMENTATION));
    }

    @Test
    public void providerConfiguration() {
        assertTrue(LlmProvider.OPENAI.baseUrl.contains("api.openai.com"));
        assertTrue(LlmProvider.GROK.baseUrl.contains("api.x.ai"));
        assertTrue(LlmProvider.GEMINI.baseUrl.contains("generativelanguage.googleapis.com"));
        assertTrue(LlmProvider.OPENAI.supportsReasoningEffort);
        assertFalse(LlmProvider.GEMINI.supportsReasoningEffort);
    }

    @Test
    public void fromNameFallsBack() {
        assertEquals(LlmProvider.GROK, LlmProvider.fromName("GROK", LlmProvider.OPENAI));
        assertEquals(LlmProvider.OPENAI, LlmProvider.fromName("nonsense", LlmProvider.OPENAI));
        assertEquals(LlmProvider.OPENAI, LlmProvider.fromName(null, LlmProvider.OPENAI));
    }
}
