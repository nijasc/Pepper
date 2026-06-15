package com.buhlergroup.pepper.action.saxophone;

import android.media.MediaPlayer;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.lang.SpeechManager;

public class SaxophoneAction extends Action {

    private static final long MAX_PLAY_MS = 35000;

    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context, "Hier ist eine kurze Saxophon performance für dich");

        Animation myAnimation = AnimationBuilder.with(context)
                .withResources(R.raw.saxophone_a001)
                .build();

        Animate animate = AnimateBuilder.with(context)
                .withAnimation(myAnimation)
                .build();

        QiFutures.consume(animate.async().run(), "Saxophone", "Animation");

        MediaPlayer player = MediaPlayer.create(context, R.raw.saxophone_song);
        if (player == null) {
            return;
        }
        try {
            player.start();
            long duration = player.getDuration();
            long playMs = duration > 0 ? Math.min(duration, MAX_PLAY_MS) : MAX_PLAY_MS;
            Thread.sleep(playMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (Exception ignored) {
            }
            player.release();
        }
    }

    @Override
    public String getDescription() {
        return "Makes Pepper play the saxophone.";
    }
}
