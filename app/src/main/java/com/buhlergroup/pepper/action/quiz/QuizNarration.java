package com.buhlergroup.pepper.action.quiz;

import com.buhlergroup.pepper.lang.SupportedLanguage;

/**
 * Produces the spoken/displayed narration lines for the quiz, localized by the
 * current {@link SupportedLanguage}.
 *
 * <p>These builders return the localized string only. The controller keeps the
 * {@code say()} calls, the game loop, scoring, timing, view interaction and the
 * raffle-offer logic.
 */
final class QuizNarration {

    private QuizNarration() {
    }

    static String tabletNotReady(SupportedLanguage lang) {
        return lang == SupportedLanguage.ENGLISH
                ? "My tablet is not ready, so I cannot run the quiz right now."
                : "Mein Tablet ist gerade nicht bereit, deshalb kann ich kein Quiz starten.";
    }

    static String alreadyPlaying(SupportedLanguage lang) {
        return lang == SupportedLanguage.ENGLISH
                ? "We are already playing a quiz!"
                : "Wir spielen doch schon ein Quiz!";
    }

    static String intro(SupportedLanguage lang) {
        return lang == SupportedLanguage.ENGLISH
                ? "Time for a little Bühler quiz! Tap the answer you think is correct on my tablet."
                : "Zeit für ein kleines Bühler-Quiz! Tippe die richtige Antwort auf meinem Tablet an.";
    }

    static String progress(SupportedLanguage lang, int current, int total) {
        return lang == SupportedLanguage.ENGLISH
                ? "Question " + current + " of " + total
                : "Frage " + current + " von " + total;
    }

    static String scoreText(SupportedLanguage lang, int score) {
        return lang == SupportedLanguage.ENGLISH ? "Score: " + score : "Punkte: " + score;
    }

    static String feedback(SupportedLanguage lang, boolean correct, boolean timedOut,
                           QuizQuestion question) {
        String correctOption = question.options.get(question.correctIndex);
        if (correct) {
            return lang == SupportedLanguage.ENGLISH
                    ? "Correct, well done!" : "Richtig, super gemacht!";
        } else if (timedOut) {
            return lang == SupportedLanguage.ENGLISH
                    ? "Time is up. The correct answer was: " + correctOption + "."
                    : "Die Zeit ist um. Richtig gewesen wäre: " + correctOption + ".";
        } else {
            return lang == SupportedLanguage.ENGLISH
                    ? "Not quite. The correct answer is: " + correctOption + "."
                    : "Leider falsch. Richtig ist: " + correctOption + ".";
        }
    }

    static String finalResult(SupportedLanguage lang, int score, int total) {
        String base = lang == SupportedLanguage.ENGLISH
                ? "You got " + score + " out of " + total + " questions right. "
                : "Du hast " + score + " von " + total + " Fragen richtig beantwortet. ";
        return base + closingComment(lang, score, total);
    }

    static String closingComment(SupportedLanguage lang, int score, int total) {
        boolean perfect = score == total;
        boolean good = score * 2 >= total;
        if (lang == SupportedLanguage.ENGLISH) {
            if (perfect) {
                return "Outstanding, you really know Bühler!";
            }
            return good ? "Nicely done, thanks for playing!"
                    : "Thanks for playing, come back and try again!";
        }
        if (perfect) {
            return "Hervorragend, du kennst dich bei Bühler richtig gut aus!";
        }
        return good ? "Gut gemacht, danke fürs Mitmachen!"
                : "Danke fürs Mitmachen, versuch es gern noch einmal!";
    }

    static String raffleOffer(SupportedLanguage lang) {
        return lang == SupportedLanguage.ENGLISH
                ? "Great result! Since you did so well, would you like to enter our raffle? "
                + "You can sign up right here on my tablet, or tap cancel."
                : "Tolles Ergebnis! Weil du so gut warst – möchtest du an unserer Verlosung "
                + "teilnehmen? Du kannst dich direkt auf meinem Tablet eintragen, "
                + "oder tippe auf Abbrechen.";
    }
}
