package com.buhlergroup.pepper.openai.history;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.action.Action;

public class HistoryEntry {
    private final HistoryRole role;
    private final String content;
    @Nullable
    private final Action action;

    public HistoryEntry(HistoryRole role, String content, @Nullable Action action) {
        this.role = role;
        this.content = content;
        this.action = action;
    }

    public HistoryRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    @Nullable
    public Action getAction() {
        return action;
    }
}
