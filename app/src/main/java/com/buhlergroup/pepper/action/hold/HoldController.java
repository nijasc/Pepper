package com.buhlergroup.pepper.action.hold;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.Locale;

public final class HoldController {

    private static final String TAG = "HoldMyBeer";
    private static final long LOOP_PAUSE_MS = 200;
    private static final long WAIT_FOR_OBJECT_MS = 15000;
    private static final long MAX_HOLD_MS = 10 * 60 * 1000;
    private static final long OVERTIME_PROMPT_INTERVAL_MS = 30000;
    private static final long[] ESCALATION_AT_MS = {60000, 180000, 300000};
    private static final String PREFS = "pepper_hold";
    private static final String KEY_HELD_COUNT = "beers_held";
    private static final HoldController INSTANCE = new HoldController();
    private final HoldQuotes quotes = new HoldQuotes();
    private volatile boolean sessionRunning;
    private volatile boolean objectConfirmed;
    private volatile boolean releaseRequested;
    private volatile int generation;
    private volatile Thread loopThread;
    private volatile HoldView view;
    private volatile HoldStateListener stateListener;

    private HoldController() {
    }

    public static HoldController get() {
        return INSTANCE;
    }

    public void attachView(HoldView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void setStateListener(HoldStateListener listener) {
        this.stateListener = listener;
    }

    public boolean isActive() {
        return sessionRunning;
    }

    public synchronized void requestHold(QiContext context) {
        if (sessionRunning) {
            sayQuietly(context, quotes.already(language() == SupportedLanguage.ENGLISH));
            return;
        }
        sessionRunning = true;
        objectConfirmed = false;
        releaseRequested = false;
        notifyState(true);
        final int gen = ++generation;
        loopThread = new Thread(() -> runSession(context, gen), "HoldMyBeerLoop");
        loopThread.start();
    }

    public void requestRelease() {
        releaseRequested = true;
    }

    public void confirmObject() {
        objectConfirmed = true;
    }

    public synchronized void stop() {
        endSession();
    }

    public synchronized void onFocusLost() {
        endSession();
    }

    private void endSession() {
        generation++;
        Thread t = loopThread;
        loopThread = null;
        if (t != null) {
            t.interrupt();
        }
        sessionRunning = false;
        hideView();
        notifyState(false);
    }

    private void runSession(QiContext context, int gen) {
        Holder holder = null;
        TouchSensor sensor = null;
        Future<ListenResult> listenFuture = null;
        SupportedLanguage lang = language();
        boolean english = lang == SupportedLanguage.ENGLISH;
        try {
            holder = HolderBuilder.with(context)
                    .withAutonomousAbilities(
                            AutonomousAbilitiesType.BASIC_AWARENESS,
                            AutonomousAbilitiesType.BACKGROUND_MOVEMENT)
                    .build();
            holder.hold();

            sayQuietly(context, quotes.accept(english));
            runAnimation(context, R.raw.hold_arm_raise);
            if (!alive(gen)) {
                return;
            }

            sensor = attachTouchSensor(context);
            showView(english ? "Place your drink on my open hand"
                            : "Stell dein Getränk auf meine offene Hand",
                    english ? "It's on the hand" : "Objekt liegt drauf", true);

            long waitStart = System.currentTimeMillis();
            while (alive(gen) && !objectConfirmed && !releaseRequested
                    && System.currentTimeMillis() - waitStart < WAIT_FOR_OBJECT_MS) {
                setTimer(formatRemaining(WAIT_FOR_OBJECT_MS
                        - (System.currentTimeMillis() - waitStart)));
                Thread.sleep(LOOP_PAUSE_MS);
            }
            if (!alive(gen)) {
                return;
            }
            if (!objectConfirmed) {
                sayQuietly(context, quotes.noObject(english));
                runAnimation(context, R.raw.hold_release);
                return;
            }

            runAnimation(context, R.raw.hold_hand_close);
            sayQuietly(context, quotes.confirm(english));
            showView(english ? "I'm holding your drink. Say \"stop\" or press STOP to get it back."
                            : "Ich halte dein Getränk. Sag \"Stopp\" oder drück STOP, um es zurückzubekommen.",
                    null, false);

            long holdStart = System.currentTimeMillis();
            boolean[] escalated = new boolean[ESCALATION_AT_MS.length];
            long lastOvertimePrompt = 0;
            while (alive(gen) && !releaseRequested) {
                long elapsed = System.currentTimeMillis() - holdStart;
                setTimer(formatElapsed(elapsed));
                if (listenFuture == null || listenFuture.isDone()) {
                    listenFuture = startStopListener(context);
                }
                for (int i = 0; i < ESCALATION_AT_MS.length; i++) {
                    if (!escalated[i] && elapsed >= ESCALATION_AT_MS[i]) {
                        escalated[i] = true;
                        sayQuietly(context, quotes.escalation(english, i));
                    }
                }
                if (elapsed >= MAX_HOLD_MS
                        && System.currentTimeMillis() - lastOvertimePrompt >= OVERTIME_PROMPT_INTERVAL_MS) {
                    lastOvertimePrompt = System.currentTimeMillis();
                    sayQuietly(context, quotes.overtime(english));
                }
                Thread.sleep(LOOP_PAUSE_MS);
            }
            if (!alive(gen)) {
                return;
            }

            showView(english ? "Careful — I'm letting go now!" : "Achtung – ich lasse jetzt los!",
                    null, false);
            sayQuietly(context, english
                    ? "Hold on tight. I'm letting go in five seconds."
                    : "Gut festhalten. Ich lasse in fünf Sekunden los.");
            for (int countdown = 5; countdown >= 1 && alive(gen); countdown--) {
                setTimer(String.valueOf(countdown));
                sayQuietly(context, String.valueOf(countdown));
                Thread.sleep(1000);
            }
            if (!alive(gen)) {
                return;
            }
            runAnimation(context, R.raw.hold_release);

            int count = incrementHeldCount(context);
            String bye = quotes.bye(english);
            if (count > 1) {
                bye += english
                        ? " That was drink number " + count + " for me."
                        : " Das war übrigens Getränk Nummer " + count + " für mich.";
            }
            sayQuietly(context, bye);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.w(TAG, "Hold session ended: " + e.getMessage());
        } finally {
            requestCancel(listenFuture);
            removeTouchSensor(sensor);
            releaseQuietly(holder);
            hideView();
            if (gen == generation) {
                sessionRunning = false;
                notifyState(false);
            }
        }
    }

    private boolean alive(int gen) {
        return gen == generation && sessionRunning && !Thread.currentThread().isInterrupted();
    }

    private TouchSensor attachTouchSensor(QiContext context) {
        try {
            TouchSensor sensor = context.getTouch().getSensor("RHand/Touch/Back");
            sensor.addOnStateChangedListener(state -> {
                if (state.getTouched()) {
                    objectConfirmed = true;
                }
            });
            return sensor;
        } catch (Exception e) {
            Log.w(TAG, "Touch sensor unavailable, tablet confirm only: " + e.getMessage());
            return null;
        }
    }

    private void removeTouchSensor(TouchSensor sensor) {
        if (sensor == null) {
            return;
        }
        try {
            sensor.removeAllOnStateChangedListeners();
        } catch (Exception ignored) {
        }
    }

    private Future<ListenResult> startStopListener(QiContext context) {
        try {
            PhraseSet phrases = PhraseSetBuilder.with(context)
                    .withTexts("stopp", "stop", "danke", "danke schön", "gib her", "fertig",
                            "thanks", "thank you", "give it back", "done")
                    .build();
            Listen listen = ListenBuilder.with(context)
                    .withPhraseSet(phrases)
                    .build();
            Future<ListenResult> future = listen.async().run();
            future.thenConsume(f -> {
                if (f.isCancelled() || f.hasError()) {
                    return;
                }
                ListenResult result = f.get();
                if (result != null && result.getHeardPhrase() != null
                        && !result.getHeardPhrase().getText().isEmpty()) {
                    requestRelease();
                }
            });
            return future;
        } catch (Exception e) {
            Log.w(TAG, "Stop listener failed: " + e.getMessage());
            return null;
        }
    }

    private void runAnimation(QiContext context, int resource) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(resource)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            animate.run();
        } catch (Exception e) {
            Log.w(TAG, "Hold animation failed: " + e.getMessage());
        }
    }

    private int incrementHeldCount(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int count = prefs.getInt(KEY_HELD_COUNT, 0) + 1;
            prefs.edit().putInt(KEY_HELD_COUNT, count).apply();
            return count;
        } catch (Exception e) {
            return 1;
        }
    }

    private SupportedLanguage language() {
        try {
            return SpeechManager.getInstance().currentLanguage();
        } catch (Exception e) {
            return SupportedLanguage.GERMAN;
        }
    }

    private String formatElapsed(long ms) {
        long seconds = ms / 1000;
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private String formatRemaining(long ms) {
        long seconds = Math.max(0, ms) / 1000;
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private void showView(String status, String confirmLabel, boolean confirmVisible) {
        HoldView current = view;
        if (current != null) {
            current.show(status, confirmLabel, confirmVisible);
        }
    }

    private void setTimer(String text) {
        HoldView current = view;
        if (current != null) {
            current.setTimer(text);
        }
    }

    private void hideView() {
        HoldView current = view;
        if (current != null) {
            current.hide();
        }
    }

    private void sayQuietly(QiContext context, String text) {
        try {
            SpeechManager.getInstance().sayStill(context, text);
        } catch (Exception e) {
            Log.w(TAG, "Say failed: " + e.getMessage());
        }
    }

    private void requestCancel(Future<?> future) {
        if (future != null && !future.isDone()) {
            future.requestCancellation();
        }
    }

    private void releaseQuietly(Holder holder) {
        if (holder == null) {
            return;
        }
        try {
            holder.release();
        } catch (Exception ignored) {
        }
    }

    private void notifyState(boolean active) {
        HoldStateListener listener = stateListener;
        if (listener != null) {
            listener.onHoldStateChanged(active);
        }
    }

    public interface HoldStateListener {
        void onHoldStateChanged(boolean active);
    }
}
