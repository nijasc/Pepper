package com.buhlergroup.pepper.action.highfive;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.lang.SpeechManager;

public class HighFiveAction extends Action {
    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context, "High five!");

        Animation myAnimation = AnimationBuilder.with(context)
                .withResources(R.raw.pepper_highfive)
                .build();

        Animate animate = AnimateBuilder.with(context)
                .withAnimation(myAnimation)
                .build();

        QiFutures.consume(animate.async().run(), "HighFive", "Animation");

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "Gives the user a high five.";
    }
}
