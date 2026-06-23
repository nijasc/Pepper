package com.buhlergroup.pepper.debug;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DebugLog {

    private static final DebugLog INSTANCE = new DebugLog();
    private static final int MAX_ENTRIES = 1000;
    private final Deque<String> entries = new ArrayDeque<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final SimpleDateFormat lineFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    private final SimpleDateFormat stampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private volatile boolean enabled = DebugSettings.DEFAULT_ENABLED;
    private volatile String status = "";
    private DebugLog() {
    }

    public static DebugLog get() {
        return INSTANCE;
    }

    public void init(Context context) {
        enabled = DebugSettings.isEnabled(context);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Context context, boolean value) {
        DebugSettings.setEnabled(context, value);
        if (enabled == value) {
            return;
        }
        enabled = value;
        for (Listener listener : listeners) {
            listener.onEnabledChanged(value);
        }
        append('I', "Debug", value ? "Debug-Modus aktiviert" : "Debug-Modus deaktiviert");
    }

    public void addListener(Listener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        status = newStatus == null ? "" : newStatus;
        for (Listener listener : listeners) {
            listener.onStatus(status);
        }
        append('S', "Status", status);
    }

    public void d(String tag, String message) {
        log('D', tag, message);
    }

    public void i(String tag, String message) {
        log('I', tag, message);
    }

    public void w(String tag, String message) {
        log('W', tag, message);
    }

    public void e(String tag, String message) {
        log('E', tag, message);
    }

    public void e(String tag, String message, Throwable throwable) {
        log('E', tag, throwable == null ? message : message + " — " + throwable);
    }

    private void log(char level, String tag, String message) {
        switch (level) {
            case 'D':
                Log.d(tag, message);
                break;
            case 'W':
                Log.w(tag, message);
                break;
            case 'E':
                Log.e(tag, message);
                break;
            default:
                Log.i(tag, message);
                break;
        }
        append(level, tag, message);
    }

    private void append(char level, String tag, String message) {
        String line = lineFormat.format(new Date()) + " " + level + "/" + tag + ": " + message;
        synchronized (entries) {
            entries.addLast(line);
            while (entries.size() > MAX_ENTRIES) {
                entries.removeFirst();
            }
        }
        for (Listener listener : listeners) {
            listener.onEntry(line);
        }
    }

    public List<String> snapshot() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
        append('I', "Debug", "Log geleert");
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        List<String> snapshot;
        synchronized (entries) {
            snapshot = new ArrayList<>(entries);
        }
        sb.append("Pepper Debug-Log\n");
        sb.append("Erstellt: ").append(stampFormat.format(new Date())).append('\n');
        sb.append("Gerät: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" (Android ").append(Build.VERSION.RELEASE).append(")\n");
        sb.append("Status: ").append(status.isEmpty() ? "—" : status).append('\n');
        sb.append("Einträge: ").append(snapshot.size()).append("\n\n");
        for (String entry : snapshot) {
            sb.append(entry).append('\n');
        }
        return sb.toString();
    }

    public interface Listener {
        void onEntry(String formattedLine);

        void onStatus(String status);

        void onEnabledChanged(boolean enabled);
    }
}
