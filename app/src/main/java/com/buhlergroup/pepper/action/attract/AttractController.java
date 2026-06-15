package com.buhlergroup.pepper.action.attract;

import android.os.SystemClock;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;

public final class AttractController {

    private static final String TAG = "AttractController";
    private static final long IDLE_MS = 120000;

    private static final AttractController INSTANCE = new AttractController();

    private volatile long lastInteractionMs = SystemClock.elapsedRealtime();
    private volatile boolean active;

    private AttractController() {
    }

    public static AttractController get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active;
    }

    public void notifyInteraction() {
        lastInteractionMs = SystemClock.elapsedRealtime();
        if (active) {
            stopAttract();
        }
    }

    public void tick(QiContext context, boolean overlayOpen, boolean busy) {
        if (overlayOpen || busy) {
            lastInteractionMs = SystemClock.elapsedRealtime();
            if (active) {
                stopAttract();
            }
            return;
        }
        if (!active && SystemClock.elapsedRealtime() - lastInteractionMs > IDLE_MS) {
            startAttract();
        }
    }

    private void startAttract() {
        active = true;
        Log.i(TAG, "Attract mode activated after idle period");
    }

    private void stopAttract() {
        active = false;
        Log.i(TAG, "Attract mode deactivated");
    }
}
