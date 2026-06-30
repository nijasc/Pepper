package com.buhlergroup.pepper.action.dance;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Pure file-system helpers for the dance feature: qianim/audio file I/O, the dance
 * cache directory, and raw-resource copying. Stateless; all methods are static.
 */
final class DanceFileStore {

    private DanceFileStore() {
    }

    static String readQianim(File file) throws Exception {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int read = 0;
            while (read < bytes.length) {
                int r = in.read(bytes, read, bytes.length - read);
                if (r < 0) {
                    break;
                }
                read += r;
            }
            return new String(bytes, 0, read, StandardCharsets.UTF_8);
        }
    }

    static File danceDir(Context context) {
        File dir = new File(context.getFilesDir(), "dances");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    static void writeFile(File file, String content) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    static void copyRawToFile(Context context, int rawRes, File dest) throws IOException {
        File tmp = new File(dest.getAbsolutePath() + ".part");
        try (InputStream in = context.getResources().openRawResource(rawRes);
             FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Alte Animations-Datei konnte nicht ersetzt werden.");
        }
        if (!tmp.renameTo(dest)) {
            throw new IOException("Animations-Datei konnte nicht gespeichert werden.");
        }
    }

    static void deleteQuietly(String path) {
        if (path == null) {
            return;
        }
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception ignored) {
        }
    }

    static String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
