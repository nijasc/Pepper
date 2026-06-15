package com.buhlergroup.pepper.action.memory;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class MemoryGameController {

    private static final String TAG = "MemoryGame";
    private static final MemoryGameController INSTANCE = new MemoryGameController();

    private static final String[] PAD_NAMES = {"Grün", "Rot", "Gelb", "Blau"};

    private final Random random = new Random();
    private volatile MemoryGameView view;
    private volatile boolean aborted = false;
    private volatile boolean running = false;

    private MemoryGameController() {
    }

    public static MemoryGameController get() {
        return INSTANCE;
    }

    public void attachView(MemoryGameView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void abort() {
        aborted = true;
    }

    public boolean isRunning() {
        return running;
    }

    public int play(QiContext context, MemoryGameConfig config) {
        MemoryGameView board = view;
        if (board == null) {
            say(context, "Mein Tablet ist gerade nicht bereit, deshalb kann ich Memory nicht starten.");
            return 0;
        }
        if (running) {
            say(context, "Wir spielen doch schon!");
            return 0;
        }

        running = true;
        aborted = false;
        BlockingQueue<Integer> touches = new LinkedBlockingQueue<>();
        board.setOnPadListener(touches::offer);
        board.setInputEnabled(false);
        board.setScore(0);
        board.setHighscore(MemoryHighscore.get(context, config.label));
        board.setStatus("Schau gut zu!");
        board.setHint("Wiederhole die Sequenz auf dem Tablet");
        board.show();

        say(context, "Willkommen bei Memory mit Bewegung auf " + config.label + "! "
                + "Ich zeige dir eine Folge aus Farben und Tönen, und du wiederholst sie auf dem Tablet.");
        playAnimation(context, R.raw.raise_right_hand_b001);

        List<Integer> sequence = new ArrayList<>();
        for (int i = 0; i < config.startLength; i++) {
            sequence.add(random.nextInt(PAD_NAMES.length));
        }

        int completed = 0;
        long flashOn = config.flashOnMs;

        try {
            while (!aborted) {
                if (completed > 0) {
                    sequence.add(random.nextInt(PAD_NAMES.length));
                }

                int round = sequence.size();
                board.setStatus("Runde " + round + " – Schau zu!");
                board.setInputEnabled(false);
                sleep(700);

                for (Integer pad : sequence) {
                    if (aborted) {
                        break;
                    }
                    board.playPad(pad, flashOn);
                    sleep(flashOn + config.gapMs);
                }
                if (aborted) {
                    break;
                }

                touches.clear();
                board.setStatus("Du bist dran!");
                board.setInputEnabled(true);

                boolean correct = true;
                for (Integer expected : sequence) {
                    Integer pressed = awaitTouch(touches, config.inputTimeoutMs);
                    if (aborted || pressed == null || !pressed.equals(expected)) {
                        correct = false;
                        break;
                    }
                }
                board.setInputEnabled(false);

                if (aborted) {
                    break;
                }
                if (!correct) {
                    board.playErrorCue();
                    break;
                }

                completed = round;
                board.setScore(completed);
                celebrateRound(context, completed, board);
                flashOn = Math.max(config.minFlashMs, Math.round(flashOn * config.speedUpFactor));
                sleep(700);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Memory-Spiel beendet: " + e.getMessage());
        } finally {
            running = false;
            board.setOnPadListener(null);
        }

        if (aborted) {
            board.hide();
            return completed;
        }

        boolean record = MemoryHighscore.submit(context, config.label, completed);
        board.setStatus("Game Over!");
        board.setScore(completed);
        board.setHighscore(MemoryHighscore.get(context, config.label));
        board.setHint("Sag \"Memory\", um nochmal zu spielen");
        endGame(context, completed, record);
        sleep(4500);
        board.hide();
        return completed;
    }

    private void celebrateRound(QiContext context, int completed, MemoryGameView board) {
        board.playSuccessCue();
        if (completed % 4 == 0) {
            say(context, "Wahnsinn, " + completed + " Runden! Du bist ein Memory-Profi!");
            playAnimation(context, R.raw.pepper_highfive);
        } else {
            say(context, pickPraise(completed));
        }
    }

    private void endGame(QiContext context, int completed, boolean record) {
        if (completed <= 0) {
            say(context, "Kein Treffer diesmal, aber das schaffst du! Sag Memory, dann spielen wir nochmal.");
            return;
        }
        if (record) {
            say(context, "Neuer Rekord! Du hast " + completed
                    + " Runden geschafft. Das war fantastisch!");
            playAnimation(context, R.raw.pepper_highfive);
            return;
        }
        if (completed >= 8) {
            say(context, "Unglaublich! Du hast " + completed
                    + " Runden geschafft. Das ist ein Spitzenergebnis!");
            playAnimation(context, R.raw.pepper_highfive);
        } else {
            say(context, "Schade, da war ein Fehler. Du hast " + completed
                    + " Runden geschafft. Willst du es nochmal versuchen?");
        }
    }

    private String pickPraise(int completed) {
        String[] options = {
                "Super gemacht!",
                "Richtig! Weiter so!",
                "Perfekt gemerkt!",
                "Stark, das war " + completed + "!",
                "Genau richtig!"
        };
        return options[random.nextInt(options.length)];
    }

    private Integer awaitTouch(BlockingQueue<Integer> queue, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!aborted && System.currentTimeMillis() < deadline) {
            try {
                Integer value = queue.poll(200, TimeUnit.MILLISECONDS);
                if (value != null) {
                    return value;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private void playAnimation(QiContext context, int animationResource) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(animationResource)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            QiFutures.consume(animate.async().run(), TAG, "Animation");
        } catch (Exception e) {
            Log.w(TAG, "Animation fehlgeschlagen: " + e.getMessage());
        }
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "Sprachausgabe fehlgeschlagen: " + e.getMessage());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
