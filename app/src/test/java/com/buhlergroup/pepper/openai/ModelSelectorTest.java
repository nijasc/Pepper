package com.buhlergroup.pepper.openai;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ModelSelectorTest {

    @Test
    public void classificationAndRewriteUseFastModel() {
        assertEquals(ModelSelector.FAST,
                ModelSelector.modelFor(ModelSelector.ModelTask.CLASSIFICATION));
        assertEquals(ModelSelector.FAST,
                ModelSelector.modelFor(ModelSelector.ModelTask.REWRITE));
    }

    @Test
    public void generationUsesStrongGenerationModel() {
        assertEquals(ModelSelector.STRONG_GENERATION,
                ModelSelector.modelFor(ModelSelector.ModelTask.GENERATION));
    }

    @Test
    public void conversationAndDocumentationUseStrongModel() {
        assertEquals(ModelSelector.STRONG,
                ModelSelector.modelFor(ModelSelector.ModelTask.CONVERSATION));
        assertEquals(ModelSelector.STRONG,
                ModelSelector.modelFor(ModelSelector.ModelTask.DOCUMENTATION));
    }
}
