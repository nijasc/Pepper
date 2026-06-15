package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.nio.ByteBuffer;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ExplorationMapBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeAndMapBuilder;
import com.aldebaran.qi.sdk.builder.LocalizeBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizationStatus;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.geometry.Quaternion;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.buhlergroup.pepper.action.navigation.data.NavigationDatabase;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;
import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NavigationManager {

    private static final String TAG = "Navigation";
    private static final long MAP_POLL_BASE_MS = 1000;
    private static final long MAP_POLL_MAX_MS = 8000;
    private static final double SCAN_RADIUS_M = 1.0;
    private static final int SCAN_ROTATION_STEPS = 4;

    public interface Callback<T> {
        void onResult(T value);

        void onError(String error);
    }

    public interface MapUpdateListener {
        void onMapBitmap(Bitmap bitmap);
    }

    private static final NavigationManager INSTANCE = new NavigationManager();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile QiContext qiContext;
    private volatile Holder holder;
    private volatile Future<Void> mappingFuture;
    private volatile LocalizeAndMap currentMapping;
    private volatile Future<Void> localizeFuture;
    private volatile ExplorationMap activeMap;
    private volatile RoomScanEntity activeScan;
    private volatile boolean localized;
    private volatile boolean scanning;
    private volatile MapUpdateListener mapUpdateListener;
    private volatile int pollGeneration;
    private volatile Future<Void> rotateFuture;
    private volatile Future<Void> activeGoTo;
    private volatile FreeFrame scanOrigin;

    private interface Condition {
        boolean shouldContinue();
    }

    private NavigationManager() {
    }

    public static NavigationManager get() {
        return INSTANCE;
    }

    public void setQiContext(QiContext context) {
        this.qiContext = context;
    }

    public void onFocusLost() {
        scanning = false;
        stopMapPolling();
        cancelRotation();
        cancelActiveGoTo();
        cancelLocalize();
        cancelMapping();
        releaseAbilities();
        localized = false;
        qiContext = null;
    }

    public void setMapUpdateListener(MapUpdateListener listener) {
        this.mapUpdateListener = listener;
    }

    public boolean isLocalized() {
        return localized;
    }

    public boolean isScanning() {
        return scanning;
    }

    public RoomScanEntity getActiveScan() {
        return activeScan;
    }

    public void startScan(Callback<Void> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                holdAbilities(c);
                LocalizeAndMap lam = LocalizeAndMapBuilder.with(c).build();
                currentMapping = lam;
                mappingFuture = lam.async().run();
                scanning = true;
                captureScanOrigin(c);
                cb.onResult(null);
                startMapPolling();
                autoScanRotate(c);
            } catch (Exception e) {
                releaseAbilities();
                Log.w(TAG, "startScan failed: " + e.getMessage());
                cb.onError("Scan konnte nicht gestartet werden.");
            }
        });
    }

    public void stopAndSaveScan(String name, Callback<RoomScanEntity> cb) {
        scanning = false;
        cancelRotation();
        executor.execute(() -> {
            QiContext c = qiContext;
            LocalizeAndMap lam = currentMapping;
            if (c == null || lam == null) {
                cb.onError("Es läuft gerade kein Scan.");
                return;
            }
            try {
                ExplorationMap map = lam.dumpMap();
                String data = map.serialize();
                String id = UUID.randomUUID().toString();
                File file = new File(mapDir(c), id + ".map");
                writeFile(file, data);

                RoomScanEntity scan = new RoomScanEntity(
                        id, name, System.currentTimeMillis(), file.getAbsolutePath());
                dao(c).insertScan(scan);

                activeMap = map;
                activeScan = scan;
                cb.onResult(scan);
                publishMap(map);
            } catch (Exception e) {
                Log.w(TAG, "stopAndSaveScan failed: " + e.getMessage());
                cb.onError("Scan konnte nicht gespeichert werden.");
            } finally {
                stopMapPolling();
                cancelMapping();
                releaseAbilities();
                scanning = false;
            }
        });
    }

    public void listScans(Callback<List<RoomScanEntity>> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                cb.onResult(dao(c).getScans());
            } catch (Exception e) {
                cb.onError("Scans konnten nicht geladen werden.");
            }
        });
    }

    public void deleteScan(RoomScanEntity scan, Callback<Void> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                dao(c).deleteWaypointsForScan(scan.id);
                dao(c).deleteScan(scan.id);
                deleteFileQuietly(scan.mapPath);
                if (activeScan != null && activeScan.id.equals(scan.id)) {
                    activeScan = null;
                    activeMap = null;
                    localized = false;
                }
                cb.onResult(null);
            } catch (Exception e) {
                cb.onError("Scan konnte nicht gelöscht werden.");
            }
        });
    }

    public void localize(RoomScanEntity scan, Callback<Boolean> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                String data = readFile(scan.mapPath);
                if (data == null) {
                    cb.onError("Karte konnte nicht geladen werden.");
                    return;
                }
                ExplorationMap map = ExplorationMapBuilder.with(c).withMapString(data).build();

                cancelLocalize();
                holdAbilities(c);
                localized = false;
                activeMap = map;
                activeScan = scan;

                Localize localize = LocalizeBuilder.with(c).withMap(map).build();
                AtomicBoolean done = new AtomicBoolean(false);
                localize.addOnStatusChangedListener(status -> {
                    if (status == LocalizationStatus.LOCALIZED) {
                        boolean recovered = done.get() && !localized;
                        localized = true;
                        if (done.compareAndSet(false, true)) {
                            cb.onResult(true);
                        } else if (recovered) {
                            announceLocalization(c, true);
                        }
                    } else if (done.get() && localized) {
                        handleLocalizationLost(c);
                    }
                });
                localizeFuture = localize.async().run();
                localizeFuture.thenConsume(f -> {
                    if (f.hasError() || f.isCancelled()) {
                        if (localized && done.get()) {
                            handleLocalizationLost(c);
                        }
                        localized = false;
                    }
                });
            } catch (Exception e) {
                releaseAbilities();
                Log.w(TAG, "localize failed: " + e.getMessage());
                cb.onError("Lokalisierung fehlgeschlagen.");
            }
        });
    }

    public void saveWaypoint(String name, String type, Callback<WaypointEntity> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            RoomScanEntity scan = activeScan;
            if (c == null || scan == null || !localized) {
                cb.onError("Pepper ist noch nicht lokalisiert.");
                return;
            }
            try {
                Transform t = robotInMap(c);
                double[] pose = pose2d(t);
                WaypointEntity wp = new WaypointEntity(
                        scan.id, name, type, pose[0], pose[1], pose[2], System.currentTimeMillis());
                long id = dao(c).insertWaypoint(wp);
                wp.id = id;
                cb.onResult(wp);
            } catch (Exception e) {
                Log.w(TAG, "saveWaypoint failed: " + e.getMessage());
                cb.onError("Wegpunkt konnte nicht gespeichert werden.");
            }
        });
    }

    public void listWaypoints(Callback<List<WaypointEntity>> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            RoomScanEntity scan = activeScan;
            if (c == null || scan == null) {
                cb.onError("Kein aktiver Raum-Scan.");
                return;
            }
            try {
                cb.onResult(dao(c).getWaypoints(scan.id));
            } catch (Exception e) {
                cb.onError("Wegpunkte konnten nicht geladen werden.");
            }
        });
    }

    public void deleteWaypoint(WaypointEntity wp, Callback<Void> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null) {
                cb.onError("Roboter ist nicht bereit.");
                return;
            }
            try {
                dao(c).deleteWaypoint(wp.id);
                cb.onResult(null);
            } catch (Exception e) {
                cb.onError("Wegpunkt konnte nicht gelöscht werden.");
            }
        });
    }

    public void goToWaypoint(WaypointEntity wp, Callback<Void> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null || !localized) {
                cb.onError("Pepper ist noch nicht lokalisiert.");
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

    public boolean hasFotostand(QiContext context) {
        if (context == null || !localized) {
            return false;
        }
        RoomScanEntity scan = activeScan;
        if (scan == null) {
            return false;
        }
        try {
            return dao(context).getWaypointByType(scan.id, WaypointEntity.TYPE_FOTOSTAND) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean driveToFotostandIfPossible(QiContext context) {
        if (context == null || !localized) {
            return false;
        }
        RoomScanEntity scan = activeScan;
        if (scan == null) {
            return false;
        }
        try {
            WaypointEntity wp = dao(context).getWaypointByType(scan.id, WaypointEntity.TYPE_FOTOSTAND);
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

    public void getRobotPose(Callback<double[]> cb) {
        executor.execute(() -> {
            QiContext c = qiContext;
            if (c == null || !localized) {
                cb.onError("Pepper ist noch nicht lokalisiert.");
                return;
            }
            try {
                cb.onResult(pose2d(robotInMap(c)));
            } catch (Exception e) {
                Log.w(TAG, "getRobotPose failed: " + e.getMessage());
                cb.onError("Position konnte nicht ermittelt werden.");
            }
        });
    }

    public void getMapBitmap(Callback<Bitmap> cb) {
        executor.execute(() -> {
            ExplorationMap map = activeMap;
            if (map == null) {
                cb.onError("Noch keine Karte vorhanden. Erst scannen oder aktivieren.");
                return;
            }
            Bitmap bitmap = renderMap(map);
            if (bitmap == null) {
                cb.onError("Karte konnte nicht erzeugt werden.");
                return;
            }
            cb.onResult(bitmap);
        });
    }

    private void handleLocalizationLost(QiContext c) {
        localized = false;
        cancelActiveGoTo();
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

    private Bitmap renderMap(ExplorationMap map) {
        try {
            ByteBuffer buffer = map.getTopGraphicalRepresentation().getImage().getData();
            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.w(TAG, "renderMap failed: " + e.getMessage());
            return null;
        }
    }

    private void publishMap(ExplorationMap map) {
        MapUpdateListener listener = mapUpdateListener;
        if (listener == null || map == null) {
            return;
        }
        Bitmap bitmap = renderMap(map);
        if (bitmap != null) {
            listener.onMapBitmap(bitmap);
        }
    }

    private void startMapPolling() {
        final int gen = ++pollGeneration;
        Thread poller = new Thread(() -> {
            int failures = 0;
            while (gen == pollGeneration && scanning) {
                boolean ok = false;
                try {
                    LocalizeAndMap lam = currentMapping;
                    if (lam == null) {
                        break;
                    }
                    publishMap(lam.dumpMap());
                    ok = true;
                } catch (Exception e) {
                    Log.d(TAG, "Map snapshot not available yet: " + e.getMessage());
                }
                if (ok) {
                    failures = 0;
                } else if (failures < 3) {
                    failures++;
                }
                long sleepMs = ok
                        ? MAP_POLL_BASE_MS
                        : Math.min(MAP_POLL_MAX_MS, MAP_POLL_BASE_MS * (1L << failures));
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "map-poller");
        poller.setDaemon(true);
        poller.start();
    }

    private void stopMapPolling() {
        pollGeneration++;
    }

    private void captureScanOrigin(QiContext c) {
        try {
            Frame robotFrame = c.getActuation().robotFrame();
            FreeFrame origin = c.getMapping().makeFreeFrame();
            origin.update(robotFrame, TransformBuilder.create().from2DTransform(0.0, 0.0, 0.0), 0L);
            scanOrigin = origin;
        } catch (Exception e) {
            Log.w(TAG, "captureScanOrigin failed: " + e.getMessage());
            scanOrigin = null;
        }
    }

    private void autoScanRotate(QiContext c) {
        Thread explorer = new Thread(() -> {
            try {
                rotationSweep(c);
                double[][] offsets = {
                        {SCAN_RADIUS_M, 0.0},
                        {0.0, SCAN_RADIUS_M},
                        {0.0, -SCAN_RADIUS_M}
                };
                for (double[] offset : offsets) {
                    if (!scanning) {
                        break;
                    }
                    if (driveToOriginOffset(c, offset[0], offset[1])) {
                        rotationSweep(c);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "autoScanExplore failed: " + e.getMessage());
            } finally {
                rotateFuture = null;
            }
        }, "scan-explorer");
        explorer.setDaemon(true);
        explorer.start();
    }

    private void rotationSweep(QiContext c) {
        double angle = 2.0 * Math.PI / SCAN_ROTATION_STEPS;
        for (int i = 0; i < SCAN_ROTATION_STEPS && scanning; i++) {
            try {
                Frame robotFrame = c.getActuation().robotFrame();
                Transform t = TransformBuilder.create().from2DTransform(0.0, 0.0, angle);
                FreeFrame target = c.getMapping().makeFreeFrame();
                target.update(robotFrame, t, 0L);
                Future<Void> goToFuture = GoToBuilder.with(c)
                        .withFrame(target.frame()).build().async().run();
                rotateFuture = goToFuture;
                awaitGoTo(goToFuture, () -> scanning);
            } catch (Exception e) {
                Log.w(TAG, "rotationSweep step failed: " + e.getMessage());
                break;
            }
        }
    }

    private boolean driveToOriginOffset(QiContext c, double dx, double dy) {
        FreeFrame origin = scanOrigin;
        if (origin == null) {
            return false;
        }
        double clampedX = clampRadius(dx);
        double clampedY = clampRadius(dy);
        try {
            Transform t = TransformBuilder.create().from2DTransform(clampedX, clampedY, 0.0);
            FreeFrame target = c.getMapping().makeFreeFrame();
            target.update(origin.frame(), t, 0L);
            Future<Void> goToFuture = GoToBuilder.with(c)
                    .withFrame(target.frame()).build().async().run();
            rotateFuture = goToFuture;
            awaitGoTo(goToFuture, () -> scanning);
            return scanning;
        } catch (Exception e) {
            Log.w(TAG, "driveToOriginOffset failed: " + e.getMessage());
            return false;
        }
    }

    private double clampRadius(double value) {
        return Math.max(-SCAN_RADIUS_M, Math.min(SCAN_RADIUS_M, value));
    }

    private void cancelRotation() {
        Future<Void> f = rotateFuture;
        rotateFuture = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private void cancelActiveGoTo() {
        Future<Void> f = activeGoTo;
        activeGoTo = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private void awaitGoTo(Future<Void> future, Condition condition) {
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
    }

    private void driveTo(QiContext c, WaypointEntity wp) {
        Mapping mapping = c.getMapping();
        Frame mapFrame = mapping.mapFrame();
        Transform t = TransformBuilder.create().from2DTransform(wp.x, wp.y, wp.theta);
        FreeFrame target = mapping.makeFreeFrame();
        target.update(mapFrame, t, 0L);
        Future<Void> future = GoToBuilder.with(c).withFrame(target.frame()).build().async().run();
        activeGoTo = future;
        try {
            awaitGoTo(future, () -> localized && qiContext != null);
        } finally {
            if (activeGoTo == future) {
                activeGoTo = null;
            }
        }
    }

    private Transform robotInMap(QiContext c) {
        Frame robotFrame = c.getActuation().robotFrame();
        Frame mapFrame = c.getMapping().mapFrame();
        return robotFrame.computeTransform(mapFrame).getTransform();
    }

    private double[] pose2d(Transform t) {
        double x = t.getTranslation().getX();
        double y = t.getTranslation().getY();
        Quaternion q = t.getRotation();
        double theta = Math.atan2(
                2.0 * (q.getW() * q.getZ() + q.getX() * q.getY()),
                1.0 - 2.0 * (q.getY() * q.getY() + q.getZ() * q.getZ()));
        return new double[]{x, y, theta};
    }

    private void holdAbilities(QiContext c) {
        releaseAbilities();
        try {
            Holder h = HolderBuilder.with(c)
                    .withAutonomousAbilities(
                            AutonomousAbilitiesType.BASIC_AWARENESS,
                            AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                    .build();
            h.hold();
            holder = h;
        } catch (Exception e) {
            Log.w(TAG, "holdAbilities failed: " + e.getMessage());
        }
    }

    private void releaseAbilities() {
        Holder h = holder;
        holder = null;
        if (h != null) {
            try {
                h.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void cancelMapping() {
        Future<Void> f = mappingFuture;
        mappingFuture = null;
        currentMapping = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private void cancelLocalize() {
        Future<Void> f = localizeFuture;
        localizeFuture = null;
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }

    private File mapDir(Context context) {
        File dir = new File(context.getFilesDir(), "maps");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void writeFile(File file, String content) throws Exception {
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = 0;
            while (read < bytes.length) {
                int r = in.read(bytes, read, bytes.length - read);
                if (r < 0) {
                    break;
                }
                read += r;
            }
            return new String(bytes, 0, read, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.w(TAG, "readFile failed: " + e.getMessage());
            return null;
        }
    }

    private void deleteFileQuietly(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private com.buhlergroup.pepper.action.navigation.data.NavigationDao dao(Context context) {
        return NavigationDatabase.get(context).navigationDao();
    }
}
