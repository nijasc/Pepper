package com.buhlergroup.pepper.action.thinking;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.ArrayList;
import java.util.List;

public final class ThinkingController {

    private static final String TAG = "Thinking";

    private static final String[] HMM_CLIPS = {
            "hmm_1", "hmm_2", "hmm_3", "mhm_1", "mhm_2"
    };

    private static final String[] FILLERS_DE = {
            "Lass mich kurz überlegen.",
            "Einen kleinen Moment.",
            "Gute Frage, einen Augenblick."
    };

    private static final String[] FILLERS_EN = {
            "Let me think for a moment.",
            "Just a second.",
            "Good question, one moment."
    };

    private static final String[] PROGRESS_DE = {
            "Gleich hab ich's.",
            "Dauert nicht mehr lange.",
            "Fast fertig, einen Moment noch."
    };

    private static final String[] PROGRESS_EN = {
            "Almost there.",
            "It won't take much longer.",
            "Nearly done, one more moment."
    };

    private static final long LOOP_INTERVAL_MS = 8000;

    private static final ThinkingController INSTANCE = new ThinkingController();

    private volatile Future<Void> animationFuture;
    private volatile Future<Void> fillerFuture;
    private volatile MediaPlayer fillerPlayer;
    private volatile boolean active;
    private volatile Thread loopThread;
    private int lastFiller = -1;
    private int lastClip = -1;
    private int lastProgress = -1;

    private ThinkingController() {
    }

    public static ThinkingController get() {
        return INSTANCE;
    }

    public synchronized void start(QiContext context) {
        if (active) {
            return;
        }
        active = true;
        startPose(context);
        startFiller(context);
        startLoop(context);
    }

    public synchronized void stop() {
        if (!active) {
            return;
        }
        active = false;
        Thread t = loopThread;
        loopThread = null;
        if (t != null) {
            t.interrupt();
        }
        cancel(animationFuture);
        cancel(fillerFuture);
        releaseFillerPlayer();
        animationFuture = null;
        fillerFuture = null;
    }

    private void startLoop(QiContext context) {
        Thread t = new Thread(() -> {
            int round = 0;
            while (active) {
                try {
                    Thread.sleep(LOOP_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (!active) {
                    return;
                }
                round++;
                restartPoseIfDone(context);
                if (round % 2 == 0) {
                    speakProgress(context);
                } else {
                    startFiller(context);
                }
            }
        }, "thinking-loop");
        t.setDaemon(true);
        loopThread = t;
        t.start();
    }

    private void restartPoseIfDone(QiContext context) {
        Future<Void> f = animationFuture;
        if (f == null || f.isDone()) {
            startPose(context);
        }
    }

    private void startPose(QiContext context) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.searching_a001)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            animationFuture = animate.async().run();
            consume(animationFuture, "thinking pose");
        } catch (Exception e) {
            Log.w(TAG, "Thinking pose failed: " + e.getMessage());
        }
    }

    private void consume(Future<Void> future, String label) {
        if (future == null) {
            return;
        }
        future.thenConsume(done -> {
            if (done.hasError()) {
                Log.w(TAG, label + " did not finish: " + done.getError().getMessage());
            }
        });
    }

    private void startFiller(QiContext context) {
        if (playHmmClip(context)) {
            return;
        }
        speakFiller(context);
    }

    private boolean playHmmClip(Context context) {
        List<Integer> clips = resolveClips(context);
        if (clips.isEmpty()) {
            Log.i(TAG, "No hmm clips in res/raw, using spoken filler");
            return false;
        }
        try {
            releaseFillerPlayer();
            int index = pickIndex(clips.size(), lastClip);
            lastClip = index;
            MediaPlayer player = MediaPlayer.create(context, clips.get(index));
            if (player == null) {
                return false;
            }
            fillerPlayer = player;
            player.setOnCompletionListener(p -> {
                p.release();
                if (fillerPlayer == p) {
                    fillerPlayer = null;
                }
            });
            player.start();
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Hmm clip playback failed: " + e.getMessage());
            releaseFillerPlayer();
            return false;
        }
    }

    private List<Integer> resolveClips(Context context) {
        List<Integer> ids = new ArrayList<>();
        for (String name : HMM_CLIPS) {
            int id = context.getResources().getIdentifier(name, "raw", context.getPackageName());
            if (id != 0) {
                ids.add(id);
            }
        }
        return ids;
    }

    private void speakFiller(QiContext context) {
        try {
            SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
            String[] fillers = lang == SupportedLanguage.ENGLISH ? FILLERS_EN : FILLERS_DE;
            int index = pickIndex(fillers.length, lastFiller);
            lastFiller = index;
            say(context, fillers[index], lang);
        } catch (Exception e) {
            Log.w(TAG, "Thinking filler failed: " + e.getMessage());
        }
    }

    private void speakProgress(QiContext context) {
        try {
            SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
            String[] phrases = lang == SupportedLanguage.ENGLISH ? PROGRESS_EN : PROGRESS_DE;
            int index = pickIndex(phrases.length, lastProgress);
            lastProgress = index;
            say(context, phrases[index], lang);
        } catch (Exception e) {
            Log.w(TAG, "Progress filler failed: " + e.getMessage());
        }
    }

    private void say(QiContext context, String text, SupportedLanguage lang) {
        Locale locale = new Locale(lang.getQiLang(), lang.getRegion());
        Say say = SayBuilder.with(context)
                .withText(text)
                .withLocale(locale)
                .build();
        fillerFuture = say.async().run();
        consume(fillerFuture, "thinking filler");
    }

    private int pickIndex(int length, int last) {
        int index;
        do {
            index = (int) (Math.random() * length);
        } while (length > 1 && index == last);
        return index;
    }

    private void releaseFillerPlayer() {
        MediaPlayer player = fillerPlayer;
        fillerPlayer = null;
        if (player != null) {
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

    private void cancel(Future<Void> future) {
        if (future != null) {
            try {
                future.requestCancellation();
            } catch (Exception ignored) {
            }
        }
    }
}
