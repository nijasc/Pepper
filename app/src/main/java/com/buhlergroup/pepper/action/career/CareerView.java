package com.buhlergroup.pepper.action.career;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

public class CareerView extends FrameLayout {

    private ImageView qrView;
    private TextView hintView;
    private volatile Runnable closeListener;

    public CareerView(Context context) {
        super(context);
        init(context);
    }

    public CareerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CareerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_career, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        qrView = findViewById(R.id.careerQr);
        hintView = findViewById(R.id.careerHint);
        TextView okayButton = findViewById(R.id.careerOkay);
        okayButton.setOnClickListener(v -> {
            Runnable listener = closeListener;
            if (listener != null) {
                listener.run();
            }
            hide();
        });
    }

    public void setOnCloseListener(Runnable listener) {
        this.closeListener = listener;
    }

    public void show(Bitmap qr, String hint) {
        post(() -> {
            qrView.setImageBitmap(qr);
            if (hint != null) {
                hintView.setText(hint);
            }
            setVisibility(View.VISIBLE);
            bringToFront();
        });
    }

    public void hide() {
        post(() -> {
            setVisibility(View.GONE);
            qrView.setImageBitmap(null);
        });
    }
}
