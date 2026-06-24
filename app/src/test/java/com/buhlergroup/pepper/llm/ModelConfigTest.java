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

    @Test
    public void flagshipIsAlwaysAvailableModel() {
        for (LlmProvider provider : LlmProvider.values()) {
            assertTrue(provider.models.length > 0);
            boolean found = false;
            for (String id : provider.modelIds()) {
                if (id.equals(provider.flagshipModel)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Flagship " + provider.flagshipModel + " missing from "
                    + provider.name() + " model list", found);
        }
    }

    @Test
    public void defaultModelsExistInOpenAiCatalog() {
        assertModelPresent(ModelSettings.DEFAULT_FAST);
        assertModelPresent(ModelSettings.DEFAULT_STRONG);
        assertModelPresent(ModelSettings.DEFAULT_GENERATION);
    }

    private void assertModelPresent(String id) {
        for (String available : LlmProvider.OPENAI.modelIds()) {
            if (available.equals(id)) {
                return;
            }
        }
        throw new AssertionError("Default model " + id + " not in OpenAI catalog");
    }
}
