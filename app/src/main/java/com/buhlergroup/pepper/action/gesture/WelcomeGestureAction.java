package com.buhlergroup.pepper.action.gesture;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.openai.history.HistoryManager;

public class WelcomeGestureAction extends Action {

    public WelcomeGestureAction(HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        Animation animation = AnimationBuilder.with(context)
                .withResources(R.raw.gesture_welcome_arm)
                .build();

        Animate animate = AnimateBuilder.with(context)
                .withAnimation(animation)
                .build();

        QiFutures.consume(animate.async().run(), "WelcomeGesture", "Animation");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getDescription() {
        return "Extends the right arm forward with an open, upturned palm to welcome or invite someone.";
    }
}
