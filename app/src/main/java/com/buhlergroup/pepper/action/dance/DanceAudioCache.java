package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP audio download + local cache concern for the dance feature. Handles preview
 * downloads, backfilling the on-device audio cache, and binding a dance to its local
 * audio file. Stateless; all methods are static.
 */
final class DanceAudioCache {

    private static final String TAG = "Dance";

    private DanceAudioCache() {
    }

    @Nullable
    static File downloadPreview(File danceDir, SongSource source) {
        if (source.previewUrl == null || source.previewUrl.isEmpty()) {
            return null;
        }
        try {
            File audioFile = new File(danceDir, DanceFileStore.sanitizeFileName(source.sourceId) + ".m4a");
            downloadToFile(source.previewUrl, audioFile);
            Log.i(TAG, "Cached preview audio for " + source.title);
            return audioFile;
        } catch (Exception e) {
            Log.w(TAG, "Could not cache preview audio for " + source.title + ": " + e.getMessage());
            return null;
        }
    }

    static void downloadToFile(String urlString, File dest) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", "PepperDance/1.0");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Audio-Download fehlgeschlagen (HTTP " + code + ").");
            }
            File tmp = new File(dest.getAbsolutePath() + ".part");
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            if (dest.exists() && !dest.delete()) {
                throw new IOException("Alte Audio-Datei konnte nicht ersetzt werden.");
            }
            if (!tmp.renameTo(dest)) {
                throw new IOException("Audio-Datei konnte nicht gespeichert werden.");
            }
        } finally {
            connection.disconnect();
        }
    }

    static boolean applyLocalAudio(DanceEntity dance) {
        if (dance.audioPath != null && new File(dance.audioPath).exists()) {
            dance.previewUrl = dance.audioPath;
            return true;
        }
        return false;
    }

    static void backfillAudioCache(Context context, DanceEntity dance, String previewUrl) {
        if (previewUrl == null || previewUrl.isEmpty()) {
            return;
        }
        try {
            File audioFile = new File(DanceFileStore.danceDir(context),
                    DanceFileStore.sanitizeFileName(dance.youtubeId) + ".m4a");
            downloadToFile(previewUrl, audioFile);
            dance.audioPath = audioFile.getAbsolutePath();
            dance.previewUrl = audioFile.getAbsolutePath();
            DanceDatabase.get(context).danceDao().setAudioPath(dance.youtubeId, dance.audioPath);
            Log.i(TAG, "Backfilled local audio cache for " + dance.songName);
        } catch (Exception e) {
            Log.w(TAG, "Could not backfill audio cache for " + dance.songName + ": " + e.getMessage());
        }
    }
}
