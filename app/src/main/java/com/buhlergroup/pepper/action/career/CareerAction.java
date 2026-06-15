package com.buhlergroup.pepper.action.career;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.config.Env;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

public class CareerAction extends Action {

    private static final String CAREER_URL_KEY = "PEPPER_CAREER_URL";

    @Override
    public void execute(QiContext context, String input) {
        SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
        String url = Env.get(context, CAREER_URL_KEY, "").trim();
        SpeechManager.getInstance().systemSay(context, answer(lang, !url.isEmpty()));
    }

    private String answer(SupportedLanguage lang, boolean hasQr) {
        if (lang == SupportedLanguage.ENGLISH) {
            String base = "Bühler offers a lot for your career: apprenticeships and trainee "
                    + "programmes, internships and student projects, plus many engineering and "
                    + "business roles around the world. Whether you are just starting out or already "
                    + "experienced, there is likely a path for you.";
            return hasQr
                    ? base + " Scan the QR code on my tablet to open our careers page."
                    : base;
        }
        String base = "Bühler bietet viel für deine Karriere: Lehrstellen und Trainee-Programme, "
                + "Praktika und Studierendenprojekte sowie viele Stellen in Technik und Wirtschaft – "
                + "weltweit. Ob Einstieg oder mit Erfahrung, es gibt wahrscheinlich einen Weg für dich.";
        return hasQr
                ? base + " Scanne den QR-Code auf meinem Tablet, um direkt unsere Karriereseite zu öffnen."
                : base;
    }

    @Override
    public String getDescription() {
        return "Explains Bühler's jobs, apprenticeships and career options and shows a QR code to "
                + "the careers page on the tablet. Use when the user asks about jobs, apprenticeships, "
                + "training, internships or working at Bühler, e.g. 'Welche Jobs gibt es?', "
                + "'Welche Ausbildungen bietet Bühler?', 'what jobs are there', 'how can I apply'.";
    }
}
