package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.DanceRepository;
import com.buhlergroup.pepper.action.dance.DanceSettings;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

final class DancePanelController {

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final Spinner danceDefault;
    private final EditText danceDuration;
    private final DanceRepository repository = new DanceRepository();
    private final List<String> danceIds = new ArrayList<>();

    DancePanelController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.danceDefault = root.findViewById(R.id.danceDefault);
        this.danceDuration = root.findViewById(R.id.danceDuration);
        root.findViewById(R.id.danceSave).setOnClickListener(v -> saveDance());
    }

    void showDance() {
        danceDuration.setText(String.valueOf(DanceSettings.getDefaultDurationSeconds(root.getContext())));
        panelNav.show(PanelNavigator.PANEL_DANCE);
        loadDances();
    }

    private void loadDances() {
        executor.execute(() -> {
            List<DanceEntity> dances = repository.all(root.getContext());
            root.post(() -> populate(dances));
        });
    }

    private void populate(List<DanceEntity> dances) {
        danceIds.clear();
        List<String> labels = new ArrayList<>();
        danceIds.add("");
        labels.add(root.getContext().getString(R.string.dance_default_none));
        for (DanceEntity dance : dances) {
            danceIds.add(dance.youtubeId);
            labels.add(dance.songName);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        danceDefault.setAdapter(adapter);
        int index = danceIds.indexOf(DanceSettings.getDefaultDanceId(root.getContext()));
        danceDefault.setSelection(index >= 0 ? index : 0);
    }

    private void saveDance() {
        int pos = danceDefault.getSelectedItemPosition();
        String id = pos >= 0 && pos < danceIds.size() ? danceIds.get(pos) : "";
        int duration = parseIntOr(danceDuration, DanceSettings.DEFAULT_DURATION_SECONDS);
        DanceSettings.save(root.getContext(), id, duration);
        Toast.makeText(root.getContext(), R.string.dance_settings_saved, Toast.LENGTH_SHORT).show();
    }

    private int parseIntOr(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
