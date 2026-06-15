package com.buhlergroup.pepper.action.quiz;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class QuizController {

    private static final String TAG = "Quiz";
    private static final int QUESTION_COUNT = 4;
    private static final long ANSWER_TIMEOUT_MS = 25000;
    private static final long PAUSE_MS = 800;

    private static final QuizController INSTANCE = new QuizController();

    public interface StateListener {
        void onQuizStateChanged(boolean active);
    }

    private volatile QuizView view;
    private volatile boolean running = false;
    private volatile StateListener stateListener;

    private QuizController() {
    }

    public static QuizController get() {
        return INSTANCE;
    }

    public void attachView(QuizView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    private void notifyState(boolean active) {
        StateListener l = stateListener;
        if (l != null) {
            l.onQuizStateChanged(active);
        }
    }

    public void play(QiContext context, SupportedLanguage lang) {
        QuizView board = view;
        if (board == null) {
            say(context, lang == SupportedLanguage.ENGLISH
                    ? "My tablet is not ready, so I cannot run the quiz right now."
                    : "Mein Tablet ist gerade nicht bereit, deshalb kann ich kein Quiz starten.");
            return;
        }
        if (running) {
            say(context, lang == SupportedLanguage.ENGLISH
                    ? "We are already playing a quiz!"
                    : "Wir spielen doch schon ein Quiz!");
            return;
        }

        running = true;
        notifyState(true);
        BlockingQueue<Integer> answers = new LinkedBlockingQueue<>();
        board.setOnOptionListener(answers::offer);
        board.show();

        try {
            say(context, lang == SupportedLanguage.ENGLISH
                    ? "Time for a little Bühler quiz! Tap the answer you think is correct on my tablet."
                    : "Zeit für ein kleines Bühler-Quiz! Tippe die richtige Antwort auf meinem Tablet an.");

            List<QuizQuestion> questions = loadQuestions(context, lang);
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion question = questions.get(i);
                answers.clear();
                board.showQuestion(progress(lang, i + 1, questions.size()),
                        question.question, question.options);
                say(context, question.question);
                awaitAnswer(answers, ANSWER_TIMEOUT_MS);
                sleep(PAUSE_MS);
            }

            say(context, lang == SupportedLanguage.ENGLISH
                    ? "Thanks for playing!"
                    : "Danke fürs Mitmachen!");
        } catch (RuntimeException e) {
            Log.w(TAG, "Quiz ended: " + e.getMessage());
        } finally {
            board.setOnOptionListener(null);
            board.hide();
            running = false;
            notifyState(false);
        }
    }

    private List<QuizQuestion> loadQuestions(QiContext context, SupportedLanguage lang) {
        List<QuizQuestion> generated = QuizGenerator.generate(context, lang, QUESTION_COUNT);
        if (generated != null && !generated.isEmpty()) {
            if (generated.size() > QUESTION_COUNT) {
                return generated.subList(0, QUESTION_COUNT);
            }
            return generated;
        }
        Log.i(TAG, "Using local fallback quiz questions");
        return QuizQuestions.fallback(lang, QUESTION_COUNT);
    }

    private String progress(SupportedLanguage lang, int current, int total) {
        return lang == SupportedLanguage.ENGLISH
                ? "Question " + current + " of " + total
                : "Frage " + current + " von " + total;
    }

    private Integer awaitAnswer(BlockingQueue<Integer> queue, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                Integer value = queue.poll(200, TimeUnit.MILLISECONDS);
                if (value != null) {
                    return value;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
