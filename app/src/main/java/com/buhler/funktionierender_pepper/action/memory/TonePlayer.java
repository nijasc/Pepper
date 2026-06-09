package com.buhler.funktionierender_pepper.action.memory;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public final class TonePlayer {

    private static final int SAMPLE_RATE = 44100;
    private static final double[] PAD_FREQUENCIES = {329.63, 261.63, 392.00, 196.00};

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
        new Thread(() -> {
            for (double note : notes) {
                emit(note, 140, 0.4);
                sleepQuietly(150);
            }
        }, "MemorySuccessTone").start();
    }

    public void playTone(double frequencyHz, long durationMs, double amplitude) {
        new Thread(() -> emit(frequencyHz, durationMs, amplitude), "MemoryTone").start();
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
        } catch (Exception ignored) {
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
