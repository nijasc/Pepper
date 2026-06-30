package com.buhlergroup.pepper.llm;

import android.util.Log;

public final class OpenAiCircuitBreaker {

    private static final String TAG = "OpenAiCircuitBreaker";
    private static final int FAILURE_THRESHOLD = 3;
    private static final long COOLDOWN_MS = 30000;

    private int consecutiveFailures;
    private long circuitOpenUntilMs;

    public synchronized boolean isOpen() {
        return System.currentTimeMillis() < circuitOpenUntilMs;
    }

    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        circuitOpenUntilMs = 0;
    }

    public synchronized void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= FAILURE_THRESHOLD) {
            circuitOpenUntilMs = System.currentTimeMillis() + COOLDOWN_MS;
            Log.w(TAG, "OpenAI circuit opened after " + consecutiveFailures
                    + " consecutive failures; failing fast for " + COOLDOWN_MS + "ms");
        }
    }
}
