package com.buhlergroup.pepper.media;

import android.media.MediaPlayer;

public final class AudioCoordinator {

    private static final float DUCK_VOLUME = 0.15f;
    private static final AudioCoordinator INSTANCE = new AudioCoordinator();

    private MediaPlayer currentMusic;
    private int speaking;

    private AudioCoordinator() {
    }

    public static AudioCoordinator get() {
        return INSTANCE;
    }

    public synchronized void attachMusic(MediaPlayer player) {
        if (player == null || player == currentMusic) {
            return;
        }
        stopAndRelease(currentMusic);
        currentMusic = player;
        applyDuck();
    }

    public synchronized void detachMusic(MediaPlayer player) {
        if (currentMusic == player) {
            currentMusic = null;
        }
    }

    public synchronized void onSpeechStart() {
        speaking++;
        applyDuck();
    }

    public synchronized void onSpeechEnd() {
        if (speaking > 0) {
            speaking--;
        }
        applyDuck();
    }

    private void applyDuck() {
        MediaPlayer player = currentMusic;
        if (player == null) {
            return;
        }
        try {
            float volume = speaking > 0 ? DUCK_VOLUME : 1f;
            player.setVolume(volume, volume);
        } catch (Exception ignored) {
        }
    }

    private void stopAndRelease(MediaPlayer player) {
        if (player == null) {
            return;
        }
        try {
            if (player.isPlaying()) {
                player.stop();
            }
        } catch (Exception ignored) {
        }
        try {
            player.release();
        } catch (Exception ignored) {
        }
    }
}
