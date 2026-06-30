package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Raw file-persistence helpers for navigation maps. Stateless; all methods static.
 */
final class MapFileStore {

    private static final String TAG = "Navigation";

    private MapFileStore() {
    }

    static File mapDir(Context context) {
        File dir = new File(context.getFilesDir(), "maps");
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

    @Nullable
    static String readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
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

    static void deleteFileQuietly(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception ignored) {
        }
    }
}
