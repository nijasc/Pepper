package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.action.dance.data.DanceDao;
import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.action.dance.itunes.ITunesSearch;
import com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DanceRepository {

    private static final String TAG = "Dance";

    private final AnimationGenerator generator = new AnimationGenerator();

    public DanceEntity getOrCreate(Context context, String query) throws Exception {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        File danceDir = danceDir(context);

        AnimationGenerator.SongPlan plan = generator.planSong(context, query);
        SongSource source = resolveSource(plan.query);
        String songName = normalizeSongName(source.title);

        DanceEntity bySong = dao.findBySongName(songName);
        if (bySong != null) {
            if (bySong.qianimPath != null && new File(bySong.qianimPath).exists()) {
                Log.i(TAG, "Reusing dance for " + songName);
                bySong.previewUrl = source.previewUrl;
                return bySong;
            }
            dao.deleteById(bySong.youtubeId);
        }

        DanceEntity existing = dao.findById(source.sourceId);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
            existing.previewUrl = source.previewUrl;
            return existing;
        }

        long durationMs = source.durationMs > 0 ? source.durationMs : 25000L;
        int seconds = (int) Math.max(8, Math.min(30, durationMs / 1000));
        String qianim = generator.generateValidatedDance(context, songName, seconds);
        if (qianim == null) {
            throw new Exception("Tanz-Choreografie konnte nicht erzeugt werden.");
        }

        File qianimFile = new File(danceDir, sanitizeFileName(source.sourceId) + ".qianim");
        writeFile(qianimFile, qianim);

        DanceEntity entity = new DanceEntity(
                source.sourceId, songName, qianimFile.getAbsolutePath(),
                durationMs, false, System.currentTimeMillis());
        entity.previewUrl = source.previewUrl;
        entity.audioStartMs = plan.startSeconds * 1000L;
        cacheAudioQuietly(danceDir, entity, source);
        dao.insert(entity);
        Log.i(TAG, "Created dance for " + songName + " from iTunes " + source.sourceId);
        return entity;
    }

    private static final class SongSource {
        final String sourceId;
        final String title;
        final String previewUrl;
        final long durationMs;

        SongSource(String sourceId, String title, String previewUrl, long durationMs) {
            this.sourceId = sourceId;
            this.title = title;
            this.previewUrl = previewUrl;
            this.durationMs = durationMs;
        }
    }

    private SongSource resolveSource(String query) throws Exception {
        ITunesSearch.Result track = new ITunesSearch().search(query);
        Log.i(TAG, "Selected '" + track.title + "' (" + track.trackId + ")");
        return new SongSource(track.trackId, track.title, track.previewUrl, track.durationMs);
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String normalizeSongName(String query) {
        return query == null ? "" : query.trim().replaceAll("\\s+", " ");
    }

    public List<DanceEntity> all(Context context) {
        return DanceDatabase.get(context).danceDao().getAll();
    }

    public void setFavorite(Context context, String youtubeId, boolean favorite) {
        DanceDatabase.get(context).danceDao().setFavorite(youtubeId, favorite);
    }

    public void setAudioStart(Context context, String youtubeId, long audioStartMs) {
        DanceDatabase.get(context).danceDao().setAudioStartMs(youtubeId, audioStartMs);
    }

    public void rename(Context context, String youtubeId, String name) {
        DanceDatabase.get(context).danceDao().rename(youtubeId, name);
    }

    public void delete(Context context, DanceEntity dance) {
        DanceDatabase.get(context).danceDao().deleteById(dance.youtubeId);
        deleteQuietly(dance.qianimPath);
        deleteQuietly(dance.audioPath);
    }

    private void cacheAudioQuietly(File danceDir, DanceEntity entity, SongSource source) {
        if (source.previewUrl == null || source.previewUrl.isEmpty()) {
            return;
        }
        try {
            File audioFile = new File(danceDir, sanitizeFileName(source.sourceId) + ".m4a");
            downloadToFile(source.previewUrl, audioFile);
            entity.audioPath = audioFile.getAbsolutePath();
            entity.previewUrl = audioFile.getAbsolutePath();
            Log.i(TAG, "Cached preview audio for " + entity.songName);
        } catch (Exception e) {
            Log.w(TAG, "Could not cache preview audio for " + entity.songName + ": " + e.getMessage());
        }
    }

    private void downloadToFile(String urlString, File dest) throws IOException {
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

    public void preparePlayback(DanceEntity dance) {
        try {
            ITunesSearch.Result track = new ITunesSearch().search(dance.songName);
            dance.previewUrl = track.previewUrl;
        } catch (Exception e) {
            Log.w(TAG, "Could not resolve preview for '" + dance.songName + "': " + e.getMessage());
            dance.previewUrl = null;
        }
    }

    public void aiEdit(Context context, DanceEntity dance, String instruction) throws Exception {
        AnimationGenerator.DanceEdit edit =
                generator.interpretEdit(context, dance.songName, dance.audioStartMs, instruction);
        DanceDao dao = DanceDatabase.get(context).danceDao();
        boolean changed = false;

        if (edit.startSeconds != null) {
            long ms = Math.max(0, Math.min(29, edit.startSeconds)) * 1000L;
            dao.setAudioStartMs(dance.youtubeId, ms);
            dance.audioStartMs = ms;
            Log.i(TAG, "Edit set start offset to " + ms + "ms for " + dance.songName);
            changed = true;
        }

        if (edit.choreography != null && dance.qianimPath != null) {
            int seconds = (int) Math.max(8, Math.min(30, dance.durationMs / 1000));
            String qianim = generator.generateValidatedDance(
                    context, dance.songName, seconds, edit.choreography);
            if (qianim != null) {
                writeFile(new File(dance.qianimPath), qianim);
                Log.i(TAG, "Edit regenerated choreography for " + dance.songName);
                changed = true;
            }
        }

        if (!changed) {
            throw new Exception("Die Anweisung konnte nicht angewendet werden.");
        }
    }

    public static String readQianim(File file) throws Exception {
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

    private File danceDir(Context context) {
        File dir = new File(context.getFilesDir(), "dances");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void writeFile(File file, String content) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void deleteQuietly(String path) {
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
}
