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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicAnimationAction extends Action {

    private static final String TAG = "DynAnim";
    private static final Pattern SECONDS = Pattern.compile(
            "(\\d+)\\s*(?:sekunden?|seconds?|secs?|sek\\b|s\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES = Pattern.compile(
            "(\\d+)\\s*(?:minuten?|minutes?|mins?\\b)", Pattern.CASE_INSENSITIVE);

    private final AnimationGenerator generator = new AnimationGenerator();

    @Override
    public void execute(QiContext context, String input) {
        int targetSeconds = parseSeconds(input);
        String qianim = generator.generateValidated(context, input, targetSeconds);
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
            Log.e(TAG, "Animation playback failed: " + e.getMessage() + "\nGenerated qianim:\n" + qianim, e);
            SpeechManager.getInstance().systemSay(context,
                    "Hoppla, diese Bewegung konnte ich nicht ausführen.");
        }
    }

    private int parseSeconds(String input) {
        if (input == null) {
            return 0;
        }
        Matcher minutes = MINUTES.matcher(input);
        if (minutes.find()) {
            try {
                return Integer.parseInt(minutes.group(1)) * 60;
            } catch (NumberFormatException ignored) {
            }
        }
        Matcher seconds = SECONDS.matcher(input);
        if (seconds.find()) {
            try {
                return Integer.parseInt(seconds.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    @Override
    public String getDescription() {
        return "Generates and performs a custom body movement or gesture on request, for example "
                + "\"move your head to the left\", \"raise your right arm\", \"nod\", \"make a gesture\". "
                + "Use this when the visitor asks Pepper to physically move a specific body part or make a "
                + "gesture, but NOT for a full dance with music.";
    }
}
