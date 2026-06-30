package com.buhlergroup.pepper.action.raffle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.databinding.ViewRaffleJoinBinding;

public class RaffleJoinView extends FrameLayout {

    public static final int STEP_NAME = RaffleJoinForm.STEP_NAME;
    public static final int STEP_EMAIL = RaffleJoinForm.STEP_EMAIL;
    public static final int STEP_PHONE = RaffleJoinForm.STEP_PHONE;
    private View card;
    private View stepContainer;
    private View consentContainer;
    private TextView consentText;
    private CheckBox consentCheck;
    private boolean consentPhase;
    private TextView titleView;
    private TextView progressView;
    private TextView promptView;
    private TextView errorView;
    private TextView confirmView;
    private EditText input;
    private Button cancelButton;
    private Button backButton;
    private Button nextButton;
    private volatile Listener listener;
    private final RaffleJoinForm form = new RaffleJoinForm();

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
        ViewRaffleJoinBinding binding =
                ViewRaffleJoinBinding.inflate(LayoutInflater.from(context), this);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);

        card = binding.raffleJoinCard;
        stepContainer = binding.raffleJoinStep;
        consentContainer = binding.raffleJoinConsent;
        consentText = binding.raffleJoinConsentText;
        consentCheck = binding.raffleJoinConsentCheck;
        titleView = binding.raffleJoinTitle;
        progressView = binding.raffleJoinProgress;
        promptView = binding.raffleJoinStepPrompt;
        errorView = binding.raffleJoinError;
        confirmView = binding.raffleJoinConfirm;
        input = binding.raffleJoinInput;
        cancelButton = binding.raffleJoinCancel;
        backButton = binding.raffleJoinBack;
        nextButton = binding.raffleJoinNext;

        cancelButton.setOnClickListener(v -> onCancel());
        backButton.setOnClickListener(v -> onBack());
        nextButton.setOnClickListener(v -> onNext());
        consentCheck.setOnCheckedChangeListener((button, checked) -> {
            if (consentPhase) {
                nextButton.setEnabled(checked);
                nextButton.setAlpha(checked ? 1f : 0.4f);
            }
        });
    }

    public void show(String title, boolean requirePhone, Listener l) {
        post(() -> {
            this.listener = l;
            form.reset(requirePhone);
            titleView.setText(title);
            confirmView.setVisibility(GONE);
            card.setVisibility(VISIBLE);
            stepContainer.setTranslationX(0f);
            stepContainer.setAlpha(1f);
            showConsentPhase();
            setVisibility(VISIBLE);
            bringToFront();
        });
    }

    private void showConsentPhase() {
        consentPhase = true;
        stepContainer.setVisibility(GONE);
        consentContainer.setVisibility(VISIBLE);
        progressView.setText(R.string.raffle_consent_title);
        consentText.setText(R.string.raffle_consent_notice);
        consentCheck.setText(R.string.raffle_consent_checkbox);
        consentCheck.setChecked(false);
        cancelButton.setVisibility(VISIBLE);
        backButton.setVisibility(GONE);
        nextButton.setText(R.string.raffle_consent_continue);
        nextButton.setEnabled(false);
        nextButton.setAlpha(0.4f);
    }

    private void leaveConsentPhase() {
        consentPhase = false;
        consentContainer.setVisibility(GONE);
        stepContainer.setVisibility(VISIBLE);
        applyStepUI();
        Listener current = listener;
        if (current != null) {
            current.onStepShown(form.stepTypeAt(0));
        }
    }

    public void goToStep(int stepType, int errorRes) {
        post(() -> {
            if (!form.moveTo(stepType)) {
                return;
            }
            applyStepUI();
            showErrorInternal(errorRes);
        });
    }

    public void setSubmitting(boolean submitting) {
        post(() -> {
            nextButton.setEnabled(!submitting);
            nextButton.setAlpha(submitting ? 0.4f : 1f);
        });
    }

    public void showConfirmation(String visitorName, Runnable onClosed) {
        post(() -> {
            confirmView.setText(getContext().getString(R.string.raffle_join_confirm, visitorName));
            card.setVisibility(GONE);
            confirmView.setVisibility(VISIBLE);
            postDelayed(() -> {
                if (onClosed != null) {
                    onClosed.run();
                }
            }, 3000);
        });
    }

    public void hide() {
        post(() -> {
            this.listener = null;
            hideKeyboard();
            setVisibility(GONE);
            input.setText("");
        });
    }

    private void onNext() {
        if (consentPhase) {
            if (consentCheck.isChecked()) {
                leaveConsentPhase();
            }
            return;
        }
        int type = form.currentStepType();
        String value = input.getText().toString().trim();
        Integer error = form.validate(type, value);
        if (error != null) {
            showErrorInternal(error);
            Listener l = listener;
            if (l != null) {
                l.onValidationError(type);
            }
            return;
        }
        form.store(type, value);
        if (form.isLastStep()) {
            Listener l = listener;
            if (l != null) {
                l.onSubmit(form.name(), form.email(), form.phone());
            }
        } else {
            form.advance();
            animateAndApply(true);
            Listener l = listener;
            if (l != null) {
                l.onStepShown(form.currentStepType());
            }
        }
    }

    private void onCancel() {
        Listener l = listener;
        if (l != null) {
            l.onCancel();
        }
    }

    private void onBack() {
        if (consentPhase || form.isFirstStep()) {
            return;
        }
        form.store(form.currentStepType(), input.getText().toString().trim());
        form.retreat();
        animateAndApply(false);
        Listener l = listener;
        if (l != null) {
            l.onStepShown(form.currentStepType());
        }
    }

    private void applyStepUI() {
        int type = form.currentStepType();
        promptView.setText(form.promptRes(type));
        input.setHint(form.hintRes(type));
        input.setInputType(form.inputTypeFor(type));
        input.setText(form.valueFor(type));
        input.setSelection(input.getText().length());
        errorView.setVisibility(GONE);
        progressView.setText(getContext().getString(
                R.string.raffle_join_progress, form.currentIndex() + 1, form.stepCount()));
        cancelButton.setVisibility(VISIBLE);
        if (form.isFirstStep()) {
            backButton.setVisibility(GONE);
        } else {
            backButton.setVisibility(VISIBLE);
            backButton.setText(R.string.admin_back);
        }
        nextButton.setText(form.isLastStep() ? R.string.raffle_join_finish : R.string.raffle_join_next);
        nextButton.setEnabled(true);
        nextButton.setAlpha(1f);
        focusInput();
    }

    private void focusInput() {
        input.post(() -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private void animateAndApply(boolean forward) {
        float width = stepContainer.getWidth();
        if (width <= 0f) {
            applyStepUI();
            return;
        }
        float outX = forward ? -width : width;
        stepContainer.animate().translationX(outX).alpha(0f).setDuration(140).withEndAction(() -> {
            applyStepUI();
            stepContainer.setTranslationX(-outX);
            stepContainer.animate().translationX(0f).alpha(1f).setDuration(140).start();
        }).start();
    }

    private void showErrorInternal(int messageRes) {
        nextButton.setEnabled(true);
        nextButton.setAlpha(1f);
        errorView.setText(messageRes);
        errorView.setVisibility(VISIBLE);
        shake(stepContainer);
    }

    private void shake(View target) {
        target.animate().cancel();
        target.setTranslationX(0f);
        target.animate().translationX(-14f).setDuration(50).withEndAction(() ->
                target.animate().translationX(14f).setDuration(50).withEndAction(() ->
                        target.animate().translationX(-8f).setDuration(50).withEndAction(() ->
                                target.animate().translationX(0f).setDuration(50).start()))).start();
    }

    public interface Listener {
        void onStepShown(int stepType);

        void onValidationError(int stepType);

        void onSubmit(String name, String email, String phone);

        void onCancel();
    }
}
