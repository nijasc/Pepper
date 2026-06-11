package com.buhlergroup.pepper.perception;

public enum BasicEmotion {
    UNKNOWN(null),
    NEUTRAL(null),
    CONTENT("appears calm and content"),
    JOYFUL("appears happy and is smiling"),
    SAD("appears a little down or sad"),
    ANGRY("appears tense or annoyed");

    private final String promptHint;

    BasicEmotion(String promptHint) {
        this.promptHint = promptHint;
    }

    public boolean isMentionable() {
        return promptHint != null;
    }

    public String getPromptHint() {
        return promptHint;
    }
}
