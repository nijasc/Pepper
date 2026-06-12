package com.buhlergroup.pepper.action.dance;

public final class DancePlayerController {

    private static final DancePlayerController INSTANCE = new DancePlayerController();

    private volatile DancePlayerView view;

    private DancePlayerController() {
    }

    public static DancePlayerController get() {
        return INSTANCE;
    }

    public void attachView(DancePlayerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void play(String videoId) {
        DancePlayerView current = view;
        if (current != null) {
            current.play(videoId);
        }
    }

    public void stop() {
        DancePlayerView current = view;
        if (current != null) {
            current.stop();
        }
    }
}
