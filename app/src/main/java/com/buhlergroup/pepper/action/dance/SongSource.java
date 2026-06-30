package com.buhlergroup.pepper.action.dance;

/** Resolved song metadata used to seed a dance (id, title, preview URL, duration). */
final class SongSource {
    final String sourceId;
    final String title;
    final String previewUrl;
    final long durationMs;

    SongSource(String sourceId, String title, String previewUrl, long durationMs) {
        this.sourceId = sourceId;
        this.title = title;
        this.previewUrl = previewUrl;
        this.durationMs = durationMs;
    }
}
