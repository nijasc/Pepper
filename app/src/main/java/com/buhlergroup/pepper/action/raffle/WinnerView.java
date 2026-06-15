package com.buhlergroup.pepper.action.raffle;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

public class WinnerView extends FrameLayout {

    private TextView titleView;
    private TextView nameView;
    private ObjectAnimator suspenseAnimator;

    public WinnerView(Context context) {
        super(context);
        init(context);
    }

    public WinnerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WinnerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        cardParams.gravity = Gravity.CENTER;
        card.setLayoutParams(cardParams);

        titleView = new TextView(context);
        titleView.setTextColor(ContextCompat.getColor(context, R.color.buhler_teal));
        titleView.setTextSize(40f);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView);

        nameView = new TextView(context);
        nameView.setTextColor(ContextCompat.getColor(context, R.color.white));
        nameView.setTextSize(72f);
        nameView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.topMargin = dp(24);
        nameView.setLayoutParams(nameParams);
        card.addView(nameView);

        addView(card);
    }

    public void showSuspense() {
        post(() -> {
            titleView.setText(R.string.winner_suspense);
            nameView.setText("…");
            setVisibility(VISIBLE);
            bringToFront();
            startSuspenseAnimation();
        });
    }

    public void revealWinner(String name) {
        post(() -> {
            stopSuspenseAnimation();
            titleView.setText(R.string.winner_title);
            nameView.setText(name);
            nameView.setScaleX(0.4f);
            nameView.setScaleY(0.4f);
            nameView.animate().scaleX(1f).scaleY(1f).setDuration(450).start();
            launchConfetti();
        });
    }

    private void launchConfetti() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        int[] colors = {0xFFE53935, 0xFFFDD835, 0xFF43A047, 0xFF1E88E5, 0xFF8E24AA, 0xFFFB8C00};
        int pieces = 28;
        for (int i = 0; i < pieces; i++) {
            View dot = new View(getContext());
            int size = dp(10 + (i % 3) * 4);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            lp.leftMargin = (int) (Math.random() * Math.max(1, width - size));
            lp.topMargin = -size;
            dot.setLayoutParams(lp);
            dot.setBackgroundColor(colors[i % colors.length]);
            addView(dot);
            long duration = 1800 + (long) (Math.random() * 1500);
            dot.animate()
                    .translationY(height + size)
                    .rotation((float) (Math.random() * 720 - 360))
                    .setDuration(duration)
                    .withEndAction(() -> removeView(dot))
                    .start();
        }
    }

    public void hide() {
        post(() -> {
            stopSuspenseAnimation();
            setVisibility(GONE);
        });
    }

    private void startSuspenseAnimation() {
        stopSuspenseAnimation();
        suspenseAnimator = ObjectAnimator.ofFloat(titleView, "alpha", 1f, 0.3f);
        suspenseAnimator.setDuration(500);
        suspenseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        suspenseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        suspenseAnimator.start();
    }

    private void stopSuspenseAnimation() {
        if (suspenseAnimator != null) {
            suspenseAnimator.cancel();
            suspenseAnimator = null;
        }
        titleView.setAlpha(1f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
