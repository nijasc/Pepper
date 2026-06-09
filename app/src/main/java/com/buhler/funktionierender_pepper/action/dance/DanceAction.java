package com.buhler.funktionierender_pepper.action.dance;

import android.media.MediaPlayer;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhler.funktionierender_pepper.R;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

public class DanceAction extends Action {
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context, "Diese Tanzeinlage habe ich extra einstudiert für dich!");

        Animation myAnimation = AnimationBuilder.with(context)
                .withResources(R.raw.wyoming_dance)
                .build();

        Animate animate = AnimateBuilder.with(context)
                .withAnimation(myAnimation)
                .build();

        animate.async().run();
        playMedia(context, R.raw.wyoming);

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
        return "Tanzt cool und spielt musik.";
    }
}
