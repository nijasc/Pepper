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
import com.buhlergroup.pepper.util.FutureUtils;

import java.util.List;
import java.util.Locale;

public final class AttractController {

    private static final String TAG = "AttractController";
    private static final double ROAM_STEP_M = 0.8;
    private static final double ROAM_TURN_MAX = 0.6;
    private static final double GREET_DISTANCE_M = 1.5;
    private static final long ROAM_PAUSE_MS = 1500;
    private static final double STUCK_PROGRESS_M = 0.15;
    private static final int STUCK_THRESHOLD = 2;
    private static final double RECOVERY_BACK_M = 0.25;
    private static final double RECOVERY_TURN_MIN = 1.6;
    private static final double RECOVERY_TURN_MAX = 2.8;
    private static final AttractController INSTANCE = new AttractController();
    private volatile long lastInteractionMs = SystemClock.elapsedRealtime();
    private volatile long lastGreetMs;
    private volatile boolean active;
    private volatile boolean greeting;
    private volatile Thread roamThread;
    private volatile Future<Void> activeGoTo;
    private volatile RoamPhase roamPhase;
    private volatile int stuckCount;
    private volatile ListeningRelevanceListener listeningListener;

    private AttractController() {
    }

    public static AttractController get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPersonClose() {
        return active && roamPhase == RoamPhase.PERSON_CLOSE;
    }

    public void setListeningRelevanceListener(ListeningRelevanceListener listener) {
        this.listeningListener = listener;
    }

    private void notifyListeningRelevance() {
        ListeningRelevanceListener l = listeningListener;
        if (l != null) {
            l.onListeningRelevanceChanged();
        }
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
                && !overlayOpen && !busy && idleElapsed();
        if (shouldRun && !active) {
            startAttract(context);
        } else if (!shouldRun && active) {
            stopAttract();
        }
    }

    public void stop() {
        stopAttract();
    }

    private boolean idleElapsed() {
        long idleMs = AttractSettings.getIdleMinutes() * 60_000L;
        return SystemClock.elapsedRealtime() - lastInteractionMs >= idleMs;
    }

    private void startAttract(QiContext context) {
        if (active) {
            return;
        }
        active = true;
        roamPhase = null;
        stuckCount = 0;
        Log.i(TAG, "Attract mode started");
        DebugLog.get().setStatus("Attract-Modus gestartet");
        DebugLog.get().i(TAG, "Attract-Modus gestartet");
        startRoaming(context);
        notifyListeningRelevance();
    }

    private void stopAttract() {
        if (!active) {
            return;
        }
        active = false;
        roamPhase = null;
        Log.i(TAG, "Attract mode stopped");
        DebugLog.get().setStatus("Attract-Modus gestoppt");
        DebugLog.get().i(TAG, "Attract-Modus gestoppt");
        cancelGoTo();
        notifyListeningRelevance();
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
                double distance = nearestPersonDistance(context);
                if (!Double.isNaN(distance) && distance <= GREET_DISTANCE_M) {
                    updateRoamPhase(RoamPhase.PERSON_CLOSE, distance);
                    greet(context);
                    sleep(ROAM_PAUSE_MS);
                } else {
                    updateRoamPhase(
                            Double.isNaN(distance) ? RoamPhase.SEARCHING : RoamPhase.PERSON_FAR,
                            distance);
                    driveRoamStep(context);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Attract roaming failed: " + e.getMessage());
        } finally {
            roamThread = null;
        }
    }

    private void updateRoamPhase(RoamPhase phase, double distance) {
        if (roamPhase == phase) {
            return;
        }
        RoamPhase previous = roamPhase;
        roamPhase = phase;
        if ((previous == RoamPhase.PERSON_CLOSE) != (phase == RoamPhase.PERSON_CLOSE)) {
            notifyListeningRelevance();
        }
        switch (phase) {
            case SEARCHING:
                DebugLog.get().setStatus("Attract – Suche nach Person");
                DebugLog.get().i(TAG, previous == null
                        ? "Attract – Suche nach Person"
                        : "Attract – Interesse verloren, suche wieder");
                break;
            case PERSON_FAR:
                DebugLog.get().setStatus(String.format(Locale.US,
                        "Attract – Person erkannt (%.1f m, zu weit)", distance));
                DebugLog.get().i(TAG, String.format(Locale.US,
                        "Attract – Person erkannt: %.1f m (zu weit zum Begrüßen)", distance));
                break;
            case PERSON_CLOSE:
                DebugLog.get().setStatus(String.format(Locale.US,
                        "Attract – Person in der Nähe (%.1f m)", distance));
                DebugLog.get().i(TAG, String.format(Locale.US,
                        "Attract – Person erfasst: %.1f m", distance));
                break;
        }
    }

    private void driveRoamStep(QiContext context) {
        try {
            double turn = (Math.random() * 2.0 - 1.0) * ROAM_TURN_MAX;
            double moved = runStep(context, ROAM_STEP_M, turn);
            if (!Double.isNaN(moved) && moved < STUCK_PROGRESS_M) {
                stuckCount++;
                DebugLog.get().i(TAG, String.format(Locale.US,
                        "Attract – wenig Fortschritt (%.2f m), blockiert=%d", moved, stuckCount));
                if (stuckCount >= STUCK_THRESHOLD) {
                    recoverFromStuck(context);
                    stuckCount = 0;
                }
            } else {
                stuckCount = 0;
            }
        } catch (Exception e) {
            Log.w(TAG, "Attract roam step failed: " + e.getMessage());
            sleep(ROAM_PAUSE_MS);
        }
    }

    private double runStep(QiContext context, double x, double theta) {
        try {
            Frame robotFrame = context.getActuation().robotFrame();
            FreeFrame start = context.getMapping().makeFreeFrame();
            start.update(robotFrame, TransformBuilder.create().from2DTransform(0.0, 0.0, 0.0), 0L);
            Transform transform = TransformBuilder.create().from2DTransform(x, 0.0, theta);
            FreeFrame target = context.getMapping().makeFreeFrame();
            target.update(robotFrame, transform, 0L);
            Future<Void> goTo = GoToBuilder.with(context)
                    .withFrame(target.frame()).build().async().run();
            activeGoTo = goTo;
            awaitGoTo(goTo);
            return movedDistance(context, start);
        } catch (Exception e) {
            Log.w(TAG, "Attract step failed: " + e.getMessage());
            return Double.NaN;
        }
    }

    private double movedDistance(QiContext context, FreeFrame start) {
        try {
            Frame robotFrame = context.getActuation().robotFrame();
            Transform t = robotFrame.computeTransform(start.frame()).getTransform();
            return Math.hypot(t.getTranslation().getX(), t.getTranslation().getY());
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private void recoverFromStuck(QiContext context) {
        if (!active) {
            return;
        }
        DebugLog.get().setStatus("Attract – Engstelle, weiche aus");
        DebugLog.get().i(TAG, "Attract – steckengeblieben, Recovery-Manöver");
        cancelGoTo();
        double turn = RECOVERY_TURN_MIN + Math.random() * (RECOVERY_TURN_MAX - RECOVERY_TURN_MIN);
        if (Math.random() < 0.5) {
            turn = -turn;
        }
        runStep(context, -RECOVERY_BACK_M, turn);
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
        FutureUtils.cancel(future);
    }

    private void greet(QiContext context) {
        if (greeting) {
            return;
        }
        long greetMs = AttractSettings.getGreetSeconds() * 1000L;
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

    private double nearestPersonDistance(QiContext context) {
        try {
            List<Human> humans = context.getHumanAwareness().getHumansAround();
            if (humans == null || humans.isEmpty()) {
                return Double.NaN;
            }
            Frame robotFrame = context.getActuation().robotFrame();
            double nearest = Double.NaN;
            for (Human human : humans) {
                Frame head = human.getHeadFrame();
                if (head == null) {
                    continue;
                }
                Transform t = head.computeTransform(robotFrame).getTransform();
                double distance = Math.hypot(t.getTranslation().getX(), t.getTranslation().getY());
                if (Double.isNaN(nearest) || distance < nearest) {
                    nearest = distance;
                }
            }
            return nearest;
        } catch (Exception e) {
            return Double.NaN;
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

    private enum RoamPhase {
        SEARCHING,
        PERSON_FAR,
        PERSON_CLOSE
    }

    public interface ListeningRelevanceListener {
        void onListeningRelevanceChanged();
    }
}
