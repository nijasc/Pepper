package com.buhlergroup.pepper.action.dance;

import com.aldebaran.qi.sdk.QiContext;

public final class RobotContext {

    private static volatile QiContext qiContext;

    private RobotContext() {
    }

    public static void set(QiContext context) {
        qiContext = context;
    }

    public static void clear() {
        qiContext = null;
    }

    public static QiContext get() {
        return qiContext;
    }
}
