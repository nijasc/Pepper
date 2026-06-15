package com.buhlergroup.pepper.action.selfie;

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

public class SelfieView extends FrameLayout {

    private ImageView photoView;
    private ImageView qrView;
    private ImageView wifiQrView;
    private View wifiBlock;
    private View shareBlock;
    private View previewBlock;
    private TextView statusView;
    private TextView okayButton;
    private TextView saveButton;
    private TextView retakeButton;
    private volatile Runnable closeListener;

    public SelfieView(Context context) {
        super(context);
        init(context);
    }

    public SelfieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelfieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_selfie, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        photoView = findViewById(R.id.selfiePhoto);
        qrView = findViewById(R.id.selfieQr);
        wifiQrView = findViewById(R.id.selfieWifiQr);
        wifiBlock = findViewById(R.id.selfieWifiBlock);
        shareBlock = findViewById(R.id.selfieShareBlock);
        previewBlock = findViewById(R.id.selfiePreviewBlock);
        statusView = findViewById(R.id.selfieStatus);
        okayButton = findViewById(R.id.selfieOkay);
        saveButton = findViewById(R.id.selfieSave);
        retakeButton = findViewById(R.id.selfieRetake);
        okayButton.setTextColor(ContextCompat.getColor(context, R.color.white));
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

    public void showPreview(Bitmap photo, boolean canRetake, Runnable onSave, Runnable onRetake) {
        post(() -> {
            photoView.setImageBitmap(photo);
            previewBlock.setVisibility(View.VISIBLE);
            shareBlock.setVisibility(View.GONE);
            retakeButton.setVisibility(canRetake ? View.VISIBLE : View.GONE);
            saveButton.setOnClickListener(v -> {
                if (onSave != null) {
                    onSave.run();
                }
            });
            retakeButton.setOnClickListener(v -> {
                if (onRetake != null) {
                    onRetake.run();
                }
            });
            setVisibility(View.VISIBLE);
            bringToFront();
        });
    }

    public void show(Bitmap photo, Bitmap qr, Bitmap wifiQr) {
        post(() -> {
            previewBlock.setVisibility(View.GONE);
            shareBlock.setVisibility(View.VISIBLE);
            photoView.setImageBitmap(photo);
            qrView.setImageBitmap(qr);
            if (wifiQr != null) {
                wifiQrView.setImageBitmap(wifiQr);
                wifiBlock.setVisibility(View.VISIBLE);
            } else {
                wifiBlock.setVisibility(View.GONE);
            }
            setVisibility(View.VISIBLE);
            bringToFront();
        });
    }

    public void setStatus(String text) {
        post(() -> statusView.setText(text));
    }

    public void hide() {
        post(() -> {
            setVisibility(View.GONE);
            photoView.setImageBitmap(null);
            qrView.setImageBitmap(null);
            wifiQrView.setImageBitmap(null);
            saveButton.setOnClickListener(null);
            retakeButton.setOnClickListener(null);
            previewBlock.setVisibility(View.GONE);
            shareBlock.setVisibility(View.VISIBLE);
        });
    }
}
