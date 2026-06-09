package com.buhler.funktionierender_pepper.openai.history;

public enum HistoryRole {
    USER,
    ASSISTANT,
    DEVELOPER;

    public String apiValue() {
        return this.toString().toLowerCase();
    }
}
