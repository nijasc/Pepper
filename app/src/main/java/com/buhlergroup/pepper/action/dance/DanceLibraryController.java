package com.buhlergroup.pepper.action.dance;

import com.buhlergroup.pepper.action.dance.data.DanceEntity;

public final class DanceLibraryController {

    public interface StateListener {
        void onDanceLibraryStateChanged(boolean open);
    }

    private static final DanceLibraryController INSTANCE = new DanceLibraryController();

    private volatile DanceLibraryView view;
    private volatile boolean open;
    private volatile StateListener stateListener;
    private volatile Runnable voiceRequester;
    private volatile DanceEntity pendingEdit;

    private DanceLibraryController() {
    }

    public static DanceLibraryController get() {
        return INSTANCE;
    }

    public void attachView(DanceLibraryView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        this.open = false;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public void setVoiceRequester(Runnable requester) {
        this.voiceRequester = requester;
    }

    public void requestVoiceEdit(DanceEntity dance) {
        pendingEdit = dance;
        Runnable requester = voiceRequester;
        if (requester != null) {
            requester.run();
        }
    }

    public void onVoiceEditResult(String text) {
        DanceEntity dance = pendingEdit;
        pendingEdit = null;
        DanceLibraryView current = view;
        if (dance != null && current != null && text != null && !text.trim().isEmpty()) {
            current.applyAiEdit(dance, text.trim());
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        DanceLibraryView current = view;
        if (current != null) {
            open = true;
            notifyState(true);
            current.open();
        }
    }

    public void close() {
        DanceLibraryView current = view;
        open = false;
        notifyState(false);
        if (current != null) {
            current.hide();
        }
    }

    private void notifyState(boolean value) {
        StateListener l = stateListener;
        if (l != null) {
            l.onDanceLibraryStateChanged(value);
        }
    }
}
