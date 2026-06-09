package com.buhler.funktionierender_pepper.openai.history;

import com.buhler.funktionierender_pepper.action.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HistoryManager implements Cloneable {
    public static final int MAX_HISTORY = 10;
    private final Map<Long, HistoryEntry> history = new HashMap<>();

    public void addUser(String content) {
        add(HistoryRole.USER, content, null);
    }

    public void addAssistant(String content, Action action) {
        add(HistoryRole.ASSISTANT, content, action);
    }

    public void addDeveloper(String content, Action action) {
        add(HistoryRole.DEVELOPER, content, action);
    }

    private void add(HistoryRole role, String content, Action action) {
        if (history.size() == MAX_HISTORY) {
            removeLast();
        }
        history.put(System.currentTimeMillis(), new HistoryEntry(role, content, action));
    }

    public List<Map<String, String>> toInput() {
        List<Map<String, String>> input = new ArrayList<>();
        for (HistoryEntry entry : orderedEntries()) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", entry.getRole().apiValue());
            msg.put("content", entry.getContent());
            input.add(msg);
        }
        return input;
    }

    private List<HistoryEntry> orderedEntries() {
        return new ArrayList<>(new TreeMap<>(history).values());
    }

    private void removeLast() {
        if (history.isEmpty()) {
            return;
        }
        history.remove(Collections.min(history.keySet()));
    }

    public int historySize() {
        return history.size();
    }
}