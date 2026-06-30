package com.buhlergroup.pepper.util;

import com.aldebaran.qi.Future;

public final class FutureUtils {

    private FutureUtils() {
    }

    public static void cancel(Future<?> f) {
        if (f != null && !f.isDone()) {
            f.requestCancellation();
        }
    }
}
