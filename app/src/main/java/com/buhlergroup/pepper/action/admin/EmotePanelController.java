package com.buhlergroup.pepper.action.admin;

import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

/**
 * Admin "Emotes" panel: manually trigger an emote (gesture animation) that repeats for
 * a chosen duration (default 60s). Built for the Bühler Future Marketing video so a
 * gesture (e.g. Winken, einladende Geste) can be triggered on demand and held long
 * enough to film. Loops by re-running the animation on a worker thread until the
 * duration elapses, so the single-animation ~30s cap of {@code QianimLooper} does not
 * apply. A Stop button cancels early.
 */
final class EmotePanelController {

    private static final String TAG = "EmotePanel";
    private static final int DURATION_SECONDS = 60;

    private static final int[] EMOTE_RAW = {
            R.raw.gesture_wave_hand,
            R.raw.gesture_welcome_arm,
            R.raw.pepper_highfive,
            R.raw.raise_right_hand_b001,
            R.raw.gesture_hands_up
    };
    private static final int[] EMOTE_LABEL = {
            R.string.emote_wave,
            R.string.emote_welcome,
            R.string.emote_highfive,
            R.string.emote_raise_hand,
            R.string.emote_hands_up
    };

    private final View root;
    private final PanelNavigator panelNav;
    private final Spinner emoteSelect;
    private final ExecutorService playExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean cancel = new AtomicBoolean(false);
    private volatile boolean playing = false;

    EmotePanelController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.emoteSelect = root.findViewById(R.id.emoteSelect);
        this.emoteSelect.setPopupBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(root.getContext(), R.color.admin_card)));
        root.findViewById(R.id.emotePlay).setOnClickListener(v -> playSelected());
        root.findViewById(R.id.emoteStop).setOnClickListener(v -> stop());
    }

    void showEmotes() {
        String[] labels = new String[EMOTE_LABEL.length];
        for (int i = 0; i < EMOTE_LABEL.length; i++) {
            labels[i] = root.getContext().getString(EMOTE_LABEL[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(),
                R.layout.spinner_item_admin, labels);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_admin);
        emoteSelect.setAdapter(adapter);
        panelNav.show(PanelNavigator.PANEL_EMOTES);
    }

    private void playSelected() {
        if (playing) {
            toast(R.string.emote_already_playing);
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
        long durationMs = DURATION_SECONDS * 1000L;

        cancel.set(false);
        playing = true;
        toast(R.string.emote_playing);
        playExecutor.execute(() -> runLoop(context, rawRes, durationMs));
    }

    private void runLoop(QiContext context, int rawRes, long durationMs) {
        long end = System.currentTimeMillis() + durationMs;
        try {
            while (!cancel.get() && System.currentTimeMillis() < end) {
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
            root.post(() -> toast(R.string.emote_stopped));
        }
    }

    private void stop() {
        cancel.set(true);
    }

    private void toast(int resId) {
        Toast.makeText(root.getContext(), resId, Toast.LENGTH_SHORT).show();
    }
}
