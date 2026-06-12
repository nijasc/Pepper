package com.buhlergroup.pepper.action.dialogue;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.R;

public class DialogueView extends FrameLayout {

    private TextView dialogueText;

    public DialogueView(Context context) {
        super(context);
        init(context);
    }

    public DialogueView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DialogueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_dialogue, this, true);
        setClickable(false);
        setFocusable(false);
        dialogueText = findViewById(R.id.dialogueText);
    }

    public void setText(CharSequence text) {
        post(() -> {
            dialogueText.setText(text);
            setVisibility(View.VISIBLE);
        });
    }

    public void hide() {
        post(() -> {
            setVisibility(View.GONE);
            dialogueText.setText("");
        });
    }
}
