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
import com.buhlergroup.pepper.action.QiFutures;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.Locale;
import java.util.Random;

public final class HoldController {

    public interface HoldStateListener {
        void onHoldStateChanged(boolean active);
    }

    private static final String TAG = "HoldMyBeer";
    private static final long LOOP_PAUSE_MS = 200;
    private static final long WAIT_FOR_OBJECT_MS = 30000;
    private static final long MAX_HOLD_MS = 10 * 60 * 1000;
    private static final long OVERTIME_PROMPT_INTERVAL_MS = 30000;
    private static final long[] ESCALATION_AT_MS = {60000, 180000, 300000};
    private static final String PREFS = "pepper_hold";
    private static final String KEY_HELD_COUNT = "beers_held";

    private static final String[] ALREADY_DE = {
            "Ich halte doch schon etwas. Zwei Sachen gleichzeitig gehen nicht.",
            "Eine Hand, ein Getränk. Mehr geht gerade nicht."
    };
    private static final String[] ALREADY_EN = {
            "I'm already holding something. One drink at a time.",
            "One hand, one drink. That's all I can do right now."
    };
    private static final String[] ACCEPT_DE = {
            "Na klar, her damit! Ich habe sowieso gerade frei.",
            "Halt mal dein Bier? Kein Problem, ich verschütte garantiert nichts.",
            "Okay! Stell es auf meine Hand, ich passe auf."
    };
    private static final String[] ACCEPT_EN = {
            "Sure, hand it over! I'm free anyway.",
            "Hold your beer? No problem, I never spill.",
            "Okay! Place it on my hand, I'll watch it."
    };
    private static final String[] CONFIRM_DE = {
            "Hab's! Sicher verwahrt.",
            "Festgehalten. Geh ruhig, ich passe auf wie ein Schweizer Uhrwerk.",
            "Dein Getränk ist bei mir in der besten Hand."
    };
    private static final String[] CONFIRM_EN = {
            "Got it! Safe and sound.",
            "Holding tight. Go ahead, I'll keep watch.",
            "Your drink is in good hands. Well, in one good hand."
    };
    private static final String[][] ESCALATION_DE = {
            {
                    "Dein Bier wird langsam warm…",
                    "Nur zur Info: dein Getränk steht hier immer noch."
            },
            {
                    "Ich kriege zwar keinen Muskelkater, aber langsam wird es einsam hier.",
                    "Drei Minuten. Ich fange gleich an, Selbstgespräche mit dem Getränk zu führen."
            },
            {
                    "Ein Roboter, allein mit einem Getränk. Soll das mein Leben sein? Hol es bitte ab!",
                    "Fünf Minuten! Ich erwäge, das Getränk als Dienstaufwand abzurechnen."
            }
    };
    private static final String[][] ESCALATION_EN = {
            {
                    "Your beer is getting warm…",
                    "Just so you know: your drink is still here."
            },
            {
                    "I can't get sore muscles, but it's getting lonely over here.",
                    "Three minutes. I'm about to start talking to the drink."
            },
            {
                    "A robot, alone with a drink. Is this my life now? Please come get it!",
                    "Five minutes! I'm considering charging a holding fee."
            }
    };
    private static final String[] OVERTIME_DE = {
            "Meine maximale Haltezeit ist um. Bitte nimm dein Getränk jetzt ab!",
            "Schichtende! Bitte hol dein Getränk ab, ich lasse es nicht einfach fallen."
    };
    private static final String[] OVERTIME_EN = {
            "My maximum holding time is up. Please take your drink now!",
            "Shift's over! Please come get your drink, I won't just drop it."
    };
    private static final String[] NO_OBJECT_DE = {
            "Niemand gibt mir etwas. Dann eben nicht.",
            "Das Angebot ist abgelaufen. Hand wieder runter."
    };
    private static final String[] NO_OBJECT_EN = {
            "Nobody is giving me anything. Fine then.",
            "Offer expired. Hand goes back down."
    };
    private static final String[] BYE_DE = {
            "Bitte sehr! Trinkgeld nehme ich in Watt.",
            "Gern geschehen. Prost!",
            "Bitte schön. Für einen Roboter war das Schwerstarbeit."
    };
    private static final String[] BYE_EN = {
            "There you go! I accept tips in watts.",
            "You're welcome. Cheers!",
            "Here you are. Heavy lifting, for a robot."
    };

    private static final HoldController INSTANCE = new HoldController();

    private final Random random = new Random();
    private volatile boolean sessionRunning;
    private volatile boolean objectConfirmed;
    private volatile boolean releaseRequested;
    private volatile int generation;
    private volatile Thread loopThread;
    private volatile HoldView view;
    private volatile HoldStateListener stateListener;
    private int lastPick = -1;

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
            sayQuietly(context, pick(language() == SupportedLanguage.ENGLISH ? ALREADY_EN : ALREADY_DE));
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
        Future<Void> poseFuture = null;
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

            sayQuietly(context, pick(english ? ACCEPT_EN : ACCEPT_DE));
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
                sayQuietly(context, pick(english ? NO_OBJECT_EN : NO_OBJECT_DE));
                runAnimation(context, R.raw.hold_release);
                return;
            }

            runAnimation(context, R.raw.hold_hand_close);
            sayQuietly(context, pick(english ? CONFIRM_EN : CONFIRM_DE));
            showView(english ? "I'm holding your drink. Say \"stop\" or press STOP to get it back."
                            : "Ich halte dein Getränk. Sag \"Stopp\" oder drück STOP, um es zurückzubekommen.",
                    null, false);

            long holdStart = System.currentTimeMillis();
            boolean[] escalated = new boolean[ESCALATION_AT_MS.length];
            long lastOvertimePrompt = 0;
            while (alive(gen) && !releaseRequested) {
                long elapsed = System.currentTimeMillis() - holdStart;
                setTimer(formatElapsed(elapsed));
                if (poseFuture == null || poseFuture.isDone()) {
                    poseFuture = startPoseLoop(context);
                }
                if (listenFuture == null || listenFuture.isDone()) {
                    listenFuture = startStopListener(context);
                }
                for (int i = 0; i < ESCALATION_AT_MS.length; i++) {
                    if (!escalated[i] && elapsed >= ESCALATION_AT_MS[i]) {
                        escalated[i] = true;
                        String[] pool = english ? ESCALATION_EN[i] : ESCALATION_DE[i];
                        sayQuietly(context, pick(pool));
                    }
                }
                if (elapsed >= MAX_HOLD_MS
                        && System.currentTimeMillis() - lastOvertimePrompt >= OVERTIME_PROMPT_INTERVAL_MS) {
                    lastOvertimePrompt = System.currentTimeMillis();
                    sayQuietly(context, pick(english ? OVERTIME_EN : OVERTIME_DE));
                }
                Thread.sleep(LOOP_PAUSE_MS);
            }
            if (!alive(gen)) {
                return;
            }

            requestCancel(poseFuture);
            poseFuture = null;
            sayQuietly(context, english
                    ? "Hold on tight. I'm letting go in 3, 2, 1."
                    : "Gut festhalten. Ich lasse los in 3, 2, 1.");
            runAnimation(context, R.raw.hold_release);

            int count = incrementHeldCount(context);
            String bye = pick(english ? BYE_EN : BYE_DE);
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
            requestCancel(poseFuture);
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

    private Future<Void> startPoseLoop(QiContext context) {
        try {
            Animation animation = AnimationBuilder.with(context)
                    .withResources(R.raw.hold_pose_loop)
                    .build();
            Animate animate = AnimateBuilder.with(context)
                    .withAnimation(animation)
                    .build();
            Future<Void> future = animate.async().run();
            QiFutures.consume(future, TAG, "hold pose loop");
            return future;
        } catch (Exception e) {
            Log.w(TAG, "Hold pose loop failed: " + e.getMessage());
            return null;
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

    private String pick(String[] pool) {
        int index;
        do {
            index = random.nextInt(pool.length);
        } while (pool.length > 1 && index == lastPick);
        lastPick = index;
        return pool[index];
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
            SpeechManager.getInstance().say(context, text);
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
}
