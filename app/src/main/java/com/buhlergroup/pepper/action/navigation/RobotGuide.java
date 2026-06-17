package com.buhlergroup.pepper.action.navigation;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;
import com.buhlergroup.pepper.debug.DebugLog;

final class RobotGuide {

    private static final String TAG = "Navigation";

    interface Condition {
        boolean shouldContinue();
    }

    private final NavigationManager nav;
    private volatile Future<Void> activeGoTo;
    private volatile boolean stopGuideRequested;

    RobotGuide(NavigationManager nav) {
        this.nav = nav;
    }

    void goToWaypoint(WaypointEntity wp, NavigationManager.Callback<Void> cb) {
        nav.submit(() -> {
            QiContext c = nav.qiContext();
            if (c == null || !nav.isLocalized()) {
                cb.onError("Pepper ist noch nicht lokalisiert. Aktiviere zuerst einen Raum-Scan.");
                return;
            }
            try {
                driveTo(c, wp);
                cb.onResult(null);
            } catch (Exception e) {
                Log.w(TAG, "goToWaypoint failed: " + e.getMessage());
                cb.onError("Pepper konnte nicht hinfahren.");
            }
        });
    }

    void guideToWaypoint(WaypointEntity wp, NavigationManager.Callback<NavigationManager.GuideOutcome> cb) {
        nav.submit(() -> {
            QiContext c = nav.qiContext();
            if (c == null || !nav.isLocalized()) {
                cb.onError("Pepper ist noch nicht lokalisiert.");
                return;
            }
            stopGuideRequested = false;
            Future<ListenResult> stopListener = startGuideStopListener(c);
            try {
                driveTo(c, wp, () -> nav.isLocalized() && nav.qiContext() != null && !stopGuideRequested);
                NavigationManager.GuideOutcome outcome;
                if (stopGuideRequested) {
                    outcome = NavigationManager.GuideOutcome.STOPPED;
                } else if (nav.isLocalized() && nav.qiContext() != null) {
                    outcome = NavigationManager.GuideOutcome.ARRIVED;
                } else {
                    outcome = NavigationManager.GuideOutcome.LOST;
                }
                cb.onResult(outcome);
            } catch (Exception e) {
                Log.w(TAG, "guideToWaypoint failed: " + e.getMessage());
                cb.onError("Pepper konnte nicht hinfahren.");
            } finally {
                if (stopListener != null && !stopListener.isDone()) {
                    stopListener.requestCancellation();
                }
            }
        });
    }

    boolean hasFotostand(QiContext context) {
        if (context == null || !nav.isLocalized()) {
            return false;
        }
        RoomScanEntity scan = nav.getActiveScan();
        if (scan == null) {
            return false;
        }
        try {
            return nav.dao(context).getWaypointByType(scan.id, WaypointEntity.TYPE_FOTOSTAND) != null;
        } catch (Exception e) {
            return false;
        }
    }

    boolean driveToFotostandIfPossible(QiContext context) {
        if (context == null || !nav.isLocalized()) {
            return false;
        }
        RoomScanEntity scan = nav.getActiveScan();
        if (scan == null) {
            return false;
        }
        try {
            WaypointEntity wp = nav.dao(context).getWaypointByType(scan.id, WaypointEntity.TYPE_FOTOSTAND);
            if (wp == null) {
                return false;
            }
            driveTo(context, wp);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "driveToFotostand failed: " + e.getMessage());
            return false;
        }
    }

    void cancelActiveGoTo() {
        Future<Void> f = activeGoTo;
        activeGoTo = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private Future<ListenResult> startGuideStopListener(QiContext c) {
        try {
            PhraseSet phrases = PhraseSetBuilder.with(c)
                    .withTexts("stopp", "stop", "halt", "halt an", "anhalten",
                            "bleib stehen", "bleib hier")
                    .build();
            Listen listen = ListenBuilder.with(c).withPhraseSet(phrases).build();
            Future<ListenResult> future = listen.async().run();
            future.thenConsume(f -> {
                if (f.isCancelled() || f.hasError()) {
                    return;
                }
                ListenResult result = f.get();
                if (result != null && result.getHeardPhrase() != null
                        && !result.getHeardPhrase().getText().isEmpty()) {
                    stopGuideRequested = true;
                    cancelActiveGoTo();
                }
            });
            return future;
        } catch (Exception e) {
            Log.w(TAG, "startGuideStopListener failed: " + e.getMessage());
            return null;
        }
    }

    private void driveTo(QiContext c, WaypointEntity wp) {
        driveTo(c, wp, () -> nav.isLocalized() && nav.qiContext() != null);
    }

    private void driveTo(QiContext c, WaypointEntity wp, Condition condition) {
        DebugLog.get().setStatus("Fahre zu Wegpunkt: " + wp.name);
        DebugLog.get().i(TAG, "Fahre zu Wegpunkt: " + wp.name);
        Mapping mapping = c.getMapping();
        Frame mapFrame = mapping.mapFrame();
        Transform t = TransformBuilder.create().from2DTransform(wp.x, wp.y, wp.theta);
        FreeFrame target = mapping.makeFreeFrame();
        target.update(mapFrame, t, 0L);
        Future<Void> future = GoToBuilder.with(c).withFrame(target.frame()).build().async().run();
        activeGoTo = future;
        try {
            awaitGoTo(future, condition);
        } finally {
            if (activeGoTo == future) {
                activeGoTo = null;
            }
        }
    }

    private boolean awaitGoTo(Future<Void> future, Condition condition) {
        while (!future.isDone()) {
            if (!condition.shouldContinue()) {
                future.requestCancellation();
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.requestCancellation();
                break;
            }
        }
        if (!future.isDone() || future.isCancelled()) {
            return false;
        }
        if (future.hasError()) {
            DebugLog.get().w(TAG, "GoTo fehlgeschlagen: " + future.getError().getMessage());
            return false;
        }
        return true;
    }
}
