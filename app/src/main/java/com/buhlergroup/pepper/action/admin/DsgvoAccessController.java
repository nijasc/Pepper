package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;

import java.util.concurrent.ExecutorService;

/**
 * DSGVO subject-access-request flow extracted from {@link AdminView}.
 *
 * <p>Prompts for an email, builds the access report off the UI thread via
 * {@link RaffleRepository}, shows the result and offers a share intent. Report content and
 * share-intent (type, subject, chooser, flags) are identical to the original inlined flow.
 */
final class DsgvoAccessController {

    private final View root;
    private final ExecutorService dbExecutor;

    DsgvoAccessController(View root, ExecutorService dbExecutor) {
        this.root = root;
        this.dbExecutor = dbExecutor;
    }

    private Context getContext() {
        return root.getContext();
    }

    void show() {
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint(R.string.dsgvo_access_hint);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dsgvo_access_title)
                .setView(input)
                .setNegativeButton(R.string.admin_back, null)
                .setPositiveButton(R.string.dsgvo_access_search,
                        (d, w) -> runAccess(input.getText().toString().trim()))
                .show();
    }

    private void runAccess(String email) {
        if (email.isEmpty()) {
            return;
        }
        dbExecutor.submit(() -> {
            String report = RaffleRepository.get(getContext()).buildAccessReport(email);
            root.post(() -> showResult(email, report));
        });
    }

    private void showResult(String email, String report) {
        if (report == null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dsgvo_access_title)
                    .setMessage(R.string.dsgvo_access_none)
                    .setPositiveButton(R.string.admin_back, null)
                    .show();
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.dsgvo_report_title, email))
                .setMessage(report)
                .setNeutralButton(R.string.dsgvo_access_share, (d, w) -> shareReport(email, report))
                .setPositiveButton(R.string.admin_back, null)
                .show();
    }

    private void shareReport(String email, String report) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.dsgvo_report_title, email));
        share.putExtra(Intent.EXTRA_TEXT, report);
        Intent chooser = Intent.createChooser(share, getContext().getString(R.string.dsgvo_access_share));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
