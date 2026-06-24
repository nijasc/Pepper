package com.buhlergroup.pepper.llm;

public final class LlmModel {

    public final String id;
    public final String label;
    public final String hint;

    public LlmModel(String id, String label, String hint) {
        this.id = id;
        this.label = label;
        this.hint = hint;
    }

    @Override
    public String toString() {
        return label;
    }
}
