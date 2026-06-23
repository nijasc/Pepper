package com.buhlergroup.pepper.action.dance.audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public final class PreviewAudioDecoder {

    private static final long DEQUEUE_TIMEOUT_US = 10000;
    private static final int MAX_SAMPLES = 60 * 48000;

    private PreviewAudioDecoder() {
    }

    public static Pcm decode(File file) throws Exception {
        if (file == null || !file.exists() || file.length() == 0) {
            throw new IllegalArgumentException("Audiodatei fehlt oder ist leer.");
        }
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec codec = null;
        try {
            extractor.setDataSource(file.getAbsolutePath());
            int trackIndex = selectAudioTrack(extractor);
            if (trackIndex < 0) {
                throw new IllegalStateException("Keine Audiospur gefunden.");
            }
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)
                    ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 1;

            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            List<float[]> chunks = new ArrayList<>();
            int totalSamples = 0;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            boolean outputDone = false;

            while (!outputDone) {
                if (!inputDone) {
                    int inIndex = codec.dequeueInputBuffer(DEQUEUE_TIMEOUT_US);
                    if (inIndex >= 0) {
                        ByteBuffer inBuffer = codec.getInputBuffer(inIndex);
                        int size = inBuffer == null ? -1 : extractor.readSampleData(inBuffer, 0);
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = codec.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT_US);
                if (outIndex >= 0) {
                    if (info.size > 0) {
                        ByteBuffer outBuffer = codec.getOutputBuffer(outIndex);
                        if (outBuffer != null) {
                            outBuffer.position(info.offset);
                            outBuffer.limit(info.offset + info.size);
                            float[] mono = toMono(outBuffer, channelCount);
                            if (mono.length > 0) {
                                chunks.add(mono);
                                totalSamples += mono.length;
                            }
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                            || totalSamples >= MAX_SAMPLES) {
                        outputDone = true;
                    }
                } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outFormat = codec.getOutputFormat();
                    if (outFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    }
                    if (outFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        channelCount = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    }
                }
            }

            if (totalSamples == 0) {
                throw new IllegalStateException("Audio konnte nicht dekodiert werden (0 Samples).");
            }
            return new Pcm(concat(chunks, totalSamples), sampleRate);
        } finally {
            if (codec != null) {
                try {
                    codec.stop();
                } catch (Exception ignored) {
                }
                try {
                    codec.release();
                } catch (Exception ignored) {
                }
            }
            try {
                extractor.release();
            } catch (Exception ignored) {
            }
        }
    }

    private static int selectAudioTrack(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    private static float[] toMono(ByteBuffer buffer, int channelCount) {
        ShortBuffer shorts = buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        int totalShorts = shorts.remaining();
        int channels = Math.max(1, channelCount);
        int frames = totalShorts / channels;
        float[] mono = new float[frames];
        for (int frame = 0; frame < frames; frame++) {
            int acc = 0;
            for (int c = 0; c < channels; c++) {
                acc += shorts.get(frame * channels + c);
            }
            mono[frame] = (acc / channels) / 32768f;
        }
        return mono;
    }

    private static float[] concat(List<float[]> chunks, int totalSamples) {
        float[] out = new float[totalSamples];
        int offset = 0;
        for (float[] chunk : chunks) {
            System.arraycopy(chunk, 0, out, offset, chunk.length);
            offset += chunk.length;
        }
        return out;
    }

    public static final class Pcm {
        public final float[] samples;
        public final int sampleRate;

        Pcm(float[] samples, int sampleRate) {
            this.samples = samples;
            this.sampleRate = sampleRate;
        }

        public long durationMs() {
            if (sampleRate <= 0) {
                return 0;
            }
            return (long) samples.length * 1000L / sampleRate;
        }
    }
}
