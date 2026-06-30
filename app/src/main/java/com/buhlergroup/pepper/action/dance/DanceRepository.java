package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.audio.SongAudioAnalyzer;
import com.buhlergroup.pepper.action.dance.data.DanceDao;
import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.action.dance.itunes.ITunesSearch;
import com.buhlergroup.pepper.action.dynamicanim.DanceGenerator;
import com.buhlergroup.pepper.action.dynamicanim.SongPlan;
import com.buhlergroup.pepper.action.dynamicanim.SongResearcher;
import com.buhlergroup.pepper.debug.DebugLog;

import java.io.File;
import java.util.List;

public final class DanceRepository {

    private static final String TAG = "Dance";

    private final DanceGenerator generator = new DanceGenerator();
    private final SongResearcher songResearcher = new SongResearcher();

    public DanceEntity getOrCreate(Context context, String query,
                                   DanceGenerator.ProgressListener progress) throws Exception {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        File danceDir = DanceFileStore.danceDir(context);

        if (progress != null) {
            progress.onStage(DanceGenerator.Stage.SEARCH);
        }
        SongPlan plan = songResearcher.planSong(context, query);
        SongSource source = resolveSource(plan.query);
        String songName = normalizeSongName(source.title);

        DanceEntity bySong = dao.findBySongName(songName);
        if (bySong != null) {
            if (bySong.qianimPath != null && new File(bySong.qianimPath).exists()) {
                Log.i(TAG, "Reusing dance for " + songName);
                if (!DanceAudioCache.applyLocalAudio(bySong)) {
                    bySong.previewUrl = source.previewUrl;
                }
                return bySong;
            }
            dao.deleteById(bySong.youtubeId);
        }

        DanceEntity existing = dao.findById(source.sourceId);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
            if (!DanceAudioCache.applyLocalAudio(existing)) {
                existing.previewUrl = source.previewUrl;
            }
            return existing;
        }

        long durationMs = source.durationMs > 0
                ? source.durationMs : DanceSettings.getDefaultDurationMs(context);
        int seconds = (int) Math.max(8, Math.min(30, durationMs / 1000));

        if (progress != null) {
            progress.onStage(DanceGenerator.Stage.AUDIO);
        }
        File audioFile = DanceAudioCache.downloadPreview(danceDir, source);

        if (progress != null) {
            progress.onStage(DanceGenerator.Stage.BEAT);
        }
        SongAudioAnalyzer.Result analysis =
                audioFile != null ? SongAudioAnalyzer.analyze(audioFile) : null;
        int measuredBpm = analysis != null && analysis.hasBpm() ? analysis.bpm : 0;

        String qianim = generator.generateValidatedDance(
                context, songName, seconds, null, plan.mood, measuredBpm, progress);
        if (qianim == null) {
            throw new Exception("Tanz-Choreografie konnte nicht erzeugt werden.");
        }

        File qianimFile = new File(danceDir, DanceFileStore.sanitizeFileName(source.sourceId) + ".qianim");
        DanceFileStore.writeFile(qianimFile, qianim);

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

    private SongSource resolveSource(String query) throws Exception {
        ITunesSearch.Result track = new ITunesSearch().search(query);
        Log.i(TAG, "Selected '" + track.title + "' (" + track.trackId + ")");
        return new SongSource(track.trackId, track.title, track.previewUrl, track.durationMs);
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
        DanceFileStore.deleteQuietly(dance.qianimPath);
        DanceFileStore.deleteQuietly(dance.audioPath);
    }

    public void preparePlayback(Context context, DanceEntity dance) {
        if (DanceAudioCache.applyLocalAudio(dance)) {
            Log.i(TAG, "Playing '" + dance.songName + "' from local cache");
            return;
        }
        if (!dance.youtubeId.startsWith(BuiltInDanceSeeder.BUILTIN_PREFIX)) {
            try {
                ITunesSearch.Result track = new ITunesSearch().search(dance.songName);
                dance.previewUrl = track.previewUrl;
                DanceAudioCache.backfillAudioCache(context, dance, track.previewUrl);
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
            File fallback = new File(DanceFileStore.danceDir(context), "fallback_audio.mp3");
            if (!fallback.exists()) {
                DanceFileStore.copyRawToFile(context, R.raw.summer, fallback);
            }
            return fallback.getAbsolutePath();
        } catch (Exception e) {
            Log.w(TAG, "Fallback audio unavailable: " + e.getMessage());
            return null;
        }
    }

    public void aiEdit(Context context, DanceEntity dance, String instruction) throws Exception {
        DanceGenerator.DanceEdit edit =
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
                DanceFileStore.writeFile(new File(dance.qianimPath), qianim);
                Log.i(TAG, "Edit regenerated choreography for " + dance.songName);
                changed = true;
            }
        }

        if (!changed) {
            throw new Exception("Die Anweisung konnte nicht angewendet werden.");
        }
    }
}
