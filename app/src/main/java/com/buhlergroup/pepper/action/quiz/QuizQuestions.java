package com.buhlergroup.pepper.action.quiz;

import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class QuizQuestions {

    private QuizQuestions() {
    }

    public static List<QuizQuestion> fallback(SupportedLanguage lang, int count) {
        List<QuizQuestion> all = new ArrayList<>(
                lang == SupportedLanguage.ENGLISH ? english() : german());
        Collections.shuffle(all);
        if (count > 0 && count < all.size()) {
            return new ArrayList<>(all.subList(0, count));
        }
        return all;
    }

    private static List<QuizQuestion> german() {
        return new ArrayList<>(Arrays.asList(
                new QuizQuestion("In welchem Land hat die Bühler Group ihren Hauptsitz?",
                        Arrays.asList("Schweiz", "Deutschland", "Österreich", "Schweden"), 0),
                new QuizQuestion("Wofür ist Bühler vor allem bekannt?",
                        Arrays.asList("Lebensmittelverarbeitung und Mahltechnik",
                                "Smartphones", "Automobilbau", "Modedesign"), 0),
                new QuizQuestion("In welchem Jahrhundert wurde Bühler gegründet?",
                        Arrays.asList("19. Jahrhundert", "21. Jahrhundert",
                                "17. Jahrhundert", "15. Jahrhundert"), 0),
                new QuizQuestion("Ein großer Teil des weltweit verarbeiteten Getreides läuft über "
                        + "Bühler-Anlagen. Welches Lebensmittel gehört dazu?",
                        Arrays.asList("Weizen", "Erdöl", "Gold", "Baumwolle"), 0),
                new QuizQuestion("Welcher Bereich gehört NICHT zum Kerngeschäft von Bühler?",
                        Arrays.asList("Soziale Netzwerke", "Mahltechnik",
                                "Druckguss", "Schokoladenherstellung"), 0),
                new QuizQuestion("Welche Art von Unternehmen ist Bühler?",
                        Arrays.asList("Ein Familienunternehmen", "Ein Staatsbetrieb",
                                "Ein Hedgefonds", "Ein Start-up"), 0),
                new QuizQuestion("Welches Studium passt typischerweise gut zu einer Karriere bei "
                        + "einem Industrieunternehmen wie Bühler?",
                        Arrays.asList("Maschinenbau", "Theaterwissenschaft",
                                "Kunstgeschichte", "Tourismus"), 0),
                new QuizQuestion("Was bietet Bühler jungen Menschen für den Berufseinstieg an?",
                        Arrays.asList("Ausbildungs- und Lehrstellen", "Nur unbezahlte Praktika",
                                "Ausschließlich Ferienjobs", "Gar nichts"), 0)));
    }

    private static List<QuizQuestion> english() {
        return new ArrayList<>(Arrays.asList(
                new QuizQuestion("In which country is the Bühler Group headquartered?",
                        Arrays.asList("Switzerland", "Germany", "Austria", "Sweden"), 0),
                new QuizQuestion("What is Bühler mainly known for?",
                        Arrays.asList("Food processing and grain milling",
                                "Smartphones", "Car manufacturing", "Fashion design"), 0),
                new QuizQuestion("In which century was Bühler founded?",
                        Arrays.asList("19th century", "21st century",
                                "17th century", "15th century"), 0),
                new QuizQuestion("A large share of the world's processed grain runs through Bühler "
                        + "equipment. Which food is part of that?",
                        Arrays.asList("Wheat", "Crude oil", "Gold", "Cotton"), 0),
                new QuizQuestion("Which area is NOT part of Bühler's core business?",
                        Arrays.asList("Social networks", "Grain milling",
                                "Die casting", "Chocolate production"), 0),
                new QuizQuestion("What kind of company is Bühler?",
                        Arrays.asList("A family-owned company", "A state-owned enterprise",
                                "A hedge fund", "A start-up"), 0),
                new QuizQuestion("Which field of study typically fits a career at an industrial "
                        + "company like Bühler?",
                        Arrays.asList("Mechanical engineering", "Theatre studies",
                                "Art history", "Tourism"), 0),
                new QuizQuestion("What does Bühler offer young people for starting their career?",
                        Arrays.asList("Apprenticeships and trainee positions",
                                "Only unpaid internships", "Only summer jobs", "Nothing at all"), 0)));
    }
}
