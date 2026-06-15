package com.buhlergroup.pepper.action.attract;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

public class AttractView extends FrameLayout {

    private static final long ROTATE_MS = 4000;

    private TextView suggestionView;
    private String[] suggestions;
    private int index;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable rotator = new Runnable() {
        @Override
        public void run() {
            showNextSuggestion();
            handler.postDelayed(this, ROTATE_MS);
        }
    };

    public AttractView(Context context) {
        super(context);
        init(context);
    }

    public AttractView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AttractView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClickable(false);
        setFocusable(false);

        suggestions = getResources().getStringArray(R.array.attract_suggestions);

        suggestionView = new TextView(context);
        suggestionView.setTextColor(ContextCompat.getColor(context, R.color.white));
        suggestionView.setTextSize(40f);
        suggestionView.setGravity(Gravity.CENTER);
        int pad = Math.round(32 * getResources().getDisplayMetrics().density);
        suggestionView.setPadding(pad, pad, pad, pad);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        suggestionView.setLayoutParams(params);
        addView(suggestionView);
    }

    public void show() {
        post(() -> {
            index = 0;
            setVisibility(VISIBLE);
            handler.removeCallbacks(rotator);
            handler.post(rotator);
        });
    }

    public void hide() {
        post(() -> {
            handler.removeCallbacks(rotator);
            setVisibility(GONE);
        });
    }

    private void showNextSuggestion() {
        if (suggestions == null || suggestions.length == 0) {
            return;
        }
        suggestionView.setText(suggestions[index % suggestions.length]);
        suggestionView.setAlpha(0f);
        suggestionView.animate().alpha(1f).setDuration(500).start();
        index++;
    }
}
