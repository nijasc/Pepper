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

    private MediaPlayer mediaPlayer = new MediaPlayer();

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
        playMedia(context, R.raw.saxophone_song);

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaPlayer.stop();
    }

    private void playMedia(QiContext context, int mediaResource) {
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(context, mediaResource);
        mediaPlayer.start();
    }

    @Override
    public String getDescription() {
        return "Makes Pepper play the saxophone.";
    }
}
