package com.buhlergroup.pepper.action.navigation;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.util.FutureUtils;

import java.util.concurrent.atomic.AtomicBoolean;

final class RobotLocalizer {

    private static final String TAG = "Navigation";
    private static final int ROTATION_STEPS = 8;

    private final NavigationManager nav;
    private volatile Future<Void> localizeFuture;
    private volatile Localize currentLocalize;

    RobotLocalizer(NavigationManager nav) {
        this.nav = nav;
    }

    void localize(RoomScanEntity scan, NavigationManager.Callback<Boolean> cb) {
        nav.submit(() -> {
            QiContext c = nav.qiContext();
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                String data = MapFileStore.readFile(scan.mapPath);
                if (data == null) {
                    cb.onError("Karte konnte nicht geladen werden.");
                    return;
                }
                ExplorationMap map = ExplorationMapBuilder.with(c).withMapString(data).build();

                cancelLocalize();
                nav.holdAbilities(c);
                nav.setLocalized(false);
                nav.setActiveMap(map);
                nav.setActiveScan(scan);
                DebugLog.get().setStatus("Lokalisiere in „" + scan.name + "“ …");
                DebugLog.get().i(TAG, "Lokalisierung gestartet: " + scan.name);

                Localize localize = LocalizeBuilder.with(c).withMap(map).build();
                currentLocalize = localize;
                AtomicBoolean done = new AtomicBoolean(false);
                localize.addOnStatusChangedListener(status -> {
                    if (nav.qiContext() == null) {
                        return;
                    }
                    if (status == LocalizationStatus.LOCALIZED) {
                        boolean recovered = done.get() && !nav.isLocalized();
                        nav.setLocalized(true);
                        DebugLog.get().setStatus("Lokalisiert in „" + scan.name + "“");
                        if (done.compareAndSet(false, true)) {
                            cb.onResult(true);
                        } else if (recovered) {
                            announceLocalization(c, true);
                        }
                    } else if (done.get() && nav.isLocalized()) {
                        handleLocalizationLost(c);
                    }
                });
                localizeFuture = localize.async().run();
                localizeFuture.thenConsume(f -> {
                    if (f.hasError() || f.isCancelled()) {
                        if (nav.isLocalized() && done.get()) {
                            handleLocalizationLost(c);
                        }
                        nav.setLocalized(false);
                    }
                });
                scheduleLocalizeTimeout(done, cb, NavigationSettings.getLocalizeTimeoutMs(c));
                startLocalizeRotation(c, done);
            } catch (Exception e) {
                nav.releaseAbilities();
                Log.w(TAG, "localize failed: " + e.getMessage());
                cb.onError("Lokalisierung fehlgeschlagen.");
            }
        });
    }

    private void startLocalizeRotation(QiContext c, AtomicBoolean done) {
        Thread t = new Thread(() -> {
            if (nav.qiContext() != null && !done.get()) {
                nav.rotateFullCircle(c, ROTATION_STEPS, null);
            }
        }, "localize-rotate");
        t.setDaemon(true);
        t.start();
    }

    void cancelLocalize() {
        Localize l = currentLocalize;
        currentLocalize = null;
        if (l != null) {
            try {
                l.removeAllOnStatusChangedListeners();
            } catch (Exception ignored) {
            }
        }
        Future<Void> f = localizeFuture;
        localizeFuture = null;
        FutureUtils.cancel(f);
    }

    private void scheduleLocalizeTimeout(AtomicBoolean done, NavigationManager.Callback<Boolean> cb,
                                         long timeoutMs) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(timeoutMs);
            } catch (InterruptedException e) {
                return;
            }
            if (done.compareAndSet(false, true)) {
                cancelLocalize();
                nav.releaseAbilities();
                nav.setLocalized(false);
                cb.onError("Pepper konnte sich nicht orientieren. Bitte näher an einen "
                        + "bekannten Bereich stellen und erneut versuchen.");
            }
        }, "localize-timeout");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private void handleLocalizationLost(QiContext c) {
        nav.setLocalized(false);
        nav.cancelActiveGoTo();
        DebugLog.get().setStatus("Orientierung verloren");
        DebugLog.get().w(TAG, "Lokalisierung verloren");
        announceLocalization(c, false);
    }

    private void announceLocalization(QiContext c, boolean recovered) {
        if (c == null) {
            return;
        }
        final String text = recovered
                ? "Ich habe mich wieder orientiert."
                : "Ich habe meine Orientierung verloren. Ich halte an und versuche, "
                + "mich neu zu orientieren.";
        Thread announcer = new Thread(() -> {
            try {
                SpeechManager.getInstance().say(c, text);
            } catch (Exception ignored) {
            }
        }, "nav-announce");
        announcer.setDaemon(true);
        announcer.start();
    }
}
