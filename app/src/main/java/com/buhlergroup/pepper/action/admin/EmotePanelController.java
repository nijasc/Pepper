package com.buhlergroup.pepper.action.admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.RobotContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class EmotePanelController {

    private static final String TAG = "EmotePanel";
    private static final String PREFS = "emote_prefs";
    private static final String KEY_OVERLAY_IMAGE = "overlay_image_uri";

    private static final int[] EMOTE_RAW = {
            R.raw.gesture_wave_hand,
            R.raw.gesture_welcome_arm,
            R.raw.pepper_highfive,
            R.raw.raise_right_hand_b001,
            R.raw.gesture_hands_up,
            R.raw.gesture_eagle,
            R.raw.gesture_open_arms,
            R.raw.gesture_present
    };
    private static final int[] EMOTE_LABEL = {
            R.string.emote_wave,
            R.string.emote_welcome,
            R.string.emote_highfive,
            R.string.emote_raise_hand,
            R.string.emote_hands_up,
            R.string.emote_eagle,
            R.string.emote_open_arms,
            R.string.emote_present
    };

    private final View root;
    private final PanelNavigator panelNav;
    private final Spinner emoteSelect;
    private final ImageView imagePreview;
    private final TextView imageHint;
    private final View overlay;
    private final ImageView overlayImage;
    private final ExecutorService playExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean cancel = new AtomicBoolean(false);
    private volatile boolean playing = false;
    private Uri overlayUri;

    EmotePanelController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.emoteSelect = root.findViewById(R.id.emoteSelect);
        this.emoteSelect.setPopupBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(ctx(), R.color.admin_card)));
        this.imagePreview = root.findViewById(R.id.emoteImagePreview);
        this.imageHint = root.findViewById(R.id.emoteImageHint);
        this.overlay = root.findViewById(R.id.adminEmoteOverlay);
        this.overlayImage = root.findViewById(R.id.emoteOverlayImage);

        root.findViewById(R.id.emotePlay).setOnClickListener(v -> playSelected());
        root.findViewById(R.id.emotePickImage).setOnClickListener(v -> pickImage());
        ImageButton overlayStop = root.findViewById(R.id.emoteOverlayStop);
        overlayStop.setOnClickListener(v -> stop());

        overlayUri = loadSavedUri();
    }

    private Context ctx() {
        return root.getContext();
    }

    void showEmotes() {
        String[] labels = new String[EMOTE_LABEL.length];
        for (int i = 0; i < EMOTE_LABEL.length; i++) {
            labels[i] = ctx().getString(EMOTE_LABEL[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx(),
                R.layout.spinner_item_admin, labels);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_admin);
        emoteSelect.setAdapter(adapter);
        bindPreview();
        panelNav.show(PanelNavigator.PANEL_EMOTES);
    }

    private void playSelected() {
        if (playing) {
            return;
        }
        QiContext context = RobotContext.get();
        if (context == null) {
            toast(R.string.emote_not_ready);
            return;
        }
        int pos = emoteSelect.getSelectedItemPosition();
        if (pos < 0 || pos >= EMOTE_RAW.length) {
            return;
        }
        int rawRes = EMOTE_RAW[pos];

        showOverlay();
        cancel.set(false);
        playing = true;
        playExecutor.execute(() -> runLoop(context, rawRes));
    }

    private void runLoop(QiContext context, int rawRes) {
        try {
            while (!cancel.get()) {
                Animation animation = AnimationBuilder.with(context)
                        .withResources(rawRes)
                        .build();
                Animate animate = AnimateBuilder.with(context)
                        .withAnimation(animation)
                        .build();
                animate.run();
            }
        } catch (Exception e) {
            Log.w(TAG, "Emote-Wiedergabe fehlgeschlagen: " + e.getMessage());
        } finally {
            playing = false;
            root.post(this::hideOverlay);
        }
    }

    private void stop() {
        cancel.set(true);
        hideOverlay();
    }

    private void showOverlay() {
        if (overlayUri != null) {
            setImage(overlayImage, overlayUri);
        } else {
            overlayImage.setImageDrawable(null);
        }
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
    }

    private void hideOverlay() {
        overlay.setVisibility(View.GONE);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            ((Activity) ctx()).startActivityForResult(intent, AdminController.REQUEST_EMOTE_IMAGE);
        } catch (Exception e) {
            toast(R.string.admin_export_failed);
        }
    }

    void onImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            ctx().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            Log.w(TAG, "Persistable permission failed: " + e.getMessage());
        }
        overlayUri = uri;
        prefs().edit().putString(KEY_OVERLAY_IMAGE, uri.toString()).apply();
        bindPreview();
    }

    private void bindPreview() {
        if (overlayUri != null && setImage(imagePreview, overlayUri)) {
            imageHint.setVisibility(View.GONE);
        } else {
            imagePreview.setImageDrawable(null);
            imageHint.setVisibility(View.VISIBLE);
        }
    }

    private boolean setImage(ImageView target, Uri uri) {
        try {
            target.setImageURI(null);
            target.setImageURI(uri);
            return target.getDrawable() != null;
        } catch (Exception e) {
            Log.w(TAG, "Bild laden fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    private Uri loadSavedUri() {
        String saved = prefs().getString(KEY_OVERLAY_IMAGE, null);
        return saved == null ? null : Uri.parse(saved);
    }

    private SharedPreferences prefs() {
        return ctx().getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private void toast(int resId) {
        Toast.makeText(ctx(), resId, Toast.LENGTH_SHORT).show();
    }
}
