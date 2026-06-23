package com.buhlergroup.pepper.action.selfie;

import android.content.Context;

import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.selfie.data.SelfieDao;
import com.buhlergroup.pepper.action.selfie.data.SelfieDatabase;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.debug.DebugLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SelfieRepository {

    private static final String TAG = "SelfieRepository";
    private static final long ORPHAN_GRACE_MILLIS = 60L * 60L * 1000L;
    private static final ExecutorService maintenanceExecutor = Executors.newSingleThreadExecutor();
    private static volatile SelfieRepository instance;
    private final SelfieDao dao;
    private final File imagesDir;

    private SelfieRepository(Context context) {
        this.dao = SelfieDatabase.get(context).selfieDao();
        this.imagesDir = new File(context.getFilesDir(), "selfies");
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            DebugLog.get().w(TAG, "Bildverzeichnis konnte nicht angelegt werden");
        }
    }

    public static SelfieRepository get(Context context) {
        if (instance == null) {
            synchronized (SelfieRepository.class) {
                if (instance == null) {
                    instance = new SelfieRepository(context);
                }
            }
        }
        return instance;
    }

    public static void purgeExpiredAsync(Context context) {
        Context app = context.getApplicationContext();
        maintenanceExecutor.submit(() -> {
            try {
                int days = SelfieSettings.getRetentionDays(app);
                if (days <= 0) {
                    return;
                }
                Set<String> protectedIds = new HashSet<>(RaffleRepository.get(app).linkedSelfieIds());
                get(app).purgeExpired(days, protectedIds);
            } catch (Exception e) {
                DebugLog.get().w(TAG, "purgeExpired fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    public SelfieEntity save(byte[] jpeg) throws IOException {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String filename = id + ".jpg";
        try (FileOutputStream fos = new FileOutputStream(new File(imagesDir, filename))) {
            fos.write(jpeg);
        }
        SelfieEntity entity = new SelfieEntity(id, System.currentTimeMillis(), false, filename);
        entity.number = dao.insert(entity);
        return entity;
    }

    public List<SelfieEntity> getAll() {
        return dao.getAll();
    }

    public SelfieEntity findById(String id) {
        return dao.findById(id);
    }

    public void setFavorite(String id, boolean favorite) {
        dao.setFavorite(id, favorite);
    }

    public void delete(String id) {
        SelfieEntity entity = dao.findById(id);
        if (entity != null) {
            dao.deleteById(id);
            File file = new File(imagesDir, entity.filename);
            if (file.exists() && !file.delete()) {
                DebugLog.get().w(TAG, "Bilddatei konnte nicht gelöscht werden");
            }
        }
    }

    public File imagesDir() {
        return imagesDir;
    }

    public void purgeExpired(int days, Set<String> protectedIds) {
        long cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L;
        for (SelfieEntity selfie : dao.getExpired(cutoff)) {
            if (protectedIds != null && protectedIds.contains(selfie.id)) {
                continue;
            }
            delete(selfie.id);
        }
        purgeOrphanedFiles();
    }

    private void purgeOrphanedFiles() {
        File[] files = imagesDir.listFiles();
        if (files == null) {
            return;
        }
        Set<String> known = new HashSet<>();
        for (SelfieEntity selfie : dao.getAll()) {
            known.add(selfie.filename);
        }
        long orphanCutoff = System.currentTimeMillis() - ORPHAN_GRACE_MILLIS;
        int removed = 0;
        for (File file : files) {
            if (file.isFile() && !known.contains(file.getName()) && file.lastModified() < orphanCutoff) {
                if (file.delete()) {
                    removed++;
                } else {
                    DebugLog.get().w(TAG, "Verwaiste Bilddatei konnte nicht gelöscht werden");
                }
            }
        }
        if (removed > 0) {
            DebugLog.get().i(TAG, "Verwaiste Bilddateien entfernt: " + removed);
        }
    }
}
