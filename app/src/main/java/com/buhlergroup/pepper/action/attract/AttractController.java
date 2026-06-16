package com.buhlergroup.pepper.action.attract;

import android.os.SystemClock;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.human.Human;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.List;

public final class AttractController {

    private static final String TAG = "AttractController";
    private static final double ROAM_STEP_M = 0.8;
    private static final double ROAM_TURN_MAX = 0.6;
    private static final double GREET_DISTANCE_M = 1.5;
    private static final long ROAM_PAUSE_MS = 1500;

    private static final AttractController INSTANCE = new AttractController();

    private volatile long lastInteractionMs = SystemClock.elapsedRealtime();
    private volatile long lastGreetMs;
    private volatile boolean active;
    private volatile boolean greeting;
    private volatile Thread roamThread;
    private volatile Future<Void> activeGoTo;

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
        if (context == null) {
            return;
        }
        boolean shouldRun = AttractSettings.isEnabled(context)
                && !overlayOpen && !busy && idleElapsed(context);
        if (shouldRun && !active) {
            startAttract(context);
        } else if (!shouldRun && active) {
            stopAttract();
        }
    }

    public void stop() {
        stopAttract();
    }

    private boolean idleElapsed(QiContext context) {
        long idleMs = AttractSettings.getIdleMinutes(context) * 60_000L;
        return SystemClock.elapsedRealtime() - lastInteractionMs >= idleMs;
    }

    private void startAttract(QiContext context) {
        if (active) {
            return;
        }
        active = true;
        Log.i(TAG, "Attract mode started");
        DebugLog.get().setStatus("Attract-Modus gestartet");
        DebugLog.get().i(TAG, "Attract-Modus gestartet");
        startRoaming(context);
    }

    private void stopAttract() {
        if (!active) {
            return;
        }
        active = false;
        Log.i(TAG, "Attract mode stopped");
        DebugLog.get().setStatus("Attract-Modus gestoppt");
        DebugLog.get().i(TAG, "Attract-Modus gestoppt");
        cancelGoTo();
    }

    private void startRoaming(QiContext context) {
        Thread running = roamThread;
        if (running != null && running.isAlive()) {
            return;
        }
        Thread thread = new Thread(() -> roamLoop(context), "attract-roam");
        thread.setDaemon(true);
        roamThread = thread;
        thread.start();
    }

    private void roamLoop(QiContext context) {
        try {
            while (active) {
                if (personClose(context)) {
                    greet(context);
                    sleep(ROAM_PAUSE_MS);
                } else {
                    driveRoamStep(context);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Attract roaming failed: " + e.getMessage());
        } finally {
            roamThread = null;
        }
    }

    private void driveRoamStep(QiContext context) {
        try {
            double turn = (Math.random() * 2.0 - 1.0) * ROAM_TURN_MAX;
            Frame robotFrame = context.getActuation().robotFrame();
            Transform transform = TransformBuilder.create().from2DTransform(ROAM_STEP_M, 0.0, turn);
            FreeFrame target = context.getMapping().makeFreeFrame();
            target.update(robotFrame, transform, 0L);
            Future<Void> goTo = GoToBuilder.with(context)
                    .withFrame(target.frame()).build().async().run();
            activeGoTo = goTo;
            awaitGoTo(goTo);
        } catch (Exception e) {
            Log.w(TAG, "Attract roam step failed: " + e.getMessage());
            sleep(ROAM_PAUSE_MS);
        }
    }

    private void awaitGoTo(Future<Void> future) {
        while (active && !future.isDone()) {
            sleep(100);
        }
        if (!future.isDone()) {
            future.requestCancellation();
        }
    }

    private void cancelGoTo() {
        Future<Void> future = activeGoTo;
        activeGoTo = null;
        if (future != null && !future.isDone()) {
            future.requestCancellation();
        }
    }

    private void greet(QiContext context) {
        if (greeting) {
            return;
        }
        long greetMs = AttractSettings.getGreetSeconds(context) * 1000L;
        if (SystemClock.elapsedRealtime() - lastGreetMs < greetMs) {
            return;
        }
        greeting = true;
        try {
            lastGreetMs = SystemClock.elapsedRealtime();
            DebugLog.get().setStatus("Attract – begrüße Besucher");
            DebugLog.get().i(TAG, "Attract – Besucher begrüßt");
            waveHand(context);
            SpeechManager.getInstance().say(context, greetingText());
        } catch (Exception e) {
            Log.w(TAG, "Attract greeting failed: " + e.getMessage());
        } finally {
            greeting = false;
        }
    }

    private boolean personClose(QiContext context) {
        try {
            List<Human> humans = context.getHumanAwareness().getHumansAround();
            if (humans == null || humans.isEmpty()) {
                return false;
            }
            Frame robotFrame = context.getActuation().robotFrame();
            for (Human human : humans) {
                Frame head = human.getHeadFrame();
                if (head == null) {
                    continue;
                }
                Transform t = head.computeTransform(robotFrame).getTransform();
                double distance = Math.hypot(t.getTranslation().getX(), t.getTranslation().getY());
                if (distance <= GREET_DISTANCE_M) {
                    return true;
                }
            }
            return false;
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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
