package com.buhlergroup.pepper.action;

import android.util.Log;

import com.aldebaran.qi.Future;

public final class QiFutures {

    private QiFutures() {
    }

    public static <T> void consume(Future<T> future, String tag, String label) {
        if (future == null) {
            return;
        }
        future.thenConsume(done -> {
            if (done.hasError()) {
                Log.w(tag, label + " did not finish: " + done.getError().getMessage());
            }
        });
    }
}
