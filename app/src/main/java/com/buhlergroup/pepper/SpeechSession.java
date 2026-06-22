package com.buhlergroup.pepper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.debug.DebugLog;

import java.util.ArrayList;
import java.util.List;

final class SpeechSession {

    private static final long WATCHDOG_INTERVAL_MS = 5000;
    private static final long WATCHDOG_IDLE_MS = 15000;
    private static final long RETRY_DELAY_MS = 800;

    interface Gate {
        boolean isListenSuppressed();

        boolean isBusy();

        void onTick();

        void onSpeechResult(String text);
    }

    private final Activity activity;
    private final int speechEvent;
    private final Gate gate;

    private Intent intent;
    private volatile boolean listening;
    private volatile boolean listenPending;
    private volatile long lastListenStartMs;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable speechWatchdog = new Runnable() {
        @Override
        public void run() {
            checkSpeechWatchdog();
            gate.onTick();
            mainHandler.postDelayed(this, WATCHDOG_INTERVAL_MS);
        }
    };

    SpeechSession(Activity activity, int speechEvent, Gate gate) {
        this.activity = activity;
        this.speechEvent = speechEvent;
        this.gate = gate;
    }

    void start() {
        checkPermission();
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        lastListenStartMs = SystemClock.elapsedRealtime();
        mainHandler.postDelayed(speechWatchdog, WATCHDOG_INTERVAL_MS);
    }

    Intent recognitionIntent() {
        return intent;
    }

    void destroy() {
        mainHandler.removeCallbacks(speechWatchdog);
        activity.runOnUiThread(() -> {
            if (listening) {
                listening = false;
                activity.finishActivity(speechEvent);
            }
        });
    }

    void listen() {
        if (gate.isListenSuppressed()) {
            listenPending = true;
            return;
        }
        listenPending = false;
        startSpeechRecognition();
    }

    void pause() {
        listenPending = true;
        activity.runOnUiThread(() -> {
            boolean wasListening = listening;
            listening = false;
            if (wasListening) {
                activity.finishActivity(speechEvent);
            }
        });
    }

    void resumeIfPending() {
        if (listenPending && !listening && !gate.isListenSuppressed()) {
            listen();
        }
    }

    void refreshListening() {
        if (gate.isListenSuppressed()) {
            pause();
        } else if (!listening) {
            listen();
        }
    }

    void requestDanceEdit(int requestCode) {
        if (intent != null && intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != speechEvent) {
            return false;
        }
        listening = false;
        lastListenStartMs = SystemClock.elapsedRealtime();
        String said = null;
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                said = results.get(0);
            }
        }
        if (said != null && !said.trim().isEmpty()) {
            DebugLog.get().setStatus("Erkannt: \"" + said + "\"");
            DebugLog.get().i("MainActivity", "Sprache erkannt: \"" + said + "\"");
            gate.onSpeechResult(said);
        } else {
            scheduleRetry();
        }
        return true;
    }

    private void startSpeechRecognition() {
        activity.runOnUiThread(() -> {
            if (listening || gate.isListenSuppressed()) {
                return;
            }
            if (intent == null || intent.resolveActivity(activity.getPackageManager()) == null) {
                Log.w("Mainactivity", "No speech recognition activity available");
                scheduleRetry();
                return;
            }
            listening = true;
            lastListenStartMs = SystemClock.elapsedRealtime();
            DebugLog.get().setStatus("Höre zu …");
            DebugLog.get().d("MainActivity", "Spracherkennung gestartet");
            try {
                activity.startActivityForResult(intent, speechEvent);
            } catch (Exception e) {
                listening = false;
                Log.w("Mainactivity", "startActivityForResult failed: " + e.getMessage());
                scheduleRetry();
            }
        });
    }

    private void scheduleRetry() {
        mainHandler.postDelayed(() -> {
            if (!listening && !gate.isListenSuppressed() && !gate.isBusy()) {
                listen();
            }
        }, RETRY_DELAY_MS);
    }

    private void checkSpeechWatchdog() {
        if (listening || gate.isBusy() || gate.isListenSuppressed()) {
            return;
        }
        long idle = SystemClock.elapsedRealtime() - lastListenStartMs;
        if (idle > WATCHDOG_IDLE_MS) {
            Log.w("Mainactivity", "Speech watchdog restarting recognition after " + idle + "ms idle");
            DebugLog.get().w("MainActivity", "Watchdog startet Spracherkennung neu nach " + idle + "ms");
            listen();
        }
    }

    private void checkPermission() {
        String[] required = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        List<String> missing = new ArrayList<>();
        for (String permission : required) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toArray(new String[0]), 1);
        }
    }
}
