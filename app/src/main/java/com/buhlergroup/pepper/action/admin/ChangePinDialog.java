package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.buhlergroup.pepper.R;

/**
 * Change-PIN dialog flow extracted from {@link AdminView}.
 *
 * <p>Builds the PIN-entry dialog, validates the input and persists the new PIN via
 * {@link AdminSettings}. Behavior and strings are identical to the original inlined flow.
 */
final class ChangePinDialog {

    private ChangePinDialog() {
    }

    static void show(View root) {
        Context context = root.getContext();
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint(R.string.admin_change_pin_hint);
        new AlertDialog.Builder(context)
                .setTitle(R.string.admin_change_pin)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.raffle_save, (d, w) -> {
                    String pin = input.getText().toString().trim();
                    if (pin.matches("\\d{4}")) {
                        AdminSettings.setPin(context, pin);
                        Toast.makeText(context, R.string.admin_change_pin_saved,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.admin_change_pin_invalid,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
