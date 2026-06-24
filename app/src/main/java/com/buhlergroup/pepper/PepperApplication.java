package com.buhlergroup.pepper;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.buhlergroup.pepper.debug.DebugLog;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PepperApplication extends Application {

    private static final String TAG = "PepperApplication";
    private static final long RESTART_DELAY_MS = 1500;
    private static final String CRASH_FILE = "last_crash.txt";

    private static long startElapsedMs;

    public static long startElapsedMs() {
        return startElapsedMs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startElapsedMs = SystemClock.elapsedRealtime();
        DebugLog.get().init(this);
        replayLastCrash();
        PDFBoxResourceLoader.init(this);
        com.buhlergroup.pepper.llm.ModelSettings.ensureSeeded(this);
        installCrashRestart();
    }

    private void installCrashRestart() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception, scheduling app restart", throwable);
            try {
                persistCrash(thread, throwable);
            } catch (Exception e) {
                Log.w(TAG, "Crash persistence failed: " + e.getMessage());
            }
            try {
                scheduleRestart();
            } catch (Exception e) {
                Log.w(TAG, "Restart scheduling failed: " + e.getMessage());
            }
            Process.killProcess(Process.myPid());
            System.exit(2);
        });
    }

    private void persistCrash(Thread thread, Throwable throwable) {
        StringWriter trace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(trace));
        String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        String report = "Absturz " + stamp + " (Thread " + thread.getName() + ")\n" + trace;
        try (PrintWriter writer = new PrintWriter(new File(getFilesDir(), CRASH_FILE))) {
            writer.print(report);
        } catch (Exception e) {
            Log.w(TAG, "Could not write crash file: " + e.getMessage());
        }
    }

    private void replayLastCrash() {
        File file = new File(getFilesDir(), CRASH_FILE);
        if (!file.exists()) {
            return;
        }
        try {
            StringBuilder content = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }
            DebugLog.get().e(TAG, "Letzter Absturz (vor Neustart):\n" + content);
        } catch (Exception e) {
            Log.w(TAG, "Could not read crash file: " + e.getMessage());
        } finally {
            if (!file.delete()) {
                Log.w(TAG, "Could not delete crash file");
            }
        }
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
