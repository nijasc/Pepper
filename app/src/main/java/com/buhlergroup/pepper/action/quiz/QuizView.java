package com.buhlergroup.pepper.action.quiz;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

import java.util.List;

public class QuizView extends FrameLayout {

    private TextView progressView;
    private TextView scoreView;
    private TextView questionView;
    private TextView feedbackView;
    private LinearLayout optionsContainer;
    private volatile boolean inputEnabled = false;
    private OnOptionListener listener;

    public QuizView(Context context) {
        super(context);
        init(context);
    }

    public QuizView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuizView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_quiz, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        progressView = findViewById(R.id.quizProgress);
        scoreView = findViewById(R.id.quizScore);
        questionView = findViewById(R.id.quizQuestion);
        feedbackView = findViewById(R.id.quizFeedback);
        optionsContainer = findViewById(R.id.quizOptions);
    }

    public void setOnOptionListener(OnOptionListener listener) {
        this.listener = listener;
    }

    public void show() {
        post(() -> {
            setVisibility(View.VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        post(() -> {
            setVisibility(View.GONE);
            optionsContainer.removeAllViews();
        });
    }

    public void setScore(String text) {
        post(() -> scoreView.setText(text));
    }

    public void setFeedback(String text) {
        post(() -> feedbackView.setText(text));
    }

    public void showQuestion(String progress, String question, List<String> options) {
        post(() -> {
            progressView.setText(progress);
            questionView.setText(question);
            feedbackView.setText("");
            optionsContainer.removeAllViews();
            for (int i = 0; i < options.size(); i++) {
                final int index = i;
                TextView button = optionButton(options.get(i));
                button.setOnClickListener(v -> onOption(index));
                optionsContainer.addView(button);
            }
            inputEnabled = true;
        });
    }

    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    public void revealAnswer(int correctIndex, int chosenIndex) {
        post(() -> {
            inputEnabled = false;
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                View child = optionsContainer.getChildAt(i);
                if (i == correctIndex) {
                    child.setBackgroundResource(R.drawable.bg_pill_teal);
                    child.setAlpha(1f);
                } else if (i == chosenIndex) {
                    child.setBackgroundResource(R.drawable.bg_pill_red);
                    child.setAlpha(1f);
                } else {
                    child.setAlpha(0.4f);
                }
            }
        });
    }

    private void onOption(int index) {
        if (!inputEnabled) {
            return;
        }
        inputEnabled = false;
        OnOptionListener current = listener;
        if (current != null) {
            current.onOption(index);
        }
    }

    private TextView optionButton(String text) {
        TextView button = new TextView(getContext());
        button.setText(text);
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        button.setTextSize(20);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(R.drawable.bg_pill_teal);
        button.setPadding(dp(28), dp(18), dp(28), dp(18));
        button.setClickable(true);
        button.setFocusable(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(12);
        button.setLayoutParams(params);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    public interface OnOptionListener {
        void onOption(int index);
    }
}
