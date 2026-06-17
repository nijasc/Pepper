package com.buhlergroup.pepper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.debug.DebugLog;

import java.util.ArrayList;
import java.util.List;

final class SpeechSession {

    private static final long WATCHDOG_INTERVAL_MS = 5000;
    private static final long WATCHDOG_IDLE_MS = 15000;

    interface Gate {
        boolean isOverlayOpen();

        boolean isBusy();

        void onTick();
    }

    private final Activity activity;
    private final int speechEvent;
    private final Gate gate;

    private SpeechRecognizer recognizer;
    private Intent intent;
    private volatile boolean listening;
    private volatile boolean listenPending;
    private volatile long lastListenStartMs;

    private final Handler watchdogHandler = new Handler(Looper.getMainLooper());
    private final Runnable speechWatchdog = new Runnable() {
        @Override
        public void run() {
            checkSpeechWatchdog();
            gate.onTick();
            watchdogHandler.postDelayed(this, WATCHDOG_INTERVAL_MS);
        }
    };

    SpeechSession(Activity activity, int speechEvent, Gate gate) {
        this.activity = activity;
        this.speechEvent = speechEvent;
        this.gate = gate;
    }

    void start() {
        checkPermission();
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        lastListenStartMs = SystemClock.elapsedRealtime();
        watchdogHandler.postDelayed(speechWatchdog, WATCHDOG_INTERVAL_MS);
    }

    Intent recognitionIntent() {
        return intent;
    }

    void destroy() {
        watchdogHandler.removeCallbacks(speechWatchdog);
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
        }
    }

    void listen() {
        if (gate.isOverlayOpen()) {
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
            try {
                recognizer.cancel();
            } catch (Exception ignored) {
            }
        });
    }

    void resumeIfPending() {
        if (listenPending && !listening && !gate.isOverlayOpen()) {
            listen();
        }
    }

    void requestDanceEdit(int requestCode) {
        if (intent != null && intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    String handleSpeechResult(int resultCode, Intent data) {
        listening = false;
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String said = results.get(0);
                DebugLog.get().setStatus("Erkannt: \"" + said + "\"");
                DebugLog.get().i("MainActivity", "Sprache erkannt: \"" + said + "\"");
                return said;
            }
        }
        return null;
    }

    private void startSpeechRecognition() {
        activity.runOnUiThread(() -> {
            if (listening || gate.isOverlayOpen()) {
                return;
            }
            if (intent != null && intent.resolveActivity(activity.getPackageManager()) != null) {
                listening = true;
                lastListenStartMs = SystemClock.elapsedRealtime();
                DebugLog.get().setStatus("Höre zu …");
                DebugLog.get().d("MainActivity", "Spracherkennung gestartet");
                activity.startActivityForResult(intent, speechEvent);
            }
        });
    }

    private void checkSpeechWatchdog() {
        if (listening || gate.isBusy() || gate.isOverlayOpen()) {
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
