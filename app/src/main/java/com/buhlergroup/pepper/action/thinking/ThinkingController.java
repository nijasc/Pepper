package com.buhlergroup.pepper.action.thinking;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

public final class ThinkingController {

    private static final String TAG = "Thinking";

    private static final String[] FILLERS_DE = {
            "Hmm, lass mich kurz überlegen.",
            "Mhm, einen kleinen Moment.",
            "Hmm, gute Frage."
    };

    private static final String[] FILLERS_EN = {
            "Hmm, let me think for a moment.",
            "Mhm, just a second.",
            "Hmm, good question."
    };

    private static final ThinkingController INSTANCE = new ThinkingController();

    private volatile Future<Void> animationFuture;
    private volatile Future<Void> fillerFuture;
    private volatile boolean active;
    private int lastFiller = -1;

    private ThinkingController() {
    }

    public static ThinkingController get() {
        return INSTANCE;
    }

    public synchronized void start(QiContext context) {
        if (active) {
            return;
        }
        active = true;
        startPose(context);
        startFiller(context);
    }

    public synchronized void stop() {
        if (!active) {
            return;
        }
        active = false;
        cancel(animationFuture);
        cancel(fillerFuture);
        animationFuture = null;
        fillerFuture = null;
    }

    private void startPose(QiContext context) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.searching_a001)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            animationFuture = animate.async().run();
        } catch (Exception e) {
            Log.w(TAG, "Thinking pose failed: " + e.getMessage());
        }
    }

    private void startFiller(QiContext context) {
        try {
            SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
            String[] fillers = lang == SupportedLanguage.ENGLISH ? FILLERS_EN : FILLERS_DE;
            String text = fillers[pickIndex(fillers.length)];
            Locale locale = new Locale(lang.getQiLang(), lang.getRegion());
            Say say = SayBuilder.with(context)
                    .withText(text)
                    .withLocale(locale)
                    .build();
            fillerFuture = say.async().run();
        } catch (Exception e) {
            Log.w(TAG, "Thinking filler failed: " + e.getMessage());
        }
    }

    private int pickIndex(int length) {
        int index;
        do {
            index = (int) (Math.random() * length);
        } while (length > 1 && index == lastFiller);
        lastFiller = index;
        return index;
    }

    private void cancel(Future<Void> future) {
        if (future != null) {
            try {
                future.requestCancellation();
            } catch (Exception ignored) {
            }
        }
    }
}
