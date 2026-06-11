package com.buhlergroup.pepper.action;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.openai.history.HistoryManager;

public abstract class Action {

    private HistoryManager historyManager;

    public abstract void execute(QiContext context, String input);

    public abstract String getDescription();


    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }
}
