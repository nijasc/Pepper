package com.buhlergroup.pepper.openai.history;

import java.util.Locale;

public enum HistoryRole {
    USER,
    ASSISTANT,
    DEVELOPER;

    public String apiValue() {
        return this.toString().toLowerCase(Locale.ROOT);
    }
}
