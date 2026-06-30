package com.buhlergroup.pepper.action.admin;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Composes and launches the winner-notification email (Gmail intent with optional
 * FileProvider selfie attachment, falling back to a chooser).
 */
final class RaffleEmailSender {

    private static final String TAG = "AdminView";

    private final View root;
    private final Executor executor;

    RaffleEmailSender(View root, Executor executor) {
        this.root = root;
        this.executor = executor;
    }

    private Context ctx() {
        return root.getContext();
    }

    void send(RaffleEntryEntity winner, String title) {
        if (winner == null) {
            return;
        }
        executor.execute(() -> {
            Uri selfieUri = null;
            if (winner.selfieId != null && !winner.selfieId.isEmpty()) {
                SelfieRepository repository = SelfieRepository.get(ctx());
                SelfieEntity selfie = repository.findById(winner.selfieId);
                if (selfie != null) {
                    File file = new File(repository.imagesDir(), selfie.filename);
                    if (file.exists()) {
                        try {
                            selfieUri = FileProvider.getUriForFile(ctx(),
                                    ctx().getPackageName() + ".fileprovider", file);
                        } catch (Exception e) {
                            Log.w(TAG, "Selfie attachment failed: " + e.getMessage());
                        }
                    }
                }
            }
            Uri attachment = selfieUri;
            root.post(() -> launchEmail(winner, title, attachment));
        });
    }

    private void launchEmail(RaffleEntryEntity winner, String title, Uri selfieUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{winner.email});
        intent.putExtra(Intent.EXTRA_SUBJECT, ctx().getString(R.string.raffle_email_subject, title));
        intent.putExtra(Intent.EXTRA_TEXT,
                ctx().getString(R.string.raffle_email_body, winner.name, title));
        if (selfieUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, selfieUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setPackage("com.google.android.gm");
        try {
            ctx().startActivity(intent);
            return;
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Gmail not available, falling back to chooser");
        }
        Intent fallback = new Intent(intent);
        fallback.setPackage(null);
        Intent chooser = Intent.createChooser(fallback, ctx().getString(R.string.raffle_email_chooser));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(ctx(), R.string.raffle_email_chooser, Toast.LENGTH_SHORT).show();
        }
    }
}
