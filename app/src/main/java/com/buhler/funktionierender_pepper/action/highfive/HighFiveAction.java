package com.buhler.funktionierender_pepper.action.highfive;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhler.funktionierender_pepper.R;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

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

        animate.async().run();

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "Spielt das Saxophon";
    }
}
