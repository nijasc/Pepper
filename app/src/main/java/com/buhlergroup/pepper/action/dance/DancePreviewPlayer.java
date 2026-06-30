package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.media.AudioCoordinator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the raw MediaPlayer lifecycle for previewing a dance's audio from a given
 * start point, plus the dedicated heavy executor used only for preview playback.
 * Extracted from {@link DanceLibraryView} so the view no longer manages MediaPlayer
 * or executor resources for previews.
 */
class DancePreviewPlayer {

    /**
     * Callback used to surface user-facing messages (toasts) back to the view,
     * which is responsible for posting them to the UI thread.
     */
    interface PreviewListener {
        void onMessage(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DanceRepository repository;
    private final PreviewListener listener;

    DancePreviewPlayer(DanceRepository repository, PreviewListener listener) {
        this.repository = repository;
        this.listener = listener;
    }

    /**
     * Prepares and plays a short preview of the dance's audio starting at {@code ms},
     * then tears the player down. Runs on its own executor. Behavior matches the
     * original DanceLibraryView.previewFrom: same 8s preview window, same stream
     * flags, same teardown in finally, same interrupt/exception handling.
     */
    void start(Context context, DanceEntity dance, long ms) {
        executor.execute(() -> {
            MediaPlayer player = null;
            try {
                repository.preparePlayback(context, dance);
                if (dance.previewUrl == null) {
                    listener.onMessage("Keine Vorschau verfügbar.");
                    return;
                }
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(dance.previewUrl);
                player.prepare();
                if (ms > 0 && ms < player.getDuration()) {
                    player.seekTo((int) ms);
                }
                AudioCoordinator.get().attachMusic(player);
                player.start();
                Thread.sleep(8000);
            } catch (Exception e) {
                listener.onMessage("Vorhören fehlgeschlagen: " + e.getMessage());
            } finally {
                if (player != null) {
                    AudioCoordinator.get().detachMusic(player);
                    try {
                        if (player.isPlaying()) {
                            player.stop();
                        }
                    } catch (Exception ignored) {
                    }
                    player.release();
                }
            }
        });
    }

    /**
     * Shuts down the preview executor. Must be called from the view's
     * onDetachedFromWindow.
     */
    void release() {
        executor.shutdownNow();
    }
}
