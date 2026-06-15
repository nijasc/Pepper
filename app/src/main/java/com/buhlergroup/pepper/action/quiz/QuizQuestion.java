package com.buhlergroup.pepper.action.quiz;

import java.util.List;

public class QuizQuestion {

    public final String question;
    public final List<String> options;
    public final int correctIndex;

    public QuizQuestion(String question, List<String> options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public boolean isValid() {
        return question != null && !question.trim().isEmpty()
                && options != null && options.size() >= 2 && options.size() <= 4
                && correctIndex >= 0 && correctIndex < options.size();
    }
}
