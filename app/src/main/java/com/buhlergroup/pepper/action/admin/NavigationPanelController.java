package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.navigation.NavigationSettings;
import com.buhlergroup.pepper.action.navigation.data.NavigationDatabase;
import com.buhlergroup.pepper.action.navigation.data.RoomScanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

final class NavigationPanelController {

    private final View root;
    private final Executor executor;
    private final PanelNavigator panelNav;
    private final CheckBox navAutoLocalize;
    private final Spinner navDefaultScan;
    private final EditText navLocalizeTimeout;
    private final List<String> scanIds = new ArrayList<>();

    NavigationPanelController(View root, Executor executor, PanelNavigator panelNav) {
        this.root = root;
        this.executor = executor;
        this.panelNav = panelNav;
        this.navAutoLocalize = root.findViewById(R.id.navAutoLocalize);
        this.navDefaultScan = root.findViewById(R.id.navDefaultScan);
        this.navLocalizeTimeout = root.findViewById(R.id.navLocalizeTimeout);
        root.findViewById(R.id.navSave).setOnClickListener(v -> saveNavigation());
    }

    void showNavigation() {
        navAutoLocalize.setChecked(NavigationSettings.isAutoLocalize(root.getContext()));
        navLocalizeTimeout.setText(
                String.valueOf(NavigationSettings.getLocalizeTimeoutSeconds(root.getContext())));
        panelNav.show(PanelNavigator.PANEL_NAV);
        loadScans();
    }

    private void loadScans() {
        executor.execute(() -> {
            List<RoomScanEntity> scans =
                    NavigationDatabase.get(root.getContext()).navigationDao().getScans();
            root.post(() -> populate(scans));
        });
    }

    private void populate(List<RoomScanEntity> scans) {
        scanIds.clear();
        List<String> labels = new ArrayList<>();
        scanIds.add("");
        labels.add(root.getContext().getString(R.string.nav_default_scan_none));
        for (RoomScanEntity scan : scans) {
            scanIds.add(scan.id);
            labels.add(scan.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        navDefaultScan.setAdapter(adapter);
        int index = scanIds.indexOf(NavigationSettings.getDefaultScanId(root.getContext()));
        navDefaultScan.setSelection(Math.max(index, 0));
    }

    private void saveNavigation() {
        int pos = navDefaultScan.getSelectedItemPosition();
        String id = pos >= 0 && pos < scanIds.size() ? scanIds.get(pos) : "";
        int timeout = parseIntOr(navLocalizeTimeout);
        NavigationSettings.save(root.getContext(), navAutoLocalize.isChecked(), id, timeout);
        Toast.makeText(root.getContext(), R.string.nav_settings_saved, Toast.LENGTH_SHORT).show();
    }

    private int parseIntOr(EditText field) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return NavigationSettings.DEFAULT_LOCALIZE_TIMEOUT_SECONDS;
        }
    }
}
