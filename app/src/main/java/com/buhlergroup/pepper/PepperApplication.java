package com.buhlergroup.pepper;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

public class PepperApplication extends Application {

    private static final String TAG = "PepperApplication";
    private static final long RESTART_DELAY_MS = 1500;

    private static long startElapsedMs;

    @Override
    public void onCreate() {
        super.onCreate();
        startElapsedMs = SystemClock.elapsedRealtime();
        installCrashRestart();
    }

    public static long startElapsedMs() {
        return startElapsedMs;
    }

    private void installCrashRestart() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception, scheduling app restart", throwable);
            try {
                scheduleRestart();
            } catch (Exception e) {
                Log.w(TAG, "Restart scheduling failed: " + e.getMessage());
            }
            Process.killProcess(Process.myPid());
            System.exit(2);
        });
    }

    private void scheduleRestart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + RESTART_DELAY_MS, pending);
        }
    }
}
