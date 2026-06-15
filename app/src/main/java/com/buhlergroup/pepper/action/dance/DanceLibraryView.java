package com.buhlergroup.pepper.action.dance;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DanceLibraryView extends FrameLayout {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ExecutorService heavyExecutor = Executors.newSingleThreadExecutor();
    private final DanceRepository repository = new DanceRepository();
    private LinearLayout list;

    public DanceLibraryView(Context context) {
        super(context);
        init(context);
    }

    public DanceLibraryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DanceLibraryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_dance_library, this, true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.game_overlay));
        setClickable(true);
        setFocusable(true);
        list = findViewById(R.id.danceList);
        findViewById(R.id.danceClose).setOnClickListener(v -> DanceLibraryController.get().close());
    }

    public void open() {
        post(() -> {
            setVisibility(VISIBLE);
            bringToFront();
            refresh();
        });
    }

    public void hide() {
        post(() -> setVisibility(GONE));
    }

    private void refresh() {
        executor.execute(() -> {
            try {
                List<DanceEntity> dances = repository.all(getContext());
                post(() -> render(dances));
            } catch (Exception e) {
                post(() -> toast(getContext().getString(R.string.dance_load_failed)));
            }
        });
    }

    private void render(List<DanceEntity> dances) {
        list.removeAllViews();
        if (dances == null || dances.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.dance_empty);
            empty.setTextColor(0xCCFFFFFF);
            empty.setTextSize(16);
            list.addView(empty);
            return;
        }
        for (DanceEntity dance : dances) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.topMargin = dp(6);
            row.setLayoutParams(rowParams);

            TextView label = new TextView(getContext());
            label.setText((dance.favorite ? "★ " : "") + dance.songName);
            label.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            label.setTextSize(18);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(label);

            row.addView(pill("Abspielen", R.drawable.bg_pill_teal, v -> playDance(dance)));
            row.addView(pill("KI-Edit", R.drawable.bg_pill_teal,
                    v -> DanceLibraryController.get().requestVoiceEdit(dance)));
            row.addView(pill(getContext().getString(R.string.dance_favorite),
                    R.drawable.bg_pill_teal, v -> toggleFavorite(dance)));
            row.addView(pill(getContext().getString(R.string.dance_rename),
                    R.drawable.bg_pill_teal, v -> promptRename(dance)));
            row.addView(pill(getContext().getString(R.string.dance_delete),
                    R.drawable.bg_pill_red, v -> delete(dance)));
            list.addView(row);
        }
    }

    private void playDance(DanceEntity dance) {
        com.aldebaran.qi.sdk.QiContext qiContext = RobotContext.get();
        if (qiContext == null) {
            toast("Roboter ist gerade nicht bereit.");
            return;
        }
        toast("Spiele " + dance.songName);
        heavyExecutor.execute(() -> {
            try {
                repository.preparePlayback(dance);
                DancePlayback.play(qiContext, dance);
            } catch (Exception e) {
                post(() -> toast("Abspielen fehlgeschlagen: " + e.getMessage()));
            }
        });
    }

    public void applyAiEdit(DanceEntity dance, String instruction) {
        toast("Bearbeite \"" + dance.songName + "\"...");
        heavyExecutor.execute(() -> {
            try {
                repository.aiEdit(getContext(), dance, instruction);
                post(() -> {
                    toast("Tanz aktualisiert.");
                    refresh();
                });
            } catch (Exception e) {
                post(() -> toast("Bearbeitung fehlgeschlagen: " + e.getMessage()));
            }
        });
    }

    private void toggleFavorite(DanceEntity dance) {
        executor.execute(() -> {
            repository.setFavorite(getContext(), dance.youtubeId, !dance.favorite);
            refresh();
        });
    }

    private void delete(DanceEntity dance) {
        executor.execute(() -> {
            repository.delete(getContext(), dance);
            refresh();
        });
    }

    private void promptRename(DanceEntity dance) {
        EditText input = new EditText(getContext());
        input.setText(dance.songName);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dance_rename)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        executor.execute(() -> {
                            repository.rename(getContext(), dance.youtubeId, name);
                            refresh();
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private TextView pill(String text, int bgRes, OnClickListener onClick) {
        TextView pill = new TextView(getContext());
        pill.setText(text);
        pill.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        pill.setTextSize(15);
        pill.setGravity(Gravity.CENTER);
        pill.setBackgroundResource(bgRes);
        pill.setPadding(dp(18), dp(10), dp(18), dp(10));
        pill.setClickable(true);
        pill.setFocusable(true);
        pill.setOnClickListener(onClick);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(dp(8));
        pill.setLayoutParams(params);
        return pill;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
