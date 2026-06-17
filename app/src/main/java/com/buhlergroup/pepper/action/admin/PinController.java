package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.buhlergroup.pepper.R;

final class PinController {

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final long PIN_LOCKOUT_MS = 60000;

    interface OnUnlocked {
        void onUnlocked();
    }

    private final Context context;
    private final TextView pinDots;
    private final TextView pinError;
    private final OnUnlocked onUnlocked;
    private final StringBuilder entered = new StringBuilder();
    private int pinAttempts = 0;
    private long pinLockoutUntil = 0;

    PinController(View root, OnUnlocked onUnlocked) {
        this.context = root.getContext();
        this.onUnlocked = onUnlocked;
        this.pinDots = root.findViewById(R.id.adminPinDots);
        this.pinError = root.findViewById(R.id.adminPinError);
        wireKeypad(root);
    }

    void resetEntry() {
        entered.setLength(0);
        updateDots();
    }

    boolean isLocked() {
        return SystemClock.elapsedRealtime() < pinLockoutUntil;
    }

    void clearError() {
        pinError.setVisibility(View.INVISIBLE);
    }

    void showLockoutMessage() {
        long remaining = Math.max(0, pinLockoutUntil - SystemClock.elapsedRealtime());
        int seconds = (int) Math.ceil(remaining / 1000.0);
        pinError.setText(context.getString(R.string.admin_pin_locked, seconds));
        pinError.setVisibility(View.VISIBLE);
    }

    private void wireKeypad(View root) {
        int[] ids = {
                R.id.adminKey0, R.id.adminKey1, R.id.adminKey2, R.id.adminKey3, R.id.adminKey4,
                R.id.adminKey5, R.id.adminKey6, R.id.adminKey7, R.id.adminKey8, R.id.adminKey9
        };
        for (int d = 0; d <= 9; d++) {
            final int digit = d;
            root.findViewById(ids[d]).setOnClickListener(v -> onDigit(digit));
        }
        root.findViewById(R.id.adminKeyClear).setOnClickListener(v -> resetEntry());
        root.findViewById(R.id.adminKeyBackspace).setOnClickListener(v -> backspace());
    }

    private void onDigit(int digit) {
        if (isLocked()) {
            showLockoutMessage();
            return;
        }
        if (entered.length() >= 4) {
            return;
        }
        pinError.setVisibility(View.INVISIBLE);
        entered.append(digit);
        updateDots();
        if (entered.length() == 4) {
            checkPin();
        }
    }

    private void backspace() {
        if (entered.length() > 0) {
            entered.deleteCharAt(entered.length() - 1);
            updateDots();
        }
    }

    private void checkPin() {
        if (AdminSettings.getPin(context).contentEquals(entered)) {
            pinAttempts = 0;
            pinLockoutUntil = 0;
            resetEntry();
            onUnlocked.onUnlocked();
        } else {
            resetEntry();
            pinAttempts++;
            if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                pinAttempts = 0;
                pinLockoutUntil = SystemClock.elapsedRealtime() + PIN_LOCKOUT_MS;
                showLockoutMessage();
            } else {
                pinError.setText(R.string.admin_pin_error);
                pinError.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            dots.append(i < entered.length() ? "●" : "○");
            if (i < 3) {
                dots.append(' ');
            }
        }
        pinDots.setText(dots.toString());
    }
}
