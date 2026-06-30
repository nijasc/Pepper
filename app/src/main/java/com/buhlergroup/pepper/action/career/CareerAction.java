package com.buhlergroup.pepper.action.career;

import android.graphics.Bitmap;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.selfie.QrGenerator;
import com.buhlergroup.pepper.config.Env;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.history.HistoryManager;

public class CareerAction extends Action {

    private static final String TAG = "Career";
    private static final String CAREER_URL_KEY = "PEPPER_CAREER_URL";
    private static final long DISPLAY_MS = 30000;

    public CareerAction(HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
        String url = Env.get(context, CAREER_URL_KEY, "").trim();
        boolean hasQr = isValidUrl(url);
        SpeechManager.getInstance().systemSay(context, answer(lang, hasQr));
        if (hasQr) {
            showQr(lang, url);
        }
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void showQr(SupportedLanguage lang, String url) {
        try {
            Bitmap qr = QrGenerator.encode(url, 600);
            CareerController.get().present(qr, hint(lang), DISPLAY_MS);
        } catch (Exception e) {
            Log.w(TAG, "Career QR could not be shown: " + e.getMessage());
        }
    }

    private String hint(SupportedLanguage lang) {
        return lang == SupportedLanguage.ENGLISH
                ? "Scan the QR code for our careers page"
                : "Scanne den QR-Code für unsere Karriereseite";
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
