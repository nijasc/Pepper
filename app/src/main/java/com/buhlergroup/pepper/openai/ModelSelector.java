package com.buhlergroup.pepper.openai;

import com.buhlergroup.pepper.llm.ModelSettings;
import com.buhlergroup.pepper.llm.ModelTask;

public final class ModelSelector {

    public static final String FAST = ModelSettings.DEFAULT_FAST;
    public static final String STRONG = ModelSettings.DEFAULT_STRONG;
    public static final String STRONG_GENERATION = ModelSettings.DEFAULT_GENERATION;

    private ModelSelector() {
    }

    public static String modelFor(ModelTask task) {
        switch (task) {
            case CLASSIFICATION:
            case REWRITE:
                return FAST;
            case GENERATION:
                return STRONG_GENERATION;
            case CONVERSATION:
            case DOCUMENTATION:
            default:
                return STRONG;
        }
    }
}
