package com.buhlergroup.pepper.action.dance;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.action.audio.AudioCoordinator;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.debug.DebugLog;

import java.io.File;

public final class DancePlayback {

    private static final String TAG = "DancePlayback";
    private static final long MAX_PLAY_MS = 35000;
    private static final long MIN_PLAY_MS = 5000;

    private DancePlayback() {
    }

    public static void play(QiContext context, DanceEntity dance) throws Exception {
        DebugLog.get().setStatus("Tanz: " + dance.songName);
        DebugLog.get().i(TAG, "Tanz gestartet: " + dance.songName);
        String qianim = DanceRepository.readQianim(new File(dance.qianimPath));
        Animation animation = AnimationBuilder.with(context).withTexts(qianim).build();
        Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();

        MediaPlayer player = startAudio(dance.previewUrl, dance.audioStartMs);
        Future<Void> animationFuture = null;
        try {
            long clipMs = player != null ? player.getDuration() - dance.audioStartMs : dance.durationMs;
            if (clipMs <= 0) {
                clipMs = dance.durationMs;
            }
            long capMs = Math.max(MIN_PLAY_MS, Math.min(MAX_PLAY_MS, clipMs));
            animationFuture = animate.async().run();
            awaitAnimation(animationFuture, capMs);
        } finally {
            stopAudio(player);
            if (animationFuture != null && !animationFuture.isDone()) {
                animationFuture.requestCancellation();
            }
            DebugLog.get().i(TAG, "Tanz beendet: " + dance.songName);
        }
    }

    private static MediaPlayer startAudio(String url, long startMs) {
        if (url == null || url.isEmpty()) {
            DebugLog.get().w(TAG, "Keine Audio-Quelle (previewUrl leer) – kein Ton");
            return null;
        }
        try {
            MediaPlayer player = new MediaPlayer();
            player.setOnErrorListener((mp, what, extra) -> {
                DebugLog.get().w(TAG, "MediaPlayer-Fehler what=" + what + " extra=" + extra);
                return false;
            });
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            if (startMs > 0 && startMs < player.getDuration()) {
                player.seekTo((int) startMs);
            }
            player.start();
            AudioCoordinator.get().attachMusic(player);
            DebugLog.get().i(TAG, "Musik gestartet");
            return player;
        } catch (Exception e) {
            Log.w(TAG, "Preview playback failed: " + e.getMessage());
            DebugLog.get().w(TAG, "Audio-Wiedergabe fehlgeschlagen: " + e.getMessage());
            return null;
        }
    }

    private static void stopAudio(MediaPlayer player) {
        if (player == null) {
            return;
        }
        AudioCoordinator.get().detachMusic(player);
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

    private static void awaitAnimation(Future<Void> animationFuture, long capMs) {
        long deadline = System.currentTimeMillis() + capMs;
        while (System.currentTimeMillis() < deadline) {
            if (animationFuture == null || animationFuture.isDone()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
