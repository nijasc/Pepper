package com.buhlergroup.pepper.action.dynamicanim;

public final class SongResearch {
    public final String genre;
    public final String tempo;
    public final String signature;
    public final String mood;
    public final String era;
    public final String signatureMoves;
    public final String structure;

    SongResearch(String genre, String tempo, String signature, String mood,
                 String era, String signatureMoves, String structure) {
        this.genre = genre;
        this.tempo = tempo;
        this.signature = signature;
        this.mood = mood;
        this.era = era;
        this.signatureMoves = signatureMoves;
        this.structure = structure;
    }

    String brief() {
        StringBuilder sb = new StringBuilder();
        line(sb, "Genre", genre);
        line(sb, "Tempo/BPM", tempo);
        line(sb, "Time signature", signature);
        line(sb, "Mood", mood);
        line(sb, "Era", era);
        line(sb, "Signature/iconic dance moves", signatureMoves);
        line(sb, "Song structure / hook timing", structure);
        return sb.toString();
    }

    private static void line(StringBuilder sb, String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append("- ").append(label).append(": ").append(value.trim()).append('\n');
        }
    }
}
