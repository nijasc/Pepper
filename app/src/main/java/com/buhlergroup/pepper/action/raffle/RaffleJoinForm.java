package com.buhlergroup.pepper.action.raffle;

import android.text.InputType;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.R;

/**
 * Form-model for the raffle-join flow: owns the step order, the collected
 * field values, validation, and the per-step resource mapping. Holds no
 * reference to any Android {@link android.view.View}; {@link RaffleJoinView}
 * delegates all data/logic here and keeps only rendering and animation.
 */
public class RaffleJoinForm {

    public static final int STEP_NAME = 0;
    public static final int STEP_EMAIL = 1;
    public static final int STEP_PHONE = 2;

    private int[] steps = {STEP_NAME, STEP_EMAIL};
    private int index;
    private String name = "";
    private String email = "";
    private String phone = "";

    /** Resets the form to the start, choosing the step order. */
    public void reset(boolean requirePhone) {
        this.steps = requirePhone
                ? new int[]{STEP_NAME, STEP_EMAIL, STEP_PHONE}
                : new int[]{STEP_NAME, STEP_EMAIL};
        this.index = 0;
        this.name = "";
        this.email = "";
        this.phone = "";
    }

    public int stepCount() {
        return steps.length;
    }

    public int currentIndex() {
        return index;
    }

    public int currentStepType() {
        return steps[index];
    }

    public int stepTypeAt(int position) {
        return steps[position];
    }

    public boolean isFirstStep() {
        return index == 0;
    }

    public boolean isLastStep() {
        return index == steps.length - 1;
    }

    /** Advances to the next step. Caller must ensure not already at the last step. */
    public void advance() {
        index++;
    }

    /** Steps back to the previous step. Caller must ensure not already at the first step. */
    public void retreat() {
        index--;
    }

    /** Moves to the step matching {@code stepType}; returns false if absent. */
    public boolean moveTo(int stepType) {
        int target = indexOf(stepType);
        if (target < 0) {
            return false;
        }
        index = target;
        return true;
    }

    private int indexOf(int stepType) {
        for (int i = 0; i < steps.length; i++) {
            if (steps[i] == stepType) {
                return i;
            }
        }
        return -1;
    }

    /** Returns an error string resource for an invalid value, or {@code null} if valid. */
    @Nullable
    public Integer validate(int type, String value) {
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

    public void store(int type, String value) {
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

    public String valueFor(int type) {
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

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public int promptRes(int type) {
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

    public int hintRes(int type) {
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

    public int inputTypeFor(int type) {
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
}
