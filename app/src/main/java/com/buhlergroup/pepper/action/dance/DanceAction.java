package com.buhlergroup.pepper.action.dance;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.action.audio.AudioCoordinator;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DanceAction extends Action {

    public DanceAction(com.buhlergroup.pepper.openai.history.HistoryManager historyManager) {
        super(historyManager);
    }

    private static final String TAG = "Dance";
    private static final long MAX_PLAY_MS = 35000;
    private static final long MIN_PLAY_MS = 5000;

    private static final Set<String> GENERIC_TERMS = new HashSet<>(Arrays.asList(
            "tanz", "tanze", "tanzen", "dance", "bitte", "los", "mal", "etwas", "ein", "eine",
            "einen", "mir", "uns", "jetzt", "für", "mich", "for", "me", "please", "song", "lied",
            "music", "musik", "doch", "kurz", "zu", "the", "a", "mach", "leg"));

    private final DanceRepository repository = new DanceRepository();
    private final Object audioLock = new Object();
    private MediaPlayer mediaPlayer;

    @Override
    public void execute(QiContext context, String input) {
        String request = input == null ? "" : input.trim();
        DanceEntity dance = pickFromLibrary(context, request);
        if (dance != null) {
            repository.preparePlayback(context, dance);
            SpeechManager.getInstance().systemSay(context,
                    "Ich tanze einen Tanz aus meiner Sammlung für dich.");
            playDance(context, dance);
            return;
        }
        SpeechManager.getInstance().systemSay(context,
                "Ich habe noch keinen Tanz gespeichert, ich tanze etwas Eigenes.");
        playFallback(context);
    }

    private boolean hasConcreteSong(String request) {
        if (request.isEmpty()) {
            return false;
        }
        for (String token : request.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9äöüÄÖÜ]+")) {
            if (!token.isEmpty() && !GENERIC_TERMS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private DanceEntity pickFromLibrary(QiContext context, String request) {
        List<DanceEntity> playable = new ArrayList<>();
        List<DanceEntity> favorites = new ArrayList<>();
        for (DanceEntity dance : repository.all(context)) {
            if (dance.qianimPath != null && new File(dance.qianimPath).exists()) {
                playable.add(dance);
                if (dance.favorite) {
                    favorites.add(dance);
                }
            }
        }
        if (playable.isEmpty()) {
            return null;
        }
        if (hasConcreteSong(request)) {
            DanceEntity match = matchByName(playable, request);
            if (match != null) {
                return match;
            }
        }
        List<DanceEntity> pool = !favorites.isEmpty() ? favorites : playable;
        return pool.get((int) (Math.random() * pool.size()));
    }

    private DanceEntity matchByName(List<DanceEntity> dances, String request) {
        String needle = request.toLowerCase(Locale.ROOT);
        DanceEntity best = null;
        int bestLen = 0;
        for (DanceEntity dance : dances) {
            String name = dance.songName == null ? "" : dance.songName.toLowerCase(Locale.ROOT);
            if (!name.isEmpty() && needle.contains(name) && name.length() > bestLen) {
                best = dance;
                bestLen = name.length();
            }
        }
        return best;
    }

    private void playDance(QiContext context, DanceEntity dance) {
        Future<Void> animationFuture = null;
        try {
            String qianim = DanceRepository.readQianim(new File(dance.qianimPath));
            Animation animation = AnimationBuilder.with(context).withTexts(qianim).build();
            Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();

            if (dance.previewUrl == null || dance.previewUrl.isEmpty()) {
                DebugLog.get().w(TAG, "Tanz '" + dance.songName + "' ohne Audio-Quelle – kein Ton");
            }
            MediaPlayer player = dance.previewUrl != null
                    ? startAudioUrl(dance.previewUrl, dance.audioStartMs) : null;

            animationFuture = animate.async().run();
            QiFutures.consume(animationFuture, TAG, "dance animation");

            long clipMs = player != null
                    ? player.getDuration() - dance.audioStartMs : dance.durationMs;
            if (clipMs <= 0) {
                clipMs = dance.durationMs;
            }
            long playMs = Math.max(MIN_PLAY_MS, Math.min(MAX_PLAY_MS, clipMs));
            awaitAnimation(animationFuture, playMs);

            stopAudio();
            if (!animationFuture.isDone()) {
                animationFuture.requestCancellation();
            }
        } catch (Exception e) {
            Log.w(TAG, "Dance playback failed: " + e.getMessage());
            stopAudio();
            if (animationFuture != null && !animationFuture.isDone()) {
                animationFuture.requestCancellation();
            }
            playFallback(context);
        }
    }

    private void awaitAnimation(Future<Void> animationFuture, long capMs) {
        long deadline = System.currentTimeMillis() + capMs;
        while (System.currentTimeMillis() < deadline) {
            if (animationFuture == null || animationFuture.isDone()) {
                return;
            }
            sleep(100);
        }
    }

    private MediaPlayer startAudioUrl(String url, long startMs) {
        stopAudio();
        try {
            MediaPlayer player = new MediaPlayer();
            player.setOnErrorListener((mp, what, extra) -> {
                DebugLog.get().w(TAG, "MediaPlayer-Fehler what=" + what + " extra=" + extra);
                return false;
            });
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            if (startMs > 0 && startMs < player.getDuration()) {
                player.seekTo((int) startMs);
            }
            player.start();
            synchronized (audioLock) {
                mediaPlayer = player;
            }
            AudioCoordinator.get().attachMusic(player);
            DebugLog.get().i(TAG, "Musik gestartet");
            return player;
        } catch (Exception e) {
            Log.w(TAG, "Preview playback failed: " + e.getMessage());
            DebugLog.get().w(TAG, "Audio-Wiedergabe fehlgeschlagen: " + e.getMessage());
            stopAudio();
            return null;
        }
    }

    private void playFallback(QiContext context) {
        try {
            SpeechManager.getInstance().systemSay(context, "Six... seven!");

            int audioRes = context.getResources()
                    .getIdentifier("doot_doot", "raw", context.getPackageName());
            if (audioRes == 0) {
                audioRes = R.raw.wyoming;
            }
            MediaPlayer player = startAudioResource(context, audioRes);

            long clipMs = player != null ? player.getDuration() : 0;
            if (clipMs <= 0) {
                clipMs = 15000;
            }
            long playMs = Math.max(MIN_PLAY_MS, Math.min(MAX_PLAY_MS, clipMs));

            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.six_seven).build();
            Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();

            long end = System.currentTimeMillis() + playMs;
            while (System.currentTimeMillis() < end) {
                animate.run();
            }
            stopAudio();
        } catch (Exception e) {
            Log.w(TAG, "Fallback dance failed: " + e.getMessage());
            stopAudio();
        }
    }

    private MediaPlayer startAudioResource(QiContext context, int resId) {
        stopAudio();
        MediaPlayer player = MediaPlayer.create(context, resId);
        synchronized (audioLock) {
            mediaPlayer = player;
        }
        if (player != null) {
            AudioCoordinator.get().attachMusic(player);
            player.start();
        }
        return player;
    }

    private void stopAudio() {
        MediaPlayer player;
        synchronized (audioLock) {
            player = mediaPlayer;
            mediaPlayer = null;
        }
        if (player != null) {
            AudioCoordinator.get().detachMusic(player);
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
            } catch (Exception ignored) {
            }
            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getDescription() {
        return "Makes Pepper dance. Pepper plays one of the dances already saved in its library, "
                + "preferring one that matches a song the user names. It never creates new dances; "
                + "new choreographies are generated only from the admin dance library.";
    }
}
