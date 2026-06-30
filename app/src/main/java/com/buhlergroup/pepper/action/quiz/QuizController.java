package com.buhlergroup.pepper.action.quiz;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.util.ThreadUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class QuizController {

    private static final String TAG = "Quiz";
    private static final int QUESTION_COUNT = 4;
    private static final long ANSWER_TIMEOUT_MS = 25000;
    private static final long FEEDBACK_PAUSE_MS = 2200;

    private static final QuizController INSTANCE = new QuizController();
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
            say(context, QuizNarration.tabletNotReady(lang));
            return;
        }
        if (running) {
            say(context, QuizNarration.alreadyPlaying(lang));
            return;
        }

        running = true;
        notifyState(true);
        BlockingQueue<Integer> answers = new LinkedBlockingQueue<>();
        board.setOnOptionListener(answers::offer);
        board.show();

        try {
            say(context, QuizNarration.intro(lang));

            List<QuizQuestion> questions = loadQuestions(context, lang);
            int score = 0;
            for (int i = 0; i < questions.size(); i++) {
                QuizQuestion question = questions.get(i);
                answers.clear();
                board.showQuestion(QuizNarration.progress(lang, i + 1, questions.size()),
                        question.question, question.options);
                board.setScore(QuizNarration.scoreText(lang, score));
                say(context, question.question);

                Integer choice = awaitAnswer(answers);
                int chosen = choice == null ? -1 : choice;
                boolean correct = chosen == question.correctIndex;
                if (correct) {
                    score++;
                }
                board.revealAnswer(question.correctIndex, chosen);
                board.setScore(QuizNarration.scoreText(lang, score));
                say(context, QuizNarration.feedback(lang, correct, chosen < 0, question));
                sleep();
            }

            say(context, QuizNarration.finalResult(lang, score, questions.size()));
            board.setOnOptionListener(null);
            board.hide();
            maybeOfferRaffle(context, lang, score, questions.size());
        } catch (RuntimeException e) {
            Log.w(TAG, "Quiz ended: " + e.getMessage());
        } finally {
            board.setOnOptionListener(null);
            board.hide();
            running = false;
            notifyState(false);
        }
    }

    private void maybeOfferRaffle(QiContext context, SupportedLanguage lang, int score, int total) {
        if (score * 2 < total) {
            return;
        }
        try {
            RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();
            if (raffle == null || raffle.status != RaffleStatus.ACTIVE) {
                return;
            }
            say(context, QuizNarration.raffleOffer(lang));
            RaffleJoinController.get().join(context, raffle);
        } catch (Exception e) {
            Log.w(TAG, "Raffle offer after quiz failed: " + e.getMessage());
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

    private Integer awaitAnswer(BlockingQueue<Integer> queue) {
        long deadline = System.currentTimeMillis() + QuizController.ANSWER_TIMEOUT_MS;
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

    private void sleep() {
        ThreadUtils.sleep(QuizController.FEEDBACK_PAUSE_MS);
    }

    public interface StateListener {
        void onQuizStateChanged(boolean active);
    }
}
