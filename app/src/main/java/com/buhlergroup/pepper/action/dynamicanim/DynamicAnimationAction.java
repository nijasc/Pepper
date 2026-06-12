package com.buhlergroup.pepper.action.dynamicanim;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.thinking.ThinkingController;
import com.buhlergroup.pepper.lang.SpeechManager;

public class DynamicAnimationAction extends Action {

    private static final String TAG = "DynAnim";

    private final AnimationGenerator generator = new AnimationGenerator();

    @Override
    public void execute(QiContext context, String input) {
        String qianim = generator.generateValidated(context, input);
        ThinkingController.get().stop();

        if (qianim == null) {
            SpeechManager.getInstance().systemSay(context,
                    "Diese Bewegung bekomme ich gerade nicht sauber hin, tut mir leid.");
            return;
        }

        try {
            Animation animation = AnimationBuilder.with(context)
                    .withTexts(qianim)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            animate.run();
        } catch (Exception e) {
            Log.w(TAG, "Animation playback failed: " + e.getMessage());
            SpeechManager.getInstance().systemSay(context,
                    "Hoppla, diese Bewegung konnte ich nicht ausführen.");
        }
    }

    @Override
    public String getDescription() {
        return "Generates and performs a custom body movement or gesture on request, for example "
                + "\"move your head to the left\", \"raise your right arm\", \"nod\", \"make a gesture\". "
                + "Use this when the visitor asks Pepper to physically move a specific body part or make a "
                + "gesture, but NOT for a full dance with music.";
    }
}
