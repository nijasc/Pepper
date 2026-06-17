package com.buhlergroup.pepper.openai;

import android.util.Log;

final class OpenAiCircuitBreaker {

    private static final String TAG = "OpenAIService";
    private static final int FAILURE_THRESHOLD = 3;
    private static final long COOLDOWN_MS = 30000;

    private int consecutiveFailures;
    private long circuitOpenUntilMs;

    synchronized boolean isOpen() {
        return System.currentTimeMillis() < circuitOpenUntilMs;
    }

    synchronized void recordSuccess() {
        consecutiveFailures = 0;
        circuitOpenUntilMs = 0;
    }

    synchronized void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= FAILURE_THRESHOLD) {
            circuitOpenUntilMs = System.currentTimeMillis() + COOLDOWN_MS;
            Log.w(TAG, "OpenAI circuit opened after " + consecutiveFailures
                    + " consecutive failures; failing fast for " + COOLDOWN_MS + "ms");
        }
    }
}
