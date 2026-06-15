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
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.action.thinking.ThinkingController;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.File;

public class DanceAction extends Action {

    private static final String TAG = "Dance";
    private static final long MAX_PLAY_MS = 35000;
    private static final long MIN_PLAY_MS = 5000;

    private final DanceRepository repository = new DanceRepository();
    private MediaPlayer mediaPlayer;

    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context,
                "Lass mich kurz einen passenden Tanz für dich einstudieren.");

        ThinkingController.get().start(context);
        DanceEntity dance;
        try {
            dance = repository.getOrCreate(context, query(input));
        } catch (Exception e) {
            ThinkingController.get().stop();
            Log.w(TAG, "Dance preparation failed: " + e.getMessage());
            SpeechManager.getInstance().systemSay(context,
                    "Diesen Song bekomme ich gerade nicht, ich tanze etwas Eigenes.");
            playFallback(context);
            return;
        }
        ThinkingController.get().stop();
        playDance(context, dance);
    }

    private String query(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "dance music";
        }
        return input.trim();
    }

    private void playDance(QiContext context, DanceEntity dance) {
        Future<Void> animationFuture = null;
        try {
            String qianim = DanceRepository.readQianim(new File(dance.qianimPath));
            Animation animation = AnimationBuilder.with(context).withTexts(qianim).build();
            Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();

            boolean audioPlaying = dance.previewUrl != null
                    && startAudioUrl(dance.previewUrl, dance.audioStartMs);

            animationFuture = animate.async().run();
            QiFutures.consume(animationFuture, TAG, "dance animation");

            long clipMs = audioPlaying && mediaPlayer != null
                    ? mediaPlayer.getDuration() - dance.audioStartMs : dance.durationMs;
            if (clipMs <= 0) {
                clipMs = dance.durationMs;
            }
            long playMs = Math.max(MIN_PLAY_MS, Math.min(MAX_PLAY_MS, clipMs));
            sleep(playMs);

            stopAudio();
            if (!animationFuture.isDone()) {
                animationFuture.requestCancellation();
            }
        } catch (Exception e) {
            Log.w(TAG, "Dance playback failed: " + e.getMessage());
            stopAudio();
            if (animationFuture != null && !animationFuture.isDone()) {
                animationFuture.requestCancellation();
            }
            playFallback(context);
        }
    }

    private boolean startAudioUrl(String url, long startMs) {
        stopAudio();
        try {
            MediaPlayer player = new MediaPlayer();
            mediaPlayer = player;
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            if (startMs > 0 && startMs < player.getDuration()) {
                player.seekTo((int) startMs);
            }
            player.start();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Preview playback failed: " + e.getMessage());
            stopAudio();
            return false;
        }
    }

    private void playFallback(QiContext context) {
        try {
            SpeechManager.getInstance().systemSay(context, "Six... seven!");

            int audioRes = context.getResources()
                    .getIdentifier("doot_doot", "raw", context.getPackageName());
            if (audioRes == 0) {
                audioRes = R.raw.wyoming;
            }
            startAudioResource(context, audioRes);

            long clipMs = mediaPlayer != null ? mediaPlayer.getDuration() : 0;
            if (clipMs <= 0) {
                clipMs = 15000;
            }
            long playMs = Math.max(MIN_PLAY_MS, Math.min(MAX_PLAY_MS, clipMs));

            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.six_seven).build();
            Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();

            long end = System.currentTimeMillis() + playMs;
            while (System.currentTimeMillis() < end) {
                animate.run();
            }
            stopAudio();
        } catch (Exception e) {
            Log.w(TAG, "Fallback dance failed: " + e.getMessage());
            stopAudio();
        }
    }

    private void startAudioResource(QiContext context, int resId) {
        stopAudio();
        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void stopAudio() {
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;
        if (player != null) {
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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getDescription() {
        return "Makes Pepper dance to a song. The user can name any song or artist; Pepper plays a "
                + "music preview and performs a generated choreography to it.";
    }
}
