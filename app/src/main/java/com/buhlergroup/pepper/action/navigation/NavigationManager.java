package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Frame;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NavigationManager {

    private static final String TAG = "Navigation";
    private static final NavigationManager INSTANCE = new NavigationManager();
    private final RoomScanner scanner = new RoomScanner(this);
    private final RobotLocalizer localizer = new RobotLocalizer(this);
    private final RobotGuide guide = new RobotGuide(this);
    private volatile ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile QiContext qiContext;
    private volatile Holder holder;
    private volatile ExplorationMap activeMap;
    private volatile RoomScanEntity activeScan;
    private volatile boolean localized;

    private NavigationManager() {
    }

    public static NavigationManager get() {
        return INSTANCE;
    }

    synchronized void submit(Runnable task) {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(task);
    }

    private synchronized void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void setQiContext(QiContext context) {
        this.qiContext = context;
    }

    QiContext qiContext() {
        return qiContext;
    }

    public void maybeAutoLocalize() {
        QiContext c = qiContext;
        if (c == null || localized || !NavigationSettings.isAutoLocalize(c)) {
            return;
        }
        String scanId = NavigationSettings.getDefaultScanId(c);
        if (scanId.isEmpty()) {
            return;
        }
        submit(() -> {
            try {
                RoomScanEntity scan = dao(c).getScan(scanId);
                if (scan != null) {
                    localize(scan, new Callback<Boolean>() {
                        @Override
                        public void onResult(Boolean value) {
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
                }
            } catch (Exception e) {
                Log.w(TAG, "auto-localize failed: " + e.getMessage());
            }
        });
    }

    public void onFocusLost() {
        scanner.stopScanning();
        scanner.cancelScanStopListener();
        guide.cancelActiveGoTo();
        localizer.cancelLocalize();
        scanner.cancelMapping();
        releaseAbilities();
        shutdownExecutor();
        localized = false;
        qiContext = null;
    }

    public void setMapUpdateListener(MapUpdateListener listener) {
        scanner.setMapUpdateListener(listener);
    }

    public void setScanStopCallback(Runnable callback) {
        scanner.setScanStopCallback(callback);
    }

    public void speak(String text) {
        QiContext c = qiContext;
        if (c == null || text == null || text.trim().isEmpty()) {
            return;
        }
        submit(() -> {
            try {
                SpeechManager.getInstance().say(c, text);
            } catch (Exception ignored) {
            }
        });
    }

    public boolean isLocalized() {
        return localized;
    }

    void setLocalized(boolean value) {
        this.localized = value;
    }

    public boolean isScanning() {
        return scanner.isScanning();
    }

    public RoomScanEntity getActiveScan() {
        return activeScan;
    }

    void setActiveScan(RoomScanEntity scan) {
        this.activeScan = scan;
    }

    void setActiveMap(ExplorationMap map) {
        this.activeMap = map;
    }

    public void startScan(Callback<Void> cb) {
        scanner.startScan(cb);
    }

    public void stopAndSaveScan(String name, Callback<RoomScanEntity> cb) {
        scanner.stopAndSaveScan(name, cb);
    }

    public void captureSnapshot() {
        scanner.captureSnapshot();
    }

    public void captureRotation() {
        scanner.captureRotation();
    }

    public void listScans(Callback<List<RoomScanEntity>> cb) {
        submit(() -> {
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
        submit(() -> {
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
        localizer.localize(scan, cb);
    }

    public void saveWaypoint(String name, String type, Callback<WaypointEntity> cb) {
        submit(() -> {
            QiContext c = qiContext;
            RoomScanEntity scan = activeScan;
            if (c == null || scan == null || !localized) {
                cb.onError("Pepper ist noch nicht lokalisiert. Aktiviere zuerst einen Raum-Scan.");
                return;
            }
            try {
                Transform t = robotInMap(c);
                double[] pose = pose2d(t);
                WaypointEntity wp = new WaypointEntity(
                        scan.id, name, type, pose[0], pose[1], pose[2], System.currentTimeMillis());
                wp.id = dao(c).insertWaypoint(wp);
                cb.onResult(wp);
            } catch (Exception e) {
                Log.w(TAG, "saveWaypoint failed: " + e.getMessage());
                cb.onError("Wegpunkt konnte nicht gespeichert werden.");
            }
        });
    }

    public void listWaypoints(Callback<List<WaypointEntity>> cb) {
        submit(() -> {
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
        submit(() -> {
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
        guide.goToWaypoint(wp, cb);
    }

    public void guideToWaypoint(WaypointEntity wp, Callback<GuideOutcome> cb) {
        guide.guideToWaypoint(wp, cb);
    }

    public boolean hasFotostand(QiContext context) {
        return guide.hasFotostand(context);
    }

    public void driveToFotostandIfPossible(QiContext context) {
        guide.driveToFotostandIfPossible(context);
    }

    public void getRobotPose(Callback<double[]> cb) {
        submit(() -> {
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

    void cancelActiveGoTo() {
        guide.cancelActiveGoTo();
    }

    void rotateFullCircle(QiContext c, int steps, Runnable onStep) {
        guide.rotateFullCircle(c, steps, onStep);
    }

    void rotateFullCircle(QiContext c, int steps, Runnable onStep, RobotGuide.Condition shouldContinue) {
        guide.rotateFullCircle(c, steps, onStep, shouldContinue);
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

    void holdAbilities(QiContext c) {
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

    void releaseAbilities() {
        Holder h = holder;
        holder = null;
        if (h != null) {
            try {
                h.release();
            } catch (Exception ignored) {
            }
        }
    }

    File mapDir(Context context) {
        File dir = new File(context.getFilesDir(), "maps");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    void writeFile(File file, String content) throws Exception {
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    String readFile(String path) {
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

    com.buhlergroup.pepper.action.navigation.data.NavigationDao dao(Context context) {
        return NavigationDatabase.get(context).navigationDao();
    }

    public enum GuideOutcome {
        ARRIVED,
        STOPPED,
        LOST
    }

    public interface Callback<T> {
        void onResult(T value);

        void onError(String error);
    }

    public interface MapUpdateListener {
        void onMapBitmap(Bitmap bitmap);
    }
}
