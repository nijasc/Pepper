package com.buhlergroup.pepper.action.quiz;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

public class QuizAction extends Action {

    public QuizAction(com.buhlergroup.pepper.openai.history.HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        SupportedLanguage lang = SpeechManager.getInstance().currentLanguage();
        QuizController.get().play(context, lang);
    }

    @Override
    public String getDescription() {
        return "Starts a short interactive multiple-choice quiz about Bühler, industry and careers. "
                + "The visitor answers on the tablet. Use when the user wants to play a quiz or be "
                + "asked questions, e.g. 'Quiz', 'Frag mich was über Bühler', 'quiz me', "
                + "'ask me a question about Bühler'.";
    }
}
