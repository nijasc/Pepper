package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.action.dance.data.DanceDao;
import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.action.dance.audio.SongAudioAnalyzer;
import com.buhlergroup.pepper.action.dance.itunes.ITunesSearch;
import com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator;
import com.buhlergroup.pepper.action.dynamicanim.SongPlan;
import com.buhlergroup.pepper.action.dynamicanim.SongResearcher;
import com.buhlergroup.pepper.debug.DebugLog;

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
    private static final String BUILTIN_PREFIX = "builtin_";
    private static final String BUILTIN_HULA_ID = BUILTIN_PREFIX + "hula";
    private static final String BUILTIN_SIX_SEVEN_ID = BUILTIN_PREFIX + "six_seven";

    private final AnimationGenerator generator = new AnimationGenerator();
    private final SongResearcher songResearcher = new SongResearcher();

    public void ensureBuiltInDances(Context context) {
        try {
            seedBuiltIn(context, BUILTIN_HULA_ID, "Hula",
                    com.buhlergroup.pepper.R.raw.hula_dance,
                    com.buhlergroup.pepper.R.raw.summer, 12000L);
            seedBuiltIn(context, BUILTIN_SIX_SEVEN_ID, "Six Seven",
                    com.buhlergroup.pepper.R.raw.six_seven,
                    com.buhlergroup.pepper.R.raw.wyoming, 15000L);
        } catch (Exception e) {
            Log.w(TAG, "Could not seed built-in dances: " + e.getMessage());
        }
    }

    private void seedBuiltIn(Context context, String id, String name, int rawRes, int audioRawRes,
            long durationMs) throws IOException {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        DanceEntity existing = dao.findById(id);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
            if (audioRawRes != 0
                    && (existing.audioPath == null || !new File(existing.audioPath).exists())) {
                File audio = new File(danceDir(context), id + ".mp3");
                copyRawToFile(context, audioRawRes, audio);
                dao.setAudioPath(id, audio.getAbsolutePath());
                Log.i(TAG, "Backfilled audio for built-in '" + name + "'");
            }
            return;
        }
        File target = new File(danceDir(context), id + ".qianim");
        copyRawToFile(context, rawRes, target);
        DanceEntity entity = new DanceEntity(
                id, name, target.getAbsolutePath(), durationMs, false, System.currentTimeMillis());
        if (audioRawRes != 0) {
            File audio = new File(danceDir(context), id + ".mp3");
            copyRawToFile(context, audioRawRes, audio);
            entity.audioPath = audio.getAbsolutePath();
        }
        dao.insert(entity);
        Log.i(TAG, "Seeded built-in dance '" + name + "'");
    }

    private void copyRawToFile(Context context, int rawRes, File dest) throws IOException {
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

    public DanceEntity getOrCreate(Context context, String query) throws Exception {
        return getOrCreate(context, query, null);
    }

    public DanceEntity getOrCreate(Context context, String query,
                                   AnimationGenerator.ProgressListener progress) throws Exception {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        File danceDir = danceDir(context);

        if (progress != null) {
            progress.onStage(AnimationGenerator.Stage.SEARCH);
        }
        SongPlan plan = songResearcher.planSong(context, query);
        SongSource source = resolveSource(plan.query);
        String songName = normalizeSongName(source.title);

        DanceEntity bySong = dao.findBySongName(songName);
        if (bySong != null) {
            if (bySong.qianimPath != null && new File(bySong.qianimPath).exists()) {
                Log.i(TAG, "Reusing dance for " + songName);
                if (!applyLocalAudio(bySong)) {
                    bySong.previewUrl = source.previewUrl;
                }
                return bySong;
            }
            dao.deleteById(bySong.youtubeId);
        }

        DanceEntity existing = dao.findById(source.sourceId);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
            if (!applyLocalAudio(existing)) {
                existing.previewUrl = source.previewUrl;
            }
            return existing;
        }

        long durationMs = source.durationMs > 0 ? source.durationMs : 25000L;
        int seconds = (int) Math.max(8, Math.min(30, durationMs / 1000));

        if (progress != null) {
            progress.onStage(AnimationGenerator.Stage.AUDIO);
        }
        File audioFile = downloadPreview(danceDir, source);

        if (progress != null) {
            progress.onStage(AnimationGenerator.Stage.BEAT);
        }
        SongAudioAnalyzer.Result analysis =
                audioFile != null ? SongAudioAnalyzer.analyze(audioFile) : null;
        int measuredBpm = analysis != null && analysis.hasBpm() ? analysis.bpm : 0;

        String qianim = generator.generateValidatedDance(
                context, songName, seconds, null, plan.mood, measuredBpm, progress);
        if (qianim == null) {
            throw new Exception("Tanz-Choreografie konnte nicht erzeugt werden.");
        }

        File qianimFile = new File(danceDir, sanitizeFileName(source.sourceId) + ".qianim");
        writeFile(qianimFile, qianim);

        DanceEntity entity = new DanceEntity(
                source.sourceId, songName, qianimFile.getAbsolutePath(),
                durationMs, false, System.currentTimeMillis());
        entity.previewUrl = source.previewUrl;
        entity.audioStartMs = analysis != null && analysis.hasHook()
                ? analysis.hookStartMs : plan.startSeconds * 1000L;
        entity.bpm = measuredBpm;
        if (audioFile != null) {
            entity.audioPath = audioFile.getAbsolutePath();
            entity.previewUrl = audioFile.getAbsolutePath();
        }
        dao.insert(entity);
        Log.i(TAG, "Created dance for " + songName + " from iTunes " + source.sourceId
                + " (bpm=" + measuredBpm + ", startMs=" + entity.audioStartMs + ")");
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

    private File downloadPreview(File danceDir, SongSource source) {
        if (source.previewUrl == null || source.previewUrl.isEmpty()) {
            return null;
        }
        try {
            File audioFile = new File(danceDir, sanitizeFileName(source.sourceId) + ".m4a");
            downloadToFile(source.previewUrl, audioFile);
            Log.i(TAG, "Cached preview audio for " + source.title);
            return audioFile;
        } catch (Exception e) {
            Log.w(TAG, "Could not cache preview audio for " + source.title + ": " + e.getMessage());
            return null;
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

    public void preparePlayback(Context context, DanceEntity dance) {
        if (applyLocalAudio(dance)) {
            Log.i(TAG, "Playing '" + dance.songName + "' from local cache");
            return;
        }
        if (dance.youtubeId == null || !dance.youtubeId.startsWith(BUILTIN_PREFIX)) {
            try {
                ITunesSearch.Result track = new ITunesSearch().search(dance.songName);
                dance.previewUrl = track.previewUrl;
                backfillAudioCache(context, dance, track.previewUrl);
            } catch (Exception e) {
                Log.w(TAG, "Could not resolve preview for '" + dance.songName + "': " + e.getMessage());
                DebugLog.get().w(TAG,
                        "iTunes-Preview für '" + dance.songName + "' fehlgeschlagen: " + e.getMessage());
                dance.previewUrl = null;
            }
        }
        if (dance.previewUrl == null || dance.previewUrl.isEmpty()) {
            dance.previewUrl = fallbackAudioPath(context);
            if (dance.previewUrl != null) {
                DebugLog.get().w(TAG, "Kein Song-Audio für '" + dance.songName + "' – nutze Fallback-Musik");
            } else {
                DebugLog.get().w(TAG, "Kein Audio und kein Fallback für '" + dance.songName + "'");
            }
        }
    }

    private String fallbackAudioPath(Context context) {
        try {
            File fallback = new File(danceDir(context), "fallback_audio.mp3");
            if (!fallback.exists()) {
                copyRawToFile(context, com.buhlergroup.pepper.R.raw.summer, fallback);
            }
            return fallback.getAbsolutePath();
        } catch (Exception e) {
            Log.w(TAG, "Fallback audio unavailable: " + e.getMessage());
            return null;
        }
    }

    private boolean applyLocalAudio(DanceEntity dance) {
        if (dance.audioPath != null && new File(dance.audioPath).exists()) {
            dance.previewUrl = dance.audioPath;
            return true;
        }
        return false;
    }

    private void backfillAudioCache(Context context, DanceEntity dance, String previewUrl) {
        if (previewUrl == null || previewUrl.isEmpty() || dance.youtubeId == null) {
            return;
        }
        try {
            File audioFile = new File(danceDir(context), sanitizeFileName(dance.youtubeId) + ".m4a");
            downloadToFile(previewUrl, audioFile);
            dance.audioPath = audioFile.getAbsolutePath();
            dance.previewUrl = audioFile.getAbsolutePath();
            DanceDatabase.get(context).danceDao().setAudioPath(dance.youtubeId, dance.audioPath);
            Log.i(TAG, "Backfilled local audio cache for " + dance.songName);
        } catch (Exception e) {
            Log.w(TAG, "Could not backfill audio cache for " + dance.songName + ": " + e.getMessage());
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
                    context, dance.songName, seconds, edit.choreography, null, dance.bpm, null);
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
