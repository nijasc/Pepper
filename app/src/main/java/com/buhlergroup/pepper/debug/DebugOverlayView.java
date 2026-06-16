package com.buhlergroup.pepper.debug;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

public class DebugOverlayView extends FrameLayout implements DebugLog.Listener {

    private TextView statusBox;
    private TextView logText;
    private ScrollView logScroll;
    private boolean suppressed = false;

    private final Runnable refresh = this::rebuild;

    public DebugOverlayView(Context context) {
        super(context);
        init(context);
    }

    public DebugOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DebugOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClickable(false);
        setFocusable(false);

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);

        statusBox = new TextView(context);
        statusBox.setTextColor(0xFFFFFFFF);
        statusBox.setTextSize(15f);
        statusBox.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusBox.setBackgroundColor(0xE600736B);
        statusBox.setPadding(dp(16), dp(8), dp(16), dp(8));
        column.addView(statusBox, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        logScroll = new ScrollView(context);
        logScroll.setBackgroundColor(0xCC0A1014);
        logText = new TextView(context);
        logText.setTextColor(0xDDFFFFFF);
        logText.setTextSize(11f);
        logText.setTypeface(Typeface.MONOSPACE);
        logText.setPadding(dp(12), dp(8), dp(12), dp(8));
        logScroll.addView(logText);
        column.addView(logScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(300)));

        addView(column, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP));
        setVisibility(GONE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DebugLog.get().addListener(this);
        updateVisibility();
    }

    @Override
    protected void onDetachedFromWindow() {
        DebugLog.get().removeListener(this);
        super.onDetachedFromWindow();
    }

    public void setSuppressed(boolean value) {
        post(() -> {
            suppressed = value;
            updateVisibility();
        });
    }

    @Override
    public void onEntry(String formattedLine) {
        scheduleRefresh();
    }

    @Override
    public void onStatus(String status) {
        post(() -> statusBox.setText(status == null || status.isEmpty() ? "—" : status));
    }

    @Override
    public void onEnabledChanged(boolean enabled) {
        post(this::updateVisibility);
    }

    private void scheduleRefresh() {
        removeCallbacks(refresh);
        post(refresh);
    }

    private void updateVisibility() {
        boolean show = DebugLog.get().isEnabled() && !suppressed;
        setVisibility(show ? VISIBLE : GONE);
        if (show) {
            String status = DebugLog.get().getStatus();
            statusBox.setText(status.isEmpty() ? "—" : status);
            rebuild();
            bringToFront();
        }
    }

    private void rebuild() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        List<String> snapshot = DebugLog.get().snapshot();
        StringBuilder sb = new StringBuilder();
        for (String line : snapshot) {
            sb.append(line).append('\n');
        }
        logText.setText(sb);
        logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
    }
}
