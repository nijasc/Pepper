package com.buhlergroup.pepper.action.memory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

public class MemoryGameView extends FrameLayout {

    private static final float DIM_ALPHA = 0.35f;
    private static final long TOUCH_FLASH_MS = 220;
    private final View[] pads = new View[4];
    private final TonePlayer tonePlayer = new TonePlayer();
    private TextView statusView;
    private TextView scoreView;
    private TextView highscoreView;
    private TextView hintView;
    private volatile boolean inputEnabled = false;
    private OnPadListener padListener;
    public MemoryGameView(Context context) {
        super(context);
        init(context);
    }

    public MemoryGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MemoryGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_memory_game, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        statusView = findViewById(R.id.memStatus);
        scoreView = findViewById(R.id.memScore);
        highscoreView = findViewById(R.id.memHighscore);
        hintView = findViewById(R.id.memHint);
        pads[0] = findViewById(R.id.memPad0);
        pads[1] = findViewById(R.id.memPad1);
        pads[2] = findViewById(R.id.memPad2);
        pads[3] = findViewById(R.id.memPad3);

        for (int i = 0; i < pads.length; i++) {
            final int index = i;
            pads[i].setOnClickListener(v -> onPadTouched(index));
        }
    }

    public void setOnPadListener(OnPadListener listener) {
        this.padListener = listener;
    }

    public void show() {
        post(() -> {
            resetPads();
            setVisibility(View.VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        post(() -> setVisibility(View.GONE));
    }

    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    public void setStatus(String text) {
        post(() -> statusView.setText(text));
    }

    public void setScore(int score) {
        post(() -> scoreView.setText("Punkte: " + score));
    }

    public void setHighscore(int highscore) {
        post(() -> highscoreView.setText(highscore > 0 ? "Rekord: " + highscore : ""));
    }

    public void setHint(String text) {
        post(() -> hintView.setText(text));
    }

    public void playPad(int index, long onMs) {
        post(() -> flashAndTone(index, onMs));
    }

    public void playSuccessCue() {
        tonePlayer.playSuccess();
    }

    public void playErrorCue() {
        tonePlayer.playError();
    }

    private void onPadTouched(int index) {
        if (!inputEnabled) {
            return;
        }
        flashAndTone(index, TOUCH_FLASH_MS);
        OnPadListener listener = padListener;
        if (listener != null) {
            listener.onPad(index);
        }
    }

    private void flashAndTone(int index, long onMs) {
        if (index < 0 || index >= pads.length) {
            return;
        }
        View pad = pads[index];
        pad.animate().cancel();
        pad.setAlpha(1f);
        pad.setScaleX(1.06f);
        pad.setScaleY(1.06f);
        pad.postDelayed(() -> pad.animate()
                .alpha(DIM_ALPHA)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .start(), onMs);
        tonePlayer.playPadTone(index, onMs);
    }

    private void resetPads() {
        for (View pad : pads) {
            pad.animate().cancel();
            pad.setAlpha(DIM_ALPHA);
            pad.setScaleX(1f);
            pad.setScaleY(1f);
        }
    }

    public interface OnPadListener {
        void onPad(int index);
    }
}
