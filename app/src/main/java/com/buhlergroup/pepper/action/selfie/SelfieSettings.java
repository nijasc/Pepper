package com.buhlergroup.pepper.action.selfie;

import android.content.Context;

public final class SelfieSettings {

    public static final int DEFAULT_RETENTION_DAYS = 14;

    private SelfieSettings() {
    }

    public static int getRetentionDays(Context context) {
        return DEFAULT_RETENTION_DAYS;
    }
}
