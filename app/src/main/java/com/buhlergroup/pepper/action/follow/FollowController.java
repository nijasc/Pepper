package com.buhlergroup.pepper.action.follow;

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
import com.aldebaran.qi.sdk.object.actuation.OrientationPolicy;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.human.Human;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.List;

public final class FollowController {

    private static final String TAG = "FollowController";
    private static final double STAND_OFF_M = 0.7;
    private static final double DEAD_ZONE_M = 0.15;
    private static final double RETARGET_M = 0.40;
    private static final double TURN_THRESHOLD_RAD = Math.toRadians(25);
    private static final long MIN_GOTO_INTERVAL_MS = 800;
    private static final double TARGET_LOCK_GATE_M = 1.0;
    private static final long LOOP_PAUSE_MS = 150;
    private static final int MAX_MISSES = 40;
    private static final long FOLLOW_CONFIRM_INTERVAL_MS = 30000;
    private static final String[] FOLLOW_CONFIRMATIONS = {
            "Ich folge dir.",
            "Ich bin noch hinter dir.",
            "Ich bleibe an deiner Seite."
    };
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
        Move activeMove = Move.STOP;
        long lastGoToAtMs = 0L;
        int misses = 0;
        boolean lostTarget = false;
        long lastConfirmAtMs = System.currentTimeMillis();
        int confirmIndex = 0;

        try {
            Frame robotFrame = context.getActuation().robotFrame();

            holder = HolderBuilder.with(context)
                    .withAutonomousAbilities(
                            AutonomousAbilitiesType.BASIC_AWARENESS,
                            AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                    .build();
            holder.hold();

            listenFuture = startStopListener(context);

            FreeFrame trackFrame = context.getMapping().makeFreeFrame();

            while (gen == generation && sessionRunning && wantFollow
                    && !Thread.currentThread().isInterrupted()) {

                Human human = selectHuman(context, robotFrame, trackValid ? trackFrame : null);
                if (human == null) {
                    if (++misses >= MAX_MISSES) {
                        lostTarget = true;
                        break;
                    }
                    if (trackValid) {
                        long missNow = System.currentTimeMillis();
                        boolean finished = goToFuture == null || goToFuture.isDone();
                        if (finished && missNow - lastGoToAtMs > MIN_GOTO_INTERVAL_MS) {
                            requestCancel(goToFuture);
                            goToFuture = GoToBuilder.with(context)
                                    .withFrame(trackFrame.frame())
                                    .withFinalOrientationPolicy(OrientationPolicy.ALIGN_X)
                                    .build()
                                    .async().run();
                            lastGoToAtMs = missNow;
                            activeMove = Move.DRIVE;
                            activeTarget = null;
                        }
                    }
                    Thread.sleep(LOOP_PAUSE_MS);
                    continue;
                }
                misses = 0;

                long confirmNow = System.currentTimeMillis();
                if (confirmNow - lastConfirmAtMs > FOLLOW_CONFIRM_INTERVAL_MS) {
                    lastConfirmAtMs = confirmNow;
                    sayQuietly(context,
                            FOLLOW_CONFIRMATIONS[confirmIndex % FOLLOW_CONFIRMATIONS.length]);
                    confirmIndex++;
                }

                Frame headFrame = human.getHeadFrame();
                if (headFrame == null) {
                    misses++;
                    Thread.sleep(LOOP_PAUSE_MS);
                    continue;
                }
                Transform t = headFrame.computeTransform(robotFrame).getTransform();
                double x = t.getTranslation().getX();
                double y = t.getTranslation().getY();
                double distance = Math.hypot(x, y);

                trackFrame.update(robotFrame, t, 0L);
                trackValid = true;
                if (lookAtFuture == null || lookAtFuture.isDone()) {
                    lookAtFuture = startHeadTracking(context, trackFrame);
                }

                double bearing = Math.atan2(y, x);
                Move desired;
                Transform goalTf;
                if (distance <= STAND_OFF_M + DEAD_ZONE_M) {
                    desired = Move.STOP;
                    goalTf = null;
                } else if (Math.abs(bearing) > TURN_THRESHOLD_RAD) {
                    desired = Move.ROTATE;
                    goalTf = TransformBuilder.create().from2DTransform(0.0, 0.0, bearing);
                } else {
                    desired = Move.DRIVE;
                    double scale = (distance - STAND_OFF_M) / distance;
                    goalTf = TransformBuilder.create()
                            .from2DTransform(x * scale, y * scale, bearing);
                }

                if (desired == Move.STOP) {
                    requestCancel(goToFuture);
                    goToFuture = null;
                    activeTarget = null;
                    activeMove = Move.STOP;
                    Thread.sleep(LOOP_PAUSE_MS);
                    continue;
                }

                FreeFrame candidate = context.getMapping().makeFreeFrame();
                candidate.update(robotFrame, goalTf, 0L);

                long now = System.currentTimeMillis();
                boolean modeChanged = desired != activeMove;
                boolean finished = goToFuture == null || goToFuture.isDone();
                boolean movedFar = targetMoved(candidate, activeTarget) > RETARGET_M;
                boolean throttleOk = now - lastGoToAtMs > MIN_GOTO_INTERVAL_MS;

                if (modeChanged || finished || (movedFar && throttleOk)) {
                    requestCancel(goToFuture);
                    goToFuture = GoToBuilder.with(context)
                            .withFrame(candidate.frame())
                            .withFinalOrientationPolicy(OrientationPolicy.ALIGN_X)
                            .build()
                            .async().run();
                    activeTarget = candidate;
                    activeMove = desired;
                    lastGoToAtMs = now;
                }

                Thread.sleep(LOOP_PAUSE_MS);
            }

            if (lostTarget && gen == generation && wantFollow) {
                sayQuietly(context,
                        "Ich habe dich leider aus den Augen verloren und bleibe jetzt hier stehen.");
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
            synchronized (this) {
                if (gen == generation) {
                    sessionRunning = false;
                    loopThread = null;
                    setWantFollow(false);
                }
            }
        }
    }

    private Future<Void> startHeadTracking(QiContext context, FreeFrame target) {
        LookAt lookAt = LookAtBuilder.with(context)
                .withFrame(target.frame())
                .build();
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

    private enum Move {STOP, ROTATE, DRIVE}

    public interface FollowStateListener {
        void onFollowStateChanged(boolean following);
    }
}
