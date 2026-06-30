package com.buhlergroup.pepper.action.dialogue;

import android.os.Handler;
import android.os.Looper;

public final class DialogueController {

    private static final long MS_PER_WORD = 330;
    private static final long AUTO_HIDE_MS = 8000;

    private static final DialogueController INSTANCE = new DialogueController();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile DialogueView view;
    private final Runnable hideRunnable = this::hideNow;
    private volatile boolean suppressed;
    private String[] words = new String[0];

    private DialogueController() {
    }

    public static DialogueController get() {
        return INSTANCE;
    }

    public void attachView(DialogueView view) {
        this.view = view;
    }

    public void detachView() {
        cancelPending();
        this.view = null;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
        if (suppressed) {
            handler.post(this::hideNow);
        }
    }

    public void beginUtterance(String text) {
        DialogueView board = view;
        if (board == null || suppressed || text == null || text.trim().isEmpty()) {
            return;
        }
        final String[] tokens = text.trim().split("\\s+");
        handler.post(() -> {
            cancelPending();
            words = tokens;
            board.setText("");
            for (int i = 1; i <= tokens.length; i++) {
                final int count = i;
                handler.postDelayed(() -> revealUpTo(count), (i - 1) * MS_PER_WORD);
            }
        });
    }

    public void endUtterance() {
        DialogueView board = view;
        if (board == null) {
            return;
        }
        handler.post(() -> {
            cancelRevealOnly();
            if (!suppressed && words.length > 0) {
                board.setText(join(words.length));
            }
            handler.removeCallbacks(hideRunnable);
            handler.postDelayed(hideRunnable, AUTO_HIDE_MS);
        });
    }

    private void revealUpTo(int count) {
        DialogueView board = view;
        if (board == null || suppressed) {
            return;
        }
        board.setText(join(count));
    }

    private String join(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count && i < words.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(words[i]);
        }
        return sb.toString();
    }

    private void hideNow() {
        DialogueView board = view;
        if (board != null) {
            board.hide();
        }
    }

    private void cancelRevealOnly() {
        handler.removeCallbacksAndMessages(null);
    }

    private void cancelPending() {
        handler.removeCallbacksAndMessages(null);
    }
}
