package com.buhlergroup.pepper.action.navigation;

public final class NavigationController {

    private static final NavigationController INSTANCE = new NavigationController();
    private volatile NavigationView view;
    private volatile boolean open;
    private volatile StateListener stateListener;
    private NavigationController() {
    }

    public static NavigationController get() {
        return INSTANCE;
    }

    public void attachView(NavigationView view) {
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
        NavigationView current = view;
        if (current != null) {
            open = true;
            notifyState(true);
            current.open();
        }
    }

    public void close() {
        NavigationView current = view;
        open = false;
        notifyState(false);
        if (current != null) {
            current.hide();
        }
    }

    private void notifyState(boolean value) {
        StateListener l = stateListener;
        if (l != null) {
            l.onNavigationStateChanged(value);
        }
    }

    public interface StateListener {
        void onNavigationStateChanged(boolean open);
    }
}
