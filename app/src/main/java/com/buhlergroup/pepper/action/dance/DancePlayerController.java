package com.buhlergroup.pepper.action.dance;

import java.util.List;

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

    public void play(List<String> videoIds, int startSeconds) {
        DancePlayerView current = view;
        if (current != null) {
            current.play(videoIds, startSeconds);
        }
    }

    public void stop() {
        DancePlayerView current = view;
        if (current != null) {
            current.stop();
        }
    }
}
