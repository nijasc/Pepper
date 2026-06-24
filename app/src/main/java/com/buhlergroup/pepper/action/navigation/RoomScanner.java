package com.buhlergroup.pepper.action.navigation;

import android.graphics.Bitmap;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.debug.DebugLog;

import java.io.File;
import java.util.UUID;

final class RoomScanner {

    private static final String TAG = "Navigation";
    private static final long SNAPSHOT_INTERVAL_MS = 1500;
    private static final int ROTATION_STEPS = 4;

    private final NavigationManager nav;

    private volatile LocalizeAndMap currentMapping;
    private volatile Future<Void> mappingFuture;
    private volatile boolean scanning;
    private volatile Thread rotationThread;
    private volatile Future<ListenResult> scanStopListenFuture;
    private volatile Runnable scanStopCallback;
    private volatile NavigationManager.MapUpdateListener mapUpdateListener;

    RoomScanner(NavigationManager nav) {
        this.nav = nav;
    }

    boolean isScanning() {
        return scanning;
    }

    void setMapUpdateListener(NavigationManager.MapUpdateListener listener) {
        this.mapUpdateListener = listener;
    }

    void setScanStopCallback(Runnable callback) {
        this.scanStopCallback = callback;
    }

    void startScan(NavigationManager.Callback<Void> cb) {
        nav.submit(() -> {
            QiContext c = nav.qiContext();
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                nav.holdAbilities(c);
                LocalizeAndMap lam = LocalizeAndMapBuilder.with(c).build();
                currentMapping = lam;
                mappingFuture = lam.async().run();
                scanning = true;
                DebugLog.get().setStatus("Raum-Scan – Pepper erfasst die erste Position, bitte Platz lassen");
                DebugLog.get().i(TAG, "Raum-Scan gestartet (automatische 360°-Erfassung)");
                cb.onResult(null);
                startSnapshotLoop();
                startScanStopListener(c);
                captureRotation();
            } catch (Exception e) {
                nav.releaseAbilities();
                Log.w(TAG, "startScan failed: " + e.getMessage());
                DebugLog.get().e(TAG, "Raum-Scan-Start fehlgeschlagen", e);
                cb.onError("Scan konnte nicht gestartet werden.");
            }
        });
    }

    void stopAndSaveScan(String name, NavigationManager.Callback<RoomScanEntity> cb) {
        scanning = false;
        cancelScanStopListener();
        nav.cancelActiveGoTo();
        nav.submit(() -> {
            QiContext c = nav.qiContext();
            LocalizeAndMap lam = currentMapping;
            if (c == null || lam == null) {
                try {
                    cb.onError("Es läuft gerade kein Scan.");
                } finally {
                    cancelMapping();
                    nav.releaseAbilities();
                    scanning = false;
                }
                return;
            }
            try {
                ExplorationMap map = lam.dumpMap();
                String data = map.serialize();
                String id = UUID.randomUUID().toString();
                File file = new File(nav.mapDir(c), id + ".map");
                nav.writeFile(file, data);

                RoomScanEntity scan = new RoomScanEntity(
                        id, name, System.currentTimeMillis(), file.getAbsolutePath());
                nav.dao(c).insertScan(scan);

                nav.setActiveMap(map);
                nav.setActiveScan(scan);
                DebugLog.get().setStatus("Raum-Scan gespeichert: " + name);
                DebugLog.get().i(TAG, "Raum-Scan gespeichert: " + name);
                cb.onResult(scan);
                publishMap(map);
            } catch (Exception e) {
                Log.w(TAG, "stopAndSaveScan failed: " + e.getMessage());
                DebugLog.get().e(TAG, "Raum-Scan speichern fehlgeschlagen", e);
                cb.onError("Scan konnte nicht gespeichert werden.");
            } finally {
                cancelMapping();
                nav.releaseAbilities();
                scanning = false;
            }
        });
    }

    void captureSnapshot() {
        nav.submit(this::publishScanSnapshot);
    }

    void captureRotation() {
        Thread running = rotationThread;
        if (running != null && running.isAlive()) {
            return;
        }
        Thread t = new Thread(() -> {
            QiContext c = nav.qiContext();
            if (c == null) {
                publishScanSnapshot();
                return;
            }
            DebugLog.get().i(TAG, "360°-Erfassung gestartet");
            nav.rotateFullCircle(c, ROTATION_STEPS, this::publishScanSnapshot, () -> scanning);
            publishScanSnapshot();
            DebugLog.get().i(TAG, "360°-Erfassung abgeschlossen");
        }, "scan-rotate");
        t.setDaemon(true);
        rotationThread = t;
        t.start();
    }

    void publishMap(ExplorationMap map) {
        NavigationManager.MapUpdateListener listener = mapUpdateListener;
        if (listener == null || map == null) {
            return;
        }
        Bitmap bitmap = NavMapRenderer.render(map);
        if (bitmap != null) {
            listener.onMapBitmap(bitmap);
        }
    }

    private synchronized void publishScanSnapshot() {
        if (!scanning) {
            return;
        }
        LocalizeAndMap lam = currentMapping;
        if (lam == null) {
            return;
        }
        try {
            publishMap(lam.dumpMap());
        } catch (Exception e) {
            Log.d(TAG, "Map snapshot not available yet: " + e.getMessage());
        }
    }

    private void startSnapshotLoop() {
        Thread t = new Thread(() -> {
            while (scanning) {
                try {
                    Thread.sleep(SNAPSHOT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                publishScanSnapshot();
            }
        }, "scan-snapshot");
        t.setDaemon(true);
        t.start();
    }

    private void startScanStopListener(QiContext c) {
        try {
            PhraseSet phrases = PhraseSetBuilder.with(c)
                    .withTexts("stopp", "stop", "halt", "halt an", "anhalten",
                            "fertig", "stopp scan", "scan stoppen", "stopp den scan")
                    .build();
            Listen listen = ListenBuilder.with(c).withPhraseSet(phrases).build();
            Future<ListenResult> future = listen.async().run();
            scanStopListenFuture = future;
            future.thenConsume(f -> {
                if (f.isCancelled() || f.hasError()) {
                    return;
                }
                ListenResult result = f.get();
                if (result != null && result.getHeardPhrase() != null
                        && !result.getHeardPhrase().getText().isEmpty() && scanning) {
                    DebugLog.get().i(TAG, "Scan-Stop per Sprache erkannt");
                    Runnable cb = scanStopCallback;
                    if (cb != null) {
                        cb.run();
                    }
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "startScanStopListener failed: " + e.getMessage());
        }
    }

    void cancelScanStopListener() {
        Future<ListenResult> f = scanStopListenFuture;
        scanStopListenFuture = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    void cancelMapping() {
        Future<Void> f = mappingFuture;
        mappingFuture = null;
        currentMapping = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    void stopScanning() {
        scanning = false;
    }
}
