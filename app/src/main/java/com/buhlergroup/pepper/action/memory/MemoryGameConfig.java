package com.buhlergroup.pepper.action.memory;

public final class MemoryGameConfig {

    public final String label;
    public final int startLength;
    public final long flashOnMs;
    public final long gapMs;
    public final long minFlashMs;
    public final double speedUpFactor;
    public final long inputTimeoutMs;

    private MemoryGameConfig(String label, int startLength, long flashOnMs, long gapMs,
                            long minFlashMs, double speedUpFactor, long inputTimeoutMs) {
        this.label = label;
        this.startLength = startLength;
        this.flashOnMs = flashOnMs;
        this.gapMs = gapMs;
        this.minFlashMs = minFlashMs;
        this.speedUpFactor = speedUpFactor;
        this.inputTimeoutMs = inputTimeoutMs;
    }

    public static MemoryGameConfig easy() {
        return new MemoryGameConfig("leicht", 1, 700, 320, 380, 0.97, 9000);
    }

    public static MemoryGameConfig normal() {
        return new MemoryGameConfig("normal", 1, 560, 240, 280, 0.94, 7000);
    }

    public static MemoryGameConfig hard() {
        return new MemoryGameConfig("schwer", 2, 440, 170, 200, 0.90, 5500);
    }

    public static MemoryGameConfig fromInput(String input) {
        if (input == null) {
            return normal();
        }
        String lower = input.toLowerCase();
        if (lower.contains("leicht") || lower.contains("einfach") || lower.contains("easy")) {
            return easy();
        }
        if (lower.contains("schwer") || lower.contains("schwierig") || lower.contains("hard")
                || lower.contains("profi")) {
            return hard();
        }
        return normal();
    }
}
