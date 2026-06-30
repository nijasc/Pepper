package com.buhlergroup.pepper.openai.history;

import com.buhlergroup.pepper.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {
    public static final int MAX_HISTORY = 10;
    private static final int MAX_DEV_LOG = 200;
    private final List<HistoryEntry> history = new ArrayList<>();
    private final List<String> devLog = new ArrayList<>();
    private final Object lock = new Object();

    public void addUser(String content) {
        add(HistoryRole.USER, content, null);
    }

    public void addAssistant(String content, Action action) {
        add(HistoryRole.ASSISTANT, content, action);
    }

    public void addDeveloper(String content) {
        add(HistoryRole.DEVELOPER, content, null);
    }

    public void addDeveloper(String content, Action action) {
        add(HistoryRole.DEVELOPER, content, action);
    }

    public void clear() {
        synchronized (lock) {
            history.clear();
            addLocked(HistoryRole.DEVELOPER, "History cleared.", null);
        }
    }

    private void add(HistoryRole role, String content, Action action) {
        synchronized (lock) {
            addLocked(role, content, action);
        }
    }

    private void addLocked(HistoryRole role, String content, Action action) {
        if (role != HistoryRole.DEVELOPER && conversationalSize() >= MAX_HISTORY) {
            removeOldestConversational();
        }
        history.add(new HistoryEntry(role, content, action));
        if (role == HistoryRole.DEVELOPER) {
            devLog.add(content);
            if (devLog.size() > MAX_DEV_LOG) {
                devLog.remove(0);
            }
        }
    }

    public List<String> getDevLog() {
        synchronized (lock) {
            return new ArrayList<>(devLog);
        }
    }

    public List<HistoryEntry> getConversation() {
        synchronized (lock) {
            List<HistoryEntry> result = new ArrayList<>();
            for (HistoryEntry entry : history) {
                if (entry.getRole() != HistoryRole.DEVELOPER) {
                    result.add(entry);
                }
            }
            return result;
        }
    }

    public List<Map<String, String>> toInput() {
        synchronized (lock) {
            List<Map<String, String>> input = new ArrayList<>();
            for (HistoryEntry entry : history) {
                Map<String, String> msg = new HashMap<>();
                msg.put("role", entry.getRole().apiValue());
                msg.put("content", entry.getContent());
                input.add(msg);
            }
            return input;
        }
    }

    public int historySize() {
        synchronized (lock) {
            return conversationalSize();
        }
    }

    private int conversationalSize() {
        int count = 0;
        for (HistoryEntry entry : history) {
            if (entry.getRole() != HistoryRole.DEVELOPER) {
                count++;
            }
        }
        return count;
    }

    private void removeOldestConversational() {
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getRole() != HistoryRole.DEVELOPER) {
                history.remove(i);
                break;
            }
        }
        while (!history.isEmpty() && history.get(0).getRole() == HistoryRole.DEVELOPER) {
            history.remove(0);
        }
    }
}
