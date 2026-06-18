package com.buhlergroup.pepper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognitionListener;
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
    private static final long RETRY_DELAY_MS = 800;

    interface Gate {
        boolean isListenSuppressed();

        boolean isBusy();

        void onTick();

        void onSpeechResult(String text);
    }

    private final Activity activity;
    private final Gate gate;

    private SpeechRecognizer recognizer;
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

    SpeechSession(Activity activity, Gate gate) {
        this.activity = activity;
        this.gate = gate;
    }

    void start() {
        checkPermission();
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        activity.runOnUiThread(this::ensureRecognizer);
        lastListenStartMs = SystemClock.elapsedRealtime();
        mainHandler.postDelayed(speechWatchdog, WATCHDOG_INTERVAL_MS);
    }

    private void ensureRecognizer() {
        if (recognizer != null) {
            return;
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        recognizer.setRecognitionListener(new Listener());
    }

    Intent recognitionIntent() {
        return intent;
    }

    void destroy() {
        mainHandler.removeCallbacks(speechWatchdog);
        activity.runOnUiThread(() -> {
            if (recognizer != null) {
                recognizer.destroy();
                recognizer = null;
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
            listening = false;
            try {
                if (recognizer != null) {
                    recognizer.cancel();
                }
            } catch (Exception ignored) {
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

    private void startSpeechRecognition() {
        activity.runOnUiThread(() -> {
            if (listening || gate.isListenSuppressed()) {
                return;
            }
            ensureRecognizer();
            listening = true;
            lastListenStartMs = SystemClock.elapsedRealtime();
            DebugLog.get().setStatus("Höre zu …");
            DebugLog.get().d("MainActivity", "Spracherkennung gestartet");
            try {
                recognizer.startListening(intent);
            } catch (Exception e) {
                listening = false;
                Log.w("Mainactivity", "startListening failed: " + e.getMessage());
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

    private final class Listener implements RecognitionListener {
        @Override
        public void onResults(Bundle results) {
            listening = false;
            lastListenStartMs = SystemClock.elapsedRealtime();
            String said = null;
            if (results != null) {
                ArrayList<String> list =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    said = list.get(0);
                }
            }
            if (said != null && !said.trim().isEmpty()) {
                DebugLog.get().setStatus("Erkannt: \"" + said + "\"");
                DebugLog.get().i("MainActivity", "Sprache erkannt: \"" + said + "\"");
                gate.onSpeechResult(said);
            } else {
                scheduleRetry();
            }
        }

        @Override
        public void onError(int error) {
            listening = false;
            lastListenStartMs = SystemClock.elapsedRealtime();
            scheduleRetry();
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
