package com.buhlergroup.pepper.action.memory;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TonePlayer {

    private static final String TAG = "TonePlayer";
    private static final int SAMPLE_RATE = 44100;
    private static final double[] PAD_FREQUENCIES = {329.63, 261.63, 392.00, 196.00};

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MemoryTone");
        t.setDaemon(true);
        return t;
    });

    public void playPadTone(int padIndex, long durationMs) {
        if (padIndex < 0 || padIndex >= PAD_FREQUENCIES.length) {
            return;
        }
        long clamped = Math.max(160, Math.min(durationMs, 700));
        playTone(PAD_FREQUENCIES[padIndex], clamped, 0.45);
    }

    public void playError() {
        playTone(110.0, 450, 0.5);
    }

    public void playSuccess() {
        final double[] notes = {523.25, 659.25, 783.99};
        executor.execute(() -> {
            for (double note : notes) {
                emit(note, 140, 0.4);
                sleepQuietly(150);
            }
        });
    }

    private void playTone(double frequencyHz, long durationMs, double amplitude) {
        executor.execute(() -> emit(frequencyHz, durationMs, amplitude));
    }

    private void emit(double frequencyHz, long durationMs, double amplitude) {
        int sampleCount = (int) (SAMPLE_RATE * durationMs / 1000.0);
        if (sampleCount <= 0) {
            return;
        }
        short[] buffer = new short[sampleCount];
        int fade = Math.min(sampleCount / 8, SAMPLE_RATE / 200);
        for (int i = 0; i < sampleCount; i++) {
            double envelope = 1.0;
            if (i < fade) {
                envelope = (double) i / fade;
            } else if (i > sampleCount - fade) {
                envelope = (double) (sampleCount - i) / fade;
            }
            double angle = 2.0 * Math.PI * i * frequencyHz / SAMPLE_RATE;
            buffer[i] = (short) (Math.sin(angle) * Short.MAX_VALUE * amplitude * envelope);
        }

        AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                sampleCount * 2,
                AudioTrack.MODE_STATIC);

        try {
            track.write(buffer, 0, sampleCount);
            track.play();
            sleepQuietly(durationMs + 60);
        } catch (Exception e) {
            Log.d(TAG, "emit failed", e);
        } finally {
            try {
                track.stop();
            } catch (Exception ignored) {
            }
            track.release();
        }
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
