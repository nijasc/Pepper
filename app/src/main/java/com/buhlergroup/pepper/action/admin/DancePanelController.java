package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.ArrayAdapter;
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
    private final DanceRepository repository = new DanceRepository();
    private final List<String> danceIds = new ArrayList<>();

    DancePanelController(View root, Executor executor, PanelNavigator panelNav, Runnable openLibrary) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.danceDefault = root.findViewById(R.id.danceDefault);
        root.findViewById(R.id.danceSave).setOnClickListener(v -> saveDance());
        root.findViewById(R.id.danceOpenLibrary).setOnClickListener(v -> openLibrary.run());
    }

    void showDance() {
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
        danceDefault.setSelection(Math.max(index, 0));
    }

    private void saveDance() {
        int pos = danceDefault.getSelectedItemPosition();
        String id = pos >= 0 && pos < danceIds.size() ? danceIds.get(pos) : "";
        DanceSettings.save(root.getContext(), id);
        Toast.makeText(root.getContext(), R.string.dance_settings_saved, Toast.LENGTH_SHORT).show();
    }
}
