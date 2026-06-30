package com.buhlergroup.pepper.action.memory;

import java.util.Random;

/**
 * Produces the spoken narration lines for the memory game.
 *
 * <p>The memory game is German-only, so these builders return the German lines
 * for each game event. They return the string only; the controller keeps the
 * {@code say()} calls, game loop, scoring, timing and view interaction.
 */
final class MemoryGameNarration {

    private MemoryGameNarration() {
    }

    static String tabletNotReady() {
        return "Mein Tablet ist gerade nicht bereit, deshalb kann ich Memory nicht starten.";
    }

    static String alreadyPlaying() {
        return "Wir spielen doch schon!";
    }

    static String welcome(String label) {
        return "Willkommen bei Memory mit Bewegung auf " + label + "! "
                + "Ich zeige dir eine Folge aus Farben und Tönen, und du wiederholst sie auf dem Tablet.";
    }

    static String roundProfi(int completed) {
        return "Wahnsinn, " + completed + " Runden! Du bist ein Memory-Profi!";
    }

    static String pickPraise(Random random, int completed) {
        String[] options = {
                "Super gemacht!",
                "Richtig! Weiter so!",
                "Perfekt gemerkt!",
                "Stark, das war " + completed + "!",
                "Genau richtig!"
        };
        return options[random.nextInt(options.length)];
    }

    static String endNoHit() {
        return "Kein Treffer diesmal, aber das schaffst du! Sag Memory, dann spielen wir nochmal.";
    }

    static String endRecord(int completed) {
        return "Neuer Rekord! Du hast " + completed
                + " Runden geschafft. Das war fantastisch!";
    }

    static String endTopResult(int completed) {
        return "Unglaublich! Du hast " + completed
                + " Runden geschafft. Das ist ein Spitzenergebnis!";
    }

    static String endRetry(int completed) {
        return "Schade, da war ein Fehler. Du hast " + completed
                + " Runden geschafft. Willst du es nochmal versuchen?";
    }
}
