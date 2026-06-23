package com.buhlergroup.pepper.action.hold;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.buhlergroup.pepper.R;

public class HoldView extends FrameLayout {

    private TextView statusText;
    private TextView timerText;
    private AppCompatButton confirmButton;

    public HoldView(Context context) {
        super(context);
        init(context);
    }

    public HoldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HoldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(0xE6101820);

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        statusText = new TextView(context);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(34f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(48, 0, 48, 24);
        column.addView(statusText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        timerText = new TextView(context);
        timerText.setTextColor(Color.WHITE);
        timerText.setTextSize(64f);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, 0, 0, 40);
        column.addView(timerText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        confirmButton = new AppCompatButton(context);
        confirmButton.setBackgroundResource(R.drawable.bg_pill_teal);
        confirmButton.setTextColor(Color.WHITE);
        confirmButton.setTextSize(26f);
        confirmButton.setPadding(64, 28, 64, 28);
        confirmButton.setOnClickListener(v -> HoldController.get().confirmObject());
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        confirmParams.gravity = Gravity.CENTER_HORIZONTAL;
        confirmParams.bottomMargin = 32;
        column.addView(confirmButton, confirmParams);

        AppCompatButton stopButton = new AppCompatButton(context);
        stopButton.setBackgroundResource(R.drawable.bg_pill_red);
        stopButton.setText("STOP");
        stopButton.setTextColor(Color.WHITE);
        stopButton.setTextSize(36f);
        stopButton.setPadding(120, 40, 120, 40);
        stopButton.setOnClickListener(v -> HoldController.get().requestRelease());
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        stopParams.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(stopButton, stopParams);

        LayoutParams columnParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(column, columnParams);
        setVisibility(GONE);
    }

    public void show(String status, @Nullable String confirmLabel, boolean confirmVisible) {
        post(() -> {
            statusText.setText(status);
            timerText.setText("");
            if (confirmLabel != null) {
                confirmButton.setText(confirmLabel);
            }
            confirmButton.setVisibility(confirmVisible ? VISIBLE : GONE);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void setTimer(String text) {
        post(() -> timerText.setText(text));
    }

    public void hide() {
        post(() -> setVisibility(GONE));
    }
}
