package com.buhlergroup.pepper.action.attract;

import android.os.SystemClock;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.human.Human;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.List;

public final class AttractController {

    private static final String TAG = "AttractController";
    private static final long IDLE_MS = 120000;
    private static final long GREET_INTERVAL_MS = 45000;

    private static final AttractController INSTANCE = new AttractController();

    private volatile long lastInteractionMs = SystemClock.elapsedRealtime();
    private volatile long lastGreetMs;
    private volatile boolean active;
    private volatile boolean greeting;

    private AttractController() {
    }

    public static AttractController get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active;
    }

    public void notifyInteraction() {
        lastInteractionMs = SystemClock.elapsedRealtime();
        if (active) {
            stopAttract();
        }
    }

    public void tick(QiContext context, boolean overlayOpen, boolean busy) {
        if (overlayOpen || busy) {
            lastInteractionMs = SystemClock.elapsedRealtime();
            if (active) {
                stopAttract();
            }
            return;
        }
        if (!active && SystemClock.elapsedRealtime() - lastInteractionMs > IDLE_MS) {
            startAttract();
        }
        if (active) {
            maybeGreet(context);
        }
    }

    private void maybeGreet(QiContext context) {
        if (context == null || greeting) {
            return;
        }
        if (SystemClock.elapsedRealtime() - lastGreetMs < GREET_INTERVAL_MS) {
            return;
        }
        greeting = true;
        Thread thread = new Thread(() -> {
            try {
                if (!personPresent(context)) {
                    return;
                }
                lastGreetMs = SystemClock.elapsedRealtime();
                waveHand(context);
                SpeechManager.getInstance().say(context, greetingText());
            } catch (Exception e) {
                Log.w(TAG, "Attract greeting failed: " + e.getMessage());
            } finally {
                greeting = false;
            }
        }, "attract-greet");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean personPresent(QiContext context) {
        try {
            List<Human> humans = context.getHumanAwareness().getHumansAround();
            return humans != null && !humans.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void waveHand(QiContext context) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.raise_right_hand_b001).build();
            Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();
            animate.run();
        } catch (Exception e) {
            Log.w(TAG, "Attract wave failed: " + e.getMessage());
        }
    }

    private String greetingText() {
        SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
        if (lang == SupportedLanguage.ENGLISH) {
            return "Hi there! Come on over — I can dance, take a selfie and much more.";
        }
        return "Hallo! Komm doch vorbei — ich kann tanzen, ein Selfie machen und vieles mehr.";
    }

    private void startAttract() {
        active = true;
        Log.i(TAG, "Attract mode activated after idle period");
    }

    private void stopAttract() {
        active = false;
        Log.i(TAG, "Attract mode deactivated");
    }
}
