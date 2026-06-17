package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.attract.AttractSettings;

final class AttractPanelController {

    private final View root;
    private final PanelNavigator panelNav;
    private final CheckBox attractEnabled;
    private final EditText attractIdle;
    private final EditText attractGreet;

    AttractPanelController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.attractEnabled = root.findViewById(R.id.attractEnabled);
        this.attractIdle = root.findViewById(R.id.attractIdle);
        this.attractGreet = root.findViewById(R.id.attractGreet);
        root.findViewById(R.id.attractSave).setOnClickListener(v -> saveAttract());
    }

    void showAttract() {
        attractEnabled.setChecked(AttractSettings.isEnabled(root.getContext()));
        attractIdle.setText(String.valueOf(AttractSettings.getIdleMinutes(root.getContext())));
        attractGreet.setText(String.valueOf(AttractSettings.getGreetSeconds(root.getContext())));
        panelNav.show(PanelNavigator.PANEL_ATTRACT);
    }

    private void saveAttract() {
        int idle = parseIntOr(attractIdle, AttractSettings.DEFAULT_IDLE_MINUTES);
        int greet = parseIntOr(attractGreet, AttractSettings.DEFAULT_GREET_SECONDS);
        AttractSettings.save(root.getContext(), attractEnabled.isChecked(), idle, greet);
        Toast.makeText(root.getContext(), R.string.attract_saved, Toast.LENGTH_SHORT).show();
    }

    private int parseIntOr(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
