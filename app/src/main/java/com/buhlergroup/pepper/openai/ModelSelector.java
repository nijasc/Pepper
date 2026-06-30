package com.buhlergroup.pepper.openai;

import com.buhlergroup.pepper.llm.ModelTask;

public final class ModelSelector {

    public static final String FAST = "gpt-4o-mini";
    public static final String STRONG = "gpt-5.4";
    public static final String STRONG_GENERATION = "gpt-5.5";

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
