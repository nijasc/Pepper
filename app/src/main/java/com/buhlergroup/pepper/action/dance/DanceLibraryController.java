package com.buhlergroup.pepper.action.dance;

public final class DanceLibraryController {

    public interface StateListener {
        void onDanceLibraryStateChanged(boolean open);
    }

    private static final DanceLibraryController INSTANCE = new DanceLibraryController();

    private volatile DanceLibraryView view;
    private volatile boolean open;
    private volatile StateListener stateListener;

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
