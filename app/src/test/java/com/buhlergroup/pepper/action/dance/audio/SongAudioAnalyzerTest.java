package com.buhlergroup.pepper.action.dance.audio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SongAudioAnalyzerTest {

    private static final int SAMPLE_RATE = 22050;

    @Test
    public void detectsTempoAndHookOnSyntheticTrack() {
        int bpm = 120;
        double seconds = 24.0;
        int total = (int) (SAMPLE_RATE * seconds);
        float[] samples = new float[total];

        int beatSamples = (int) (SAMPLE_RATE * 60.0 / bpm);
        for (int beat = 0; beat * beatSamples < total; beat++) {
            int base = beat * beatSamples;
            for (int k = 0; k < 8 && base + k < total; k++) {
                samples[base + k] += 0.8f * (1f - k / 8f);
            }
        }

        int hookStart = 6 * SAMPLE_RATE;
        int hookEnd = 10 * SAMPLE_RATE;
        for (int i = hookStart; i < hookEnd && i < total; i++) {
            double t = (double) i / SAMPLE_RATE;
            samples[i] += 0.5 * Math.sin(2 * Math.PI * 220 * t);
        }

        PreviewAudioDecoder.Pcm pcm = new PreviewAudioDecoder.Pcm(samples, SAMPLE_RATE);
        SongAudioAnalyzer.Result result = SongAudioAnalyzer.analyze(pcm);

        assertNotNull(result);
        assertTrue("bpm was " + result.bpm, result.bpm >= 108 && result.bpm <= 132);
        assertTrue("hookStartMs was " + result.hookStartMs,
                result.hookStartMs >= 4000 && result.hookStartMs <= 9000);
    }

    @Test
    public void returnsNullForTooShortAudio() {
        float[] samples = new float[64];
        PreviewAudioDecoder.Pcm pcm = new PreviewAudioDecoder.Pcm(samples, SAMPLE_RATE);
        assertTrue(SongAudioAnalyzer.analyze(pcm) == null);
    }

    @Test
    public void beatFrameStepLocksToTempo() {
        assertTrue(com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator.beatFrameStep(0) == 0);
        int step120 = com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator.beatFrameStep(120);
        assertTrue("step120 was " + step120, step120 >= 10 && step120 <= 16);
        int step170 = com.buhlergroup.pepper.action.dynamicanim.AnimationGenerator.beatFrameStep(170);
        assertTrue("step170 was " + step170, step170 >= 10 && step170 <= 25);
    }
}
