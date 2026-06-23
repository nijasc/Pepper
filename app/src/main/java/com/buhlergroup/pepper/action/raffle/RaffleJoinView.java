package com.buhlergroup.pepper.action.raffle;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Patterns;
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

    public static final int STEP_NAME = 0;
    public static final int STEP_EMAIL = 1;
    public static final int STEP_PHONE = 2;
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
    private int[] steps = {STEP_NAME, STEP_EMAIL};
    private int index;
    private String name = "";
    private String email = "";
    private String phone = "";
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
            this.steps = requirePhone
                    ? new int[]{STEP_NAME, STEP_EMAIL, STEP_PHONE}
                    : new int[]{STEP_NAME, STEP_EMAIL};
            this.index = 0;
            this.name = "";
            this.email = "";
            this.phone = "";
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
        index = 0;
        applyStepUI();
        Listener current = listener;
        if (current != null) {
            current.onStepShown(steps[0]);
        }
    }

    public void goToStep(int stepType, int errorRes) {
        post(() -> {
            int target = indexOf(stepType);
            if (target < 0) {
                return;
            }
            index = target;
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
        int type = steps[index];
        String value = input.getText().toString().trim();
        Integer error = validate(type, value);
        if (error != null) {
            showErrorInternal(error);
            Listener l = listener;
            if (l != null) {
                l.onValidationError(type);
            }
            return;
        }
        store(type, value);
        if (index == steps.length - 1) {
            Listener l = listener;
            if (l != null) {
                l.onSubmit(name, email, phone);
            }
        } else {
            index++;
            animateAndApply(true);
            Listener l = listener;
            if (l != null) {
                l.onStepShown(steps[index]);
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
        if (consentPhase || index == 0) {
            return;
        }
        store(steps[index], input.getText().toString().trim());
        index--;
        animateAndApply(false);
        Listener l = listener;
        if (l != null) {
            l.onStepShown(steps[index]);
        }
    }

    private Integer validate(int type, String value) {
        switch (type) {
            case STEP_NAME:
                return value.isEmpty() ? R.string.raffle_join_name_required : null;
            case STEP_EMAIL:
                return Patterns.EMAIL_ADDRESS.matcher(value).matches()
                        ? null : R.string.raffle_join_email_invalid;
            case STEP_PHONE:
                if (value.isEmpty()) {
                    return R.string.raffle_join_phone_required;
                }
                String digits = value.replaceAll("\\D", "");
                boolean validChars = value.matches("[+0-9\\s()\\-]+");
                return validChars && digits.length() >= 7 && digits.length() <= 15
                        ? null : R.string.raffle_join_phone_invalid;
            default:
                return null;
        }
    }

    private void store(int type, String value) {
        switch (type) {
            case STEP_NAME:
                name = value;
                break;
            case STEP_EMAIL:
                email = value;
                break;
            case STEP_PHONE:
                phone = value;
                break;
            default:
                break;
        }
    }

    private String valueFor(int type) {
        switch (type) {
            case STEP_NAME:
                return name;
            case STEP_EMAIL:
                return email;
            case STEP_PHONE:
                return phone;
            default:
                return "";
        }
    }

    private void applyStepUI() {
        int type = steps[index];
        promptView.setText(promptRes(type));
        input.setHint(hintRes(type));
        input.setInputType(inputTypeFor(type));
        input.setText(valueFor(type));
        input.setSelection(input.getText().length());
        errorView.setVisibility(GONE);
        progressView.setText(getContext().getString(R.string.raffle_join_progress, index + 1, steps.length));
        cancelButton.setVisibility(VISIBLE);
        if (index == 0) {
            backButton.setVisibility(GONE);
        } else {
            backButton.setVisibility(VISIBLE);
            backButton.setText(R.string.admin_back);
        }
        nextButton.setText(index == steps.length - 1 ? R.string.raffle_join_finish : R.string.raffle_join_next);
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

    private int indexOf(int stepType) {
        for (int i = 0; i < steps.length; i++) {
            if (steps[i] == stepType) {
                return i;
            }
        }
        return -1;
    }

    private int promptRes(int type) {
        switch (type) {
            case STEP_EMAIL:
                return R.string.raffle_join_step_email;
            case STEP_PHONE:
                return R.string.raffle_join_step_phone;
            case STEP_NAME:
            default:
                return R.string.raffle_join_step_name;
        }
    }

    private int hintRes(int type) {
        switch (type) {
            case STEP_EMAIL:
                return R.string.raffle_join_email_hint;
            case STEP_PHONE:
                return R.string.raffle_join_phone_hint;
            case STEP_NAME:
            default:
                return R.string.raffle_join_name_hint;
        }
    }

    private int inputTypeFor(int type) {
        switch (type) {
            case STEP_EMAIL:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case STEP_PHONE:
                return InputType.TYPE_CLASS_PHONE;
            case STEP_NAME:
            default:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
        }
    }

    public interface Listener {
        void onStepShown(int stepType);

        void onValidationError(int stepType);

        void onSubmit(String name, String email, String phone);

        void onCancel();
    }
}
