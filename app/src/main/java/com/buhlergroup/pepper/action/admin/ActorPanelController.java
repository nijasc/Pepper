package com.buhlergroup.pepper.action.admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.RobotContext;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The "Actor" admin tile: Pepper as a camera actor for the Sommerferien video. A deck of
 * tap-to-fire {@link ActorPreset presets} (grouped by Drehbuch) drives a full-screen
 * display-state on the tablet plus an optional looping gesture. Supports a hands-free
 * start delay (tap, walk behind the camera, then it fires) and timed auto-sequences
 * (countdown → fireworks). Replaces the old single-image "Emotes" tile.
 */
final class ActorPanelController {

    private static final String TAG = "ActorPanel";
    private static final String PREFS = "emote_prefs";
    private static final String KEY_OVERLAY_IMAGE = "overlay_image_uri";
    private static final int[] DELAYS = {0, 3, 5, 10};

    private final View root;
    private final PanelNavigator panelNav;
    private final LinearLayout deck;
    private final View overlay;
    private final ActorDisplayView display;
    private final Button delayButton;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService playExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService speechExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger generation = new AtomicInteger(0);

    private List<ActorPreset> presets;
    private int delayIdx = 0;
    private Uri overlayUri;

    ActorPanelController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.deck = root.findViewById(R.id.actorDeckContainer);
        this.overlay = root.findViewById(R.id.adminActorOverlay);
        this.delayButton = root.findViewById(R.id.actorDelayButton);

        ViewGroup host = root.findViewById(R.id.actorDisplayHost);
        this.display = new ActorDisplayView(ctx());
        host.addView(display, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageButton overlayStop = root.findViewById(R.id.actorOverlayStop);
        overlayStop.setOnClickListener(v -> stop());
        delayButton.setOnClickListener(v -> cycleDelay());

        overlayUri = loadSavedUri();
        buildDeck();
        updateDelayLabel();
    }

    private Context ctx() {
        return root.getContext();
    }

    void showActor() {
        panelNav.show(PanelNavigator.PANEL_ACTOR);
    }

    // ----------------------------------------------------------------- deck UI

    private void buildDeck() {
        presets = ActorPresets.build(ctx());
        deck.removeAllViews();
        String lastGroup = null;
        for (ActorPreset preset : presets) {
            if (!preset.group.equals(lastGroup)) {
                deck.addView(header(preset.group));
                lastGroup = preset.group;
            }
            deck.addView(button(preset));
        }
    }

    private TextView header(String text) {
        TextView t = new TextView(ctx());
        t.setText(text);
        t.setAllCaps(true);
        t.setTextColor(ActorState.SUN);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        t.setPadding(4, 28, 4, 6);
        return t;
    }

    private Button button(ActorPreset preset) {
        AppCompatButton b = new AppCompatButton(ctx());
        b.setText(preset.label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        b.setBackgroundColor(preset.isSequence() ? ActorState.TEAL_DARK : Color.parseColor("#22FFFFFF"));
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(32, 28, 32, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 8;
        b.setLayoutParams(lp);
        b.setOnClickListener(v -> onPreset(preset));
        return b;
    }

    private void cycleDelay() {
        delayIdx = (delayIdx + 1) % DELAYS.length;
        updateDelayLabel();
    }

    private void updateDelayLabel() {
        int sec = DELAYS[delayIdx];
        delayButton.setText(sec == 0 ? ctx().getString(R.string.actor_delay_off) : sec + " s");
    }

    // -------------------------------------------------------------- fire / stop

    private void onPreset(ActorPreset preset) {
        if (preset.picksImage) {
            pickImage();
            return;
        }
        fire(preset);
    }

    private void fire(ActorPreset preset) {
        int gen = newGeneration();
        showOverlay();
        int delaySec = DELAYS[delayIdx];
        if (delaySec > 0) {
            startDelayCountdown(preset, delaySec, gen);
        } else {
            run(preset, gen);
        }
    }

    private void startDelayCountdown(ActorPreset preset, int remaining, int gen) {
        if (gen != generation.get()) {
            return;
        }
        if (remaining <= 0) {
            run(preset, gen);
            return;
        }
        display.render(ActorState.number(String.valueOf(remaining)));
        main.postDelayed(() -> startDelayCountdown(preset, remaining - 1, gen), 1000);
    }

    private void run(ActorPreset preset, int gen) {
        if (gen != generation.get()) {
            return;
        }
        if (preset.isSequence()) {
            runSequence(preset.sequence, 0, gen);
            return;
        }
        display.render(preset.state);
        speak(preset.speech, gen);
        if (preset.gestureRaw != 0) {
            startGesture(preset.gestureRaw, gen);
        }
    }

    private void runSequence(List<ActorPreset.Step> steps, int index, int gen) {
        if (gen != generation.get() || index >= steps.size()) {
            return;
        }
        ActorPreset.Step step = steps.get(index);
        display.render(step.state);
        speak(step.speech, gen);
        if (index < steps.size() - 1) {
            main.postDelayed(() -> runSequence(steps, index + 1, gen), step.holdMs);
        }
    }

    /** Pepper speaks the line (countdown digit, greeting …) in parallel with the pose. */
    private void speak(String text, int gen) {
        if (text == null || gen != generation.get()) {
            return;
        }
        QiContext context = RobotContext.get();
        if (context == null) {
            return; // display still shows; only speech needs the robot
        }
        speechExecutor.execute(() -> {
            if (gen != generation.get()) {
                return;
            }
            try {
                SpeechManager.getInstance().say(context, text);
            } catch (Exception e) {
                Log.w(TAG, "Actor-Speech fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    private void startGesture(int rawRes, int gen) {
        QiContext context = RobotContext.get();
        if (context == null) {
            toast(R.string.actor_not_ready);
            return; // display state still shows; only the physical gesture needs the robot
        }
        playExecutor.execute(() -> {
            try {
                while (gen == generation.get()) {
                    Animation animation = AnimationBuilder.with(context).withResources(rawRes).build();
                    Animate animate = AnimateBuilder.with(context).withAnimation(animation).build();
                    animate.run();
                }
            } catch (Exception e) {
                Log.w(TAG, "Actor-Geste fehlgeschlagen: " + e.getMessage());
            }
        });
    }

    private void stop() {
        newGeneration();
        main.removeCallbacksAndMessages(null);
        hideOverlay();
        toast(R.string.actor_stopped);
    }

    private int newGeneration() {
        main.removeCallbacksAndMessages(null);
        return generation.incrementAndGet();
    }

    private void showOverlay() {
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
    }

    private void hideOverlay() {
        overlay.setVisibility(View.GONE);
        display.release();
        display.render(null);
    }

    // ---------------------------------------------------------------- own image

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            ((Activity) ctx()).startActivityForResult(intent, AdminController.REQUEST_ACTOR_IMAGE);
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
        showImageOverlay(uri);
    }

    private void showImageOverlay(Uri uri) {
        int gen = newGeneration();
        showOverlay();
        imageExecutor.execute(() -> {
            Bitmap bitmap = decodeScaled(uri);
            root.post(() -> {
                if (gen == generation.get() && bitmap != null) {
                    display.showImage(bitmap);
                }
            });
        });
    }

    private Bitmap decodeScaled(Uri uri) {
        int reqW = root.getResources().getDisplayMetrics().widthPixels;
        int reqH = root.getResources().getDisplayMetrics().heightPixels;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = ctx().getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, reqW, reqH);
            try (InputStream in = ctx().getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(in, null, opts);
            }
        } catch (Exception e) {
            Log.w(TAG, "Bild laden fehlgeschlagen: " + e.getMessage());
            return null;
        }
    }

    private int sampleSize(int width, int height, int reqW, int reqH) {
        int sample = 1;
        if (width <= 0 || height <= 0 || reqW <= 0 || reqH <= 0) {
            return sample;
        }
        while ((width / (sample * 2)) >= reqW && (height / (sample * 2)) >= reqH) {
            sample *= 2;
        }
        return sample;
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
