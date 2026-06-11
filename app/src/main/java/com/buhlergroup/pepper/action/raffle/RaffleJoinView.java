package com.buhlergroup.pepper.action.raffle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

public class RaffleJoinView extends FrameLayout {

    public interface Listener {
        void onSubmit(String name, String email, String phone);

        void onCancel();
    }

    private TextView titleView;
    private TextView errorView;
    private EditText nameField;
    private EditText emailField;
    private EditText phoneField;
    private Button saveButton;
    private volatile Listener listener;

    public RaffleJoinView(Context context) {
        super(context);
        init(context);
    }

    public RaffleJoinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RaffleJoinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_raffle_join, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        titleView = findViewById(R.id.raffleJoinTitle);
        errorView = findViewById(R.id.raffleJoinError);
        nameField = findViewById(R.id.raffleJoinName);
        emailField = findViewById(R.id.raffleJoinEmail);
        phoneField = findViewById(R.id.raffleJoinPhone);
        saveButton = findViewById(R.id.raffleJoinSave);

        saveButton.setOnClickListener(v -> {
            Listener l = listener;
            if (l != null) {
                l.onSubmit(
                        nameField.getText().toString(),
                        emailField.getText().toString(),
                        phoneField.getText().toString());
            }
        });
        findViewById(R.id.raffleJoinCancel).setOnClickListener(v -> {
            Listener l = listener;
            if (l != null) {
                l.onCancel();
            }
        });
    }

    public void show(String title, boolean requirePhone, Listener l) {
        post(() -> {
            this.listener = l;
            titleView.setText(title);
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            phoneField.setVisibility(requirePhone ? VISIBLE : GONE);
            errorView.setVisibility(GONE);
            saveButton.setEnabled(true);
            saveButton.setAlpha(1f);
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    public void showError(int messageRes) {
        post(() -> {
            saveButton.setEnabled(true);
            saveButton.setAlpha(1f);
            errorView.setText(messageRes);
            errorView.setVisibility(VISIBLE);
        });
    }

    public void setSubmitting(boolean submitting) {
        post(() -> {
            saveButton.setEnabled(!submitting);
            saveButton.setAlpha(submitting ? 0.4f : 1f);
        });
    }

    public void hide() {
        post(() -> {
            this.listener = null;
            setVisibility(GONE);
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
        });
    }
}
