package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Builds the dynamic entry-row views for the raffle overview list (thumbnail,
 * name/email/phone label, winner styling, delete button, selfie tap-to-detail).
 */
final class RaffleEntryRowFactory {

    /** Callbacks the factory needs from its host. */
    interface Host {
        void onDeleteEntry(RaffleEntryEntity entry);

        void onShowSelfie(SelfieEntity selfie);
    }

    private final View root;
    private final Executor executor;
    private final Host host;

    RaffleEntryRowFactory(View root, Executor executor, Host host) {
        this.root = root;
        this.executor = executor;
        this.host = host;
    }

    private Context ctx() {
        return root.getContext();
    }

    private int dp(int value) {
        return Math.round(value * root.getResources().getDisplayMetrics().density);
    }

    View createEntryRow(RaffleEntryEntity entry, boolean isWinner) {
        LinearLayout row = new LinearLayout(ctx());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pv = dp(6);
        row.setPadding(0, pv, 0, pv);

        ImageView thumb = new ImageView(ctx());
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        thumbParams.setMarginEnd(dp(12));
        thumb.setLayoutParams(thumbParams);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundColor(0xFF000000);
        thumb.setVisibility(View.GONE);
        row.addView(thumb);

        StringBuilder sb = new StringBuilder();
        if (isWinner) {
            sb.append("🏆 ");
        }
        sb.append(entry.name).append(" · ").append(entry.email);
        if (entry.phone != null && !entry.phone.isEmpty()) {
            sb.append(" · ").append(entry.phone);
        }
        TextView text = new TextView(ctx());
        text.setText(sb.toString());
        text.setTextColor(isWinner ? 0xFFFFD54F : 0xFFFFFFFF);
        if (isWinner) {
            text.setTypeface(text.getTypeface(), Typeface.BOLD);
        }
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(textParams);
        row.addView(text);

        Button deleteButton = new Button(ctx());
        deleteButton.setText(R.string.raffle_entry_delete);
        deleteButton.setTextColor(0xFFFFFFFF);
        deleteButton.setBackgroundResource(R.drawable.bg_pill_red);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        deleteParams.setMarginStart(dp(8));
        deleteButton.setLayoutParams(deleteParams);
        deleteButton.setOnClickListener(v -> host.onDeleteEntry(entry));
        row.addView(deleteButton);

        if (entry.selfieId != null && !entry.selfieId.isEmpty()) {
            executor.execute(() -> {
                SelfieRepository repository = SelfieRepository.get(ctx());
                SelfieEntity selfie = repository.findById(entry.selfieId);
                if (selfie == null) {
                    return;
                }
                Bitmap bitmap = SelfieAdapter.decodeThumb(
                        new File(repository.imagesDir(), selfie.filename), 160);
                root.post(() -> {
                    if (bitmap != null) {
                        thumb.setImageBitmap(bitmap);
                    }
                    thumb.setVisibility(View.VISIBLE);
                    row.setOnClickListener(v -> host.onShowSelfie(selfie));
                });
            });
        }
        return row;
    }
}
