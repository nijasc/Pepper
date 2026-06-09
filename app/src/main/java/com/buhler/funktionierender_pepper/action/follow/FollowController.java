package com.buhler.funktionierender_pepper.action.follow;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.LookAtBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.object.actuation.LookAtMovementPolicy;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.human.Human;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

import java.util.List;

public final class FollowController {

    public interface FollowStateListener {
        void onFollowStateChanged(boolean following);
    }

    private static final String TAG = "FollowController";

    private static final double STAND_OFF_M = 0.6;
    private static final double DEAD_ZONE_M = 0.10;
    private static final double RETARGET_M = 0.30;
    /**
     * How far the followed person may have moved between two loop iterations and still be
     * recognised as the same target. Keeps Pepper locked onto one person instead of jumping
     * to whoever happens to be closest.
     */
    private static final double TARGET_LOCK_GATE_M = 0.75;
    private static final long LOOP_PAUSE_MS = 200;
    private static final int MAX_MISSES = 25;

    private static final FollowController INSTANCE = new FollowController();
    private volatile boolean wantFollow = false;
    private volatile boolean sessionRunning = false;
    private volatile int generation = 0;
    private volatile FollowStateListener stateListener;
    private Thread loopThread;

    private FollowController() {
    }

    public static FollowController get() {
        return INSTANCE;
    }

    public boolean isFollowing() {
        return wantFollow;
    }

    public void setFollowStateListener(FollowStateListener listener) {
        this.stateListener = listener;
    }

    public synchronized void requestFollow(QiContext context) {
        setWantFollow(true);
        startSession(context);
    }

    public synchronized void cancelFollow() {
        setWantFollow(false);
        stopSession();
    }

    public synchronized void stop() {
        cancelFollow();
    }

    public synchronized void onFocusGained(QiContext context) {
        if (wantFollow) startSession(context);
    }

    public synchronized void onFocusLost() {
        stopSession();
    }

    private void setWantFollow(boolean value) {
        if (wantFollow == value) return;
        wantFollow = value;
        FollowStateListener l = stateListener;
        if (l != null) l.onFollowStateChanged(value);
    }

    private void startSession(QiContext context) {
        if (sessionRunning) return;
        sessionRunning = true;
        final int gen = ++generation;
        loopThread = new Thread(() -> runSession(context, gen), "FollowMeLoop");
        loopThread.start();
    }

    private void stopSession() {
        sessionRunning = false;
        generation++;
        if (loopThread != null) {
            loopThread.interrupt();
            loopThread = null;
        }
    }

    private void runSession(QiContext context, int gen) {
        Holder holder = null;
        Future<Void> goToFuture = null;
        Future<Void> lookAtFuture = null;
        Future<ListenResult> listenFuture = null;
        FreeFrame activeTarget = null;
        boolean trackValid = false;
        int misses = 0;

        try {
            Frame robotFrame = context.getActuation().robotFrame();

            // Hold BASIC_AWARENESS so Pepper's autonomous head movement does not fight our
            // explicit LookAt, and BACKGROUND_MOVEMENT so it stays still between go-to moves.
            holder = HolderBuilder.with(context)
                    .withAutonomousAbilities(
                            AutonomousAbilitiesType.BASIC_AWARENESS,
                            AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                    .build();
            holder.hold();

            listenFuture = startStopListener(context);

            // A single frame we keep re-pointing at the followed person. The base drives towards
            // it (GoTo) while the head keeps looking at it (LookAt, HEAD_ONLY) at the same time.
            FreeFrame trackFrame = context.getMapping().makeFreeFrame();

            while (gen == generation && sessionRunning && wantFollow
                    && !Thread.currentThread().isInterrupted()) {

                Human human = selectHuman(context, robotFrame, trackValid ? trackFrame : null);
                if (human == null) {
                    if (++misses >= MAX_MISSES) break;
                    Thread.sleep(LOOP_PAUSE_MS);
                    continue;
                }
                misses = 0;

                Transform t = human.getHeadFrame().computeTransform(robotFrame).getTransform();
                double x = t.getTranslation().getX();
                double y = t.getTranslation().getY();
                double distance = Math.hypot(x, y);

                // Re-point the shared frame at the person and make sure the head is tracking it.
                trackFrame.update(robotFrame, t, 0L);
                trackValid = true;
                if (lookAtFuture == null || lookAtFuture.isDone()) {
                    lookAtFuture = startHeadTracking(context, trackFrame);
                }

                if (distance <= STAND_OFF_M + DEAD_ZONE_M) {
                    // Close enough: stop driving but keep the head following the person.
                    requestCancel(goToFuture);
                    goToFuture = null;
                    activeTarget = null;
                    Thread.sleep(LOOP_PAUSE_MS);
                    continue;
                }

                double scale = (distance - STAND_OFF_M) / distance;
                Transform goalTf = TransformBuilder.create()
                        .from2DTransform(x * scale, y * scale, Math.atan2(y, x));
                FreeFrame candidate = context.getMapping().makeFreeFrame();
                candidate.update(robotFrame, goalTf, 0L);

                boolean retarget = goToFuture == null
                        || goToFuture.isDone()
                        || targetMoved(candidate, activeTarget) > RETARGET_M;

                if (retarget) {
                    requestCancel(goToFuture);
                    goToFuture = GoToBuilder.with(context)
                            .withFrame(candidate.frame())
                            .build()
                            .async().run();
                    activeTarget = candidate;
                }

                Thread.sleep(LOOP_PAUSE_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            Log.w(TAG, "Follow-Session beendet: " + e.getMessage());
        } finally {
            requestCancel(goToFuture);
            requestCancel(lookAtFuture);
            requestCancel(listenFuture);
            releaseQuietly(holder);
            if (gen == generation) {
                sessionRunning = false;
                setWantFollow(false);
            }
        }
    }

    private Future<Void> startHeadTracking(QiContext context, FreeFrame target) {
        LookAt lookAt = LookAtBuilder.with(context)
                .withFrame(target.frame())
                .build();
        // Only the head should track the person; the base orientation is handled by GoTo.
        lookAt.setPolicy(LookAtMovementPolicy.HEAD_ONLY);
        return lookAt.async().run();
    }

    private Future<ListenResult> startStopListener(QiContext context) {
        PhraseSet phrases = PhraseSetBuilder.with(context)
                .withTexts("stopp", "stop", "bleib hier", "bleib stehen", "halt an")
                .build();
        Listen listen = ListenBuilder.with(context)
                .withPhraseSet(phrases)
                .build();

        Future<ListenResult> future = listen.async().run();
        future.thenConsume(f -> {
            if (f.isCancelled() || f.hasError()) return;
            ListenResult result = f.get();
            if (result != null
                    && result.getHeardPhrase() != null
                    && !result.getHeardPhrase().getText().isEmpty()) {
                sayQuietly(context, "Ich bleibe jetzt hier.");
                cancelFollow();
            }
        });
        return future;
    }

    private void requestCancel(Future<?> f) {
        if (f != null && !f.isDone()) f.requestCancellation();
    }

    private void releaseQuietly(Holder holder) {
        if (holder == null) return;
        try {
            holder.release();
        } catch (Exception ignored) {
        }
    }

    private void sayQuietly(QiContext context, String text) {
        try {
            SpeechManager.getInstance().say(context, text);
        } catch (Exception ignored) {
        }
    }

    private double targetMoved(FreeFrame candidate, FreeFrame active) {
        if (active == null) return Double.MAX_VALUE;
        try {
            Transform d = candidate.frame().computeTransform(active.frame()).getTransform();
            return Math.hypot(d.getTranslation().getX(), d.getTranslation().getY());
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Picks the human to follow. When we already track someone ({@code lastTarget} set), the
     * person closest to that last position wins as long as they stayed within
     * {@link #TARGET_LOCK_GATE_M} — this keeps Pepper locked onto one person. Otherwise (or when
     * the locked person disappeared) it falls back to the human closest to the robot.
     */
    private Human selectHuman(QiContext context, Frame robotFrame, FreeFrame lastTarget) {
        List<Human> humans = context.getHumanAwareness().getHumansAround();

        Human closestToRobot = null;
        double bestRobot = Double.MAX_VALUE;
        Human closestToLast = null;
        double bestLast = Double.MAX_VALUE;

        for (Human h : humans) {
            try {
                Frame head = h.getHeadFrame();

                Transform tr = head.computeTransform(robotFrame).getTransform();
                double dRobot = Math.hypot(tr.getTranslation().getX(), tr.getTranslation().getY());
                if (dRobot < bestRobot) {
                    bestRobot = dRobot;
                    closestToRobot = h;
                }

                if (lastTarget != null) {
                    Transform tl = head.computeTransform(lastTarget.frame()).getTransform();
                    double dLast = Math.hypot(tl.getTranslation().getX(),
                            tl.getTranslation().getY());
                    if (dLast < bestLast) {
                        bestLast = dLast;
                        closestToLast = h;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (closestToLast != null && bestLast <= TARGET_LOCK_GATE_M) {
            return closestToLast;
        }
        return closestToRobot;
    }
}
