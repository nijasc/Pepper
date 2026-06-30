package com.buhlergroup.pepper.util;

public final class ThreadUtils {

    private ThreadUtils() {
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
