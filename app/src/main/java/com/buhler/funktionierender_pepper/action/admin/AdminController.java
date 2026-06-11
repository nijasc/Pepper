package com.buhler.funktionierender_pepper.action.admin;

import com.buhler.funktionierender_pepper.openai.history.HistoryManager;

import java.util.Collections;
import java.util.List;

public final class AdminController {

    private static final AdminController INSTANCE = new AdminController();

    private volatile AdminView view;
    private volatile HistoryManager historyManager;
    private volatile boolean open = false;

    private AdminController() {
    }

    public static AdminController get() {
        return INSTANCE;
    }

    public void attachView(AdminView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        this.open = false;
    }

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public void open() {
        AdminView current = view;
        if (current != null) {
            open = true;
            current.open();
        }
    }

    public boolean isOpen() {
        return open;
    }

    void markClosed() {
        open = false;
    }

    public boolean clearHistory() {
        HistoryManager hm = historyManager;
        if (hm == null) {
            return false;
        }
        hm.clear();
        return true;
    }

    public List<String> getDevLog() {
        HistoryManager hm = historyManager;
        if (hm == null) {
            return Collections.emptyList();
        }
        return hm.getDevLog();
    }
}
