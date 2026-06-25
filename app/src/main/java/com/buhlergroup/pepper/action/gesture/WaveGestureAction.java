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

/**
 * Waves the right hand in greeting (Winken). Pure gesture, no speech, so it can be
 * filmed cleanly and looped (e.g. via the Actor admin tile). Built for the Bühler
 * Future Marketing summer-campaign video.
 */
public class WaveGestureAction extends Action {

    public WaveGestureAction(HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        Animation animation = AnimationBuilder.with(context)
                .withResources(R.raw.gesture_wave_hand)
                .build();

        Animate animate = AnimateBuilder.with(context)
                .withAnimation(animation)
                .build();

        QiFutures.consume(animate.async().run(), "WaveGesture", "Animation");

        try {
            Thread.sleep(3800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getDescription() {
        return "Waves the right hand to greet or say hello.";
    }
}
