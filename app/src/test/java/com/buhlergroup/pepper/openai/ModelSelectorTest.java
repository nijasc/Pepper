package com.buhlergroup.pepper.openai;

import static org.junit.Assert.assertEquals;

import com.buhlergroup.pepper.llm.ModelTask;

import org.junit.Test;

public class ModelSelectorTest {

    @Test
    public void classificationAndRewriteUseFastModel() {
        assertEquals(ModelSelector.FAST,
                ModelSelector.modelFor(ModelTask.CLASSIFICATION));
        assertEquals(ModelSelector.FAST,
                ModelSelector.modelFor(ModelTask.REWRITE));
    }

    @Test
    public void generationUsesStrongGenerationModel() {
        assertEquals(ModelSelector.STRONG_GENERATION,
                ModelSelector.modelFor(ModelTask.GENERATION));
    }

    @Test
    public void conversationAndDocumentationUseStrongModel() {
        assertEquals(ModelSelector.STRONG,
                ModelSelector.modelFor(ModelTask.CONVERSATION));
        assertEquals(ModelSelector.STRONG,
                ModelSelector.modelFor(ModelTask.DOCUMENTATION));
    }
}
