package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.action.dance.data.DanceDao;
import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.action.dance.spotify.SpotifySearch;
import com.buhlergroup.pepper.action.dance.youtube.YoutubeSearch;
import com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DanceRepository {

    private static final String TAG = "Dance";

    private final AnimationGenerator generator = new AnimationGenerator();

    public DanceEntity getOrCreate(Context context, String query) throws Exception {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        File danceDir = danceDir(context);

        String songName = normalizeSongName(query);
        DanceEntity bySong = dao.findBySongName(songName);
        if (bySong != null) {
            if (bySong.qianimPath != null && new File(bySong.qianimPath).exists()) {
                Log.i(TAG, "Reusing dance for " + songName);
                return bySong;
            }
            dao.deleteById(bySong.youtubeId);
        }

        SongSource source = resolveSource(context, songName);

        DanceEntity existing = dao.findById(source.sourceId);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
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
                source.previewUrl, durationMs, false, System.currentTimeMillis());
        dao.insert(entity);
        Log.i(TAG, "Created dance for " + songName + " from " + source.origin);
        return entity;
    }

    private static final class SongSource {
        final String sourceId;
        final String previewUrl;
        final long durationMs;
        final String origin;

        SongSource(String sourceId, String previewUrl, long durationMs, String origin) {
            this.sourceId = sourceId;
            this.previewUrl = previewUrl;
            this.durationMs = durationMs;
            this.origin = origin;
        }
    }

    private SongSource resolveSource(Context context, String songName) throws Exception {
        SpotifySearch spotify = new SpotifySearch();
        if (spotify.isConfigured(context)) {
            try {
                SpotifySearch.Result result = spotify.search(context, songName);
                if (result.previewUrl != null) {
                    long durationMs = result.durationMs > 0
                            ? Math.min(result.durationMs, 30000L) : 30000L;
                    return new SongSource("spotify:" + result.trackId,
                            result.previewUrl, durationMs, "spotify");
                }
                Log.i(TAG, "Spotify track without preview, falling back to YouTube");
            } catch (Exception e) {
                Log.w(TAG, "Spotify search failed, falling back to YouTube: " + e.getMessage());
            }
        }
        YoutubeSearch.Result result = new YoutubeSearch().search(songName);
        return new SongSource(result.videoId, null, result.durationMs, "youtube");
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

    public void rename(Context context, String youtubeId, String name) {
        DanceDatabase.get(context).danceDao().rename(youtubeId, name);
    }

    public void delete(Context context, DanceEntity dance) {
        DanceDatabase.get(context).danceDao().deleteById(dance.youtubeId);
        deleteQuietly(dance.qianimPath);
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
