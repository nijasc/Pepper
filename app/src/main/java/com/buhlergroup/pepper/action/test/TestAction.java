package com.buhlergroup.pepper.action.test;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.actuation.OrientationPolicy;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.history.HistoryManager;

public class TestAction extends Action {

    public TestAction(HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context, "Ich drehe mich einmal im Kreis");

        Actuation actuation = context.getActuation();
        Mapping mapping = context.getMapping();

        int steps = 4;
        double angleStep = Math.toRadians(360.0 / steps);

        for (int i = 0; i < steps; i++) {
            Frame robotFrame = actuation.robotFrame();
            Transform transform = TransformBuilder.create().from2DTransform(0.0, 0.0, angleStep);
            FreeFrame target = mapping.makeFreeFrame();
            target.update(robotFrame, transform, 0L);

            GoTo goTo = GoToBuilder.with(context)
                    .withFrame(target.frame())
                    .withFinalOrientationPolicy(OrientationPolicy.FREE_ORIENTATION)
                    .build();

            try {
                goTo.run();
            } catch (Exception e) {
                Log.e("TestAction", "Drehung fehlgeschlagen", e);
                return;
            }
        }

        Log.i("TestAction", "Drehung erfolgreich");
    }

    @Override
    public String getDescription() {
        return "Test action; only run when explicitly requested.";
    }
}
