package com.buhlergroup.pepper.action.navigation;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.concurrent.atomic.AtomicBoolean;

final class RobotLocalizer {

    private static final String TAG = "Navigation";
    private static final long LOCALIZE_TIMEOUT_MS = 40000;

    private final NavigationManager nav;
    private volatile Future<Void> localizeFuture;

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
                String data = nav.readFile(scan.mapPath);
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
                AtomicBoolean done = new AtomicBoolean(false);
                localize.addOnStatusChangedListener(status -> {
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
                scheduleLocalizeTimeout(done, cb);
            } catch (Exception e) {
                nav.releaseAbilities();
                Log.w(TAG, "localize failed: " + e.getMessage());
                cb.onError("Lokalisierung fehlgeschlagen.");
            }
        });
    }

    void cancelLocalize() {
        Future<Void> f = localizeFuture;
        localizeFuture = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private void scheduleLocalizeTimeout(AtomicBoolean done, NavigationManager.Callback<Boolean> cb) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(LOCALIZE_TIMEOUT_MS);
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
