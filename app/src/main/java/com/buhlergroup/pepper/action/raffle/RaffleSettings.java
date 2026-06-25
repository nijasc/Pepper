package com.buhlergroup.pepper.action.raffle;

import android.content.Context;

public final class RaffleSettings {

    public static final int DEFAULT_RETENTION_DAYS = 30;

    private RaffleSettings() {
    }

    public static int getRetentionDays(Context context) {
        return DEFAULT_RETENTION_DAYS;
    }
}
