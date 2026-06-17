package com.buhlergroup.pepper.action.dance.audio;

import android.util.Log;

import java.io.File;

public final class SongAudioAnalyzer {

    private static final String TAG = "SongAudio";

    private static final int HOP = 512;
    private static final int MIN_BPM = 70;
    private static final int MAX_BPM = 180;
    private static final double HOOK_WINDOW_SECONDS = 4.0;
    private static final long MIN_REMAINING_MS = 12000;

    public static final class Result {
        public final int bpm;
        public final long hookStartMs;

        Result(int bpm, long hookStartMs) {
            this.bpm = bpm;
            this.hookStartMs = hookStartMs;
        }

        public boolean hasBpm() {
            return bpm > 0;
        }

        public boolean hasHook() {
            return hookStartMs >= 0;
        }
    }

    private SongAudioAnalyzer() {
    }

    public static Result analyze(File audioFile) {
        try {
            PreviewAudioDecoder.Pcm pcm = PreviewAudioDecoder.decode(audioFile);
            return analyze(pcm);
        } catch (Exception e) {
            Log.w(TAG, "Audio-Analyse fehlgeschlagen: " + e.getMessage());
            return null;
        }
    }

    static Result analyze(PreviewAudioDecoder.Pcm pcm) {
        if (pcm == null || pcm.samples.length < HOP * 4 || pcm.sampleRate <= 0) {
            return null;
        }
        double frameRate = (double) pcm.sampleRate / HOP;
        double[] energy = energyEnvelope(pcm.samples);
        double[] onset = onsetEnvelope(energy);

        int bpm = detectBpm(onset, frameRate);
        long hookStartMs = detectHookStart(energy, frameRate, pcm.durationMs());

        Log.i(TAG, "Analyse: bpm=" + bpm + " hookStartMs=" + hookStartMs
                + " (" + (pcm.durationMs() / 1000) + "s, " + pcm.sampleRate + "Hz)");
        return new Result(bpm, hookStartMs);
    }

    private static double[] energyEnvelope(float[] samples) {
        int frames = samples.length / HOP;
        double[] energy = new double[frames];
        for (int i = 0; i < frames; i++) {
            double sum = 0;
            int base = i * HOP;
            for (int j = 0; j < HOP; j++) {
                float s = samples[base + j];
                sum += s * s;
            }
            energy[i] = Math.sqrt(sum / HOP);
        }
        return energy;
    }

    private static double[] onsetEnvelope(double[] energy) {
        double[] onset = new double[energy.length];
        for (int i = 1; i < energy.length; i++) {
            double diff = energy[i] - energy[i - 1];
            onset[i] = diff > 0 ? diff : 0;
        }
        return onset;
    }

    private static int detectBpm(double[] onset, double frameRate) {
        if (onset.length < frameRate * 4) {
            return 0;
        }
        int minLag = (int) Math.round(frameRate * 60.0 / MAX_BPM);
        int maxLag = (int) Math.round(frameRate * 60.0 / MIN_BPM);
        minLag = Math.max(1, minLag);
        maxLag = Math.min(onset.length - 1, maxLag);
        if (maxLag <= minLag) {
            return 0;
        }

        double bestScore = 0;
        double meanScore = 0;
        int count = 0;
        int bestLag = -1;
        for (int lag = minLag; lag <= maxLag; lag++) {
            double acc = 0;
            for (int i = lag; i < onset.length; i++) {
                acc += onset[i] * onset[i - lag];
            }
            acc /= (onset.length - lag);
            meanScore += acc;
            count++;
            if (acc > bestScore) {
                bestScore = acc;
                bestLag = lag;
            }
        }
        if (bestLag <= 0 || count == 0) {
            return 0;
        }
        meanScore /= count;
        if (meanScore <= 0 || bestScore < meanScore * 1.30) {
            return 0;
        }

        double bpm = 60.0 * frameRate / bestLag;
        while (bpm < 85) {
            bpm *= 2;
        }
        while (bpm > 170) {
            bpm /= 2;
        }
        return (int) Math.max(MIN_BPM, Math.min(MAX_BPM, Math.round(bpm)));
    }

    private static long detectHookStart(double[] energy, double frameRate, long durationMs) {
        int windowFrames = (int) Math.round(HOOK_WINDOW_SECONDS * frameRate);
        if (windowFrames < 1 || energy.length <= windowFrames) {
            return -1;
        }
        long latestStartMs = durationMs - MIN_REMAINING_MS;
        if (latestStartMs <= 0) {
            return -1;
        }
        int latestStartFrame = (int) Math.min(energy.length - windowFrames,
                Math.round(latestStartMs / 1000.0 * frameRate));
        if (latestStartFrame < 0) {
            return -1;
        }

        double windowSum = 0;
        for (int i = 0; i < windowFrames; i++) {
            windowSum += energy[i];
        }
        double bestSum = windowSum;
        int bestStart = 0;
        for (int start = 1; start <= latestStartFrame; start++) {
            windowSum += energy[start + windowFrames - 1] - energy[start - 1];
            if (windowSum > bestSum) {
                bestSum = windowSum;
                bestStart = start;
            }
        }
        return Math.round(bestStart / frameRate * 1000.0);
    }
}
