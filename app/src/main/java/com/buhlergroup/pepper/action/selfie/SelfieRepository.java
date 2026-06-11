package com.buhlergroup.pepper.action.selfie;

import android.content.Context;

import com.buhlergroup.pepper.action.selfie.data.SelfieDao;
import com.buhlergroup.pepper.action.selfie.data.SelfieDatabase;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class SelfieRepository {

    private static volatile SelfieRepository instance;

    private final SelfieDao dao;
    private final File imagesDir;

    private SelfieRepository(Context context) {
        this.dao = SelfieDatabase.get(context).selfieDao();
        this.imagesDir = new File(context.getFilesDir(), "selfies");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
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
            new File(imagesDir, entity.filename).delete();
        }
    }

    public File imagesDir() {
        return imagesDir;
    }
}
