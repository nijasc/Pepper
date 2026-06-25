package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.TextView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.debug.DebugLog;

import java.util.LinkedHashMap;
import java.util.Map;

final class PanelNavigator {

    public static final int PANEL_PIN = 0;
    public static final int PANEL_MENU = 1;
    public static final int PANEL_DEVLOG = 2;
    public static final int PANEL_GALLERY = 3;
    public static final int PANEL_DETAIL = 4;
    public static final int PANEL_LANG = 5;
    public static final int PANEL_HISTORY = 6;
    public static final int PANEL_RAFFLE_CREATE = 7;
    public static final int PANEL_RAFFLE = 8;
    public static final int PANEL_CAMERA = 9;
    public static final int PANEL_STATUS = 10;
    public static final int PANEL_STATS = 11;
    public static final int PANEL_SYSTEM = 12;
    public static final int PANEL_DEBUG = 13;
    public static final int PANEL_DANCE = 14;
    public static final int PANEL_NAV = 15;
    public static final int PANEL_PROFILES = 16;
    public static final int PANEL_PROFILE_EDIT = 17;
    public static final int PANEL_MODELS = 18;
    public static final int PANEL_ACTOR = 19;
    private static final String TAG = "AdminNav";
    private final Map<Integer, View> panels = new LinkedHashMap<>();
    private final View header;
    private final TextView headerTitle;
    private final OnPanelShown onPanelShown;
    private int currentPanel = PANEL_PIN;

    PanelNavigator(View header, TextView headerTitle, OnPanelShown onPanelShown) {
        this.header = header;
        this.headerTitle = headerTitle;
        this.onPanelShown = onPanelShown;
    }

    void register(int which, View panel) {
        panels.put(which, panel);
    }

    int current() {
        return currentPanel;
    }

    void show(int which) {
        DebugLog.get().d(TAG, "Panel anzeigen: " + which);
        currentPanel = which;
        for (Map.Entry<Integer, View> entry : panels.entrySet()) {
            entry.getValue().setVisibility(entry.getKey() == which ? View.VISIBLE : View.GONE);
        }
        if (onPanelShown != null) {
            onPanelShown.onPanelShown(which);
        }
        updateHeader(which);
    }

    private void updateHeader(int which) {
        boolean show = which != PANEL_PIN && which != PANEL_MENU;
        header.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            headerTitle.setText(titleFor(which));
            header.bringToFront();
        }
    }

    private int titleFor(int which) {
        switch (which) {
            case PANEL_DEVLOG:
                return R.string.admin_dev_logs;
            case PANEL_GALLERY:
            case PANEL_DETAIL:
                return R.string.admin_selfies;
            case PANEL_LANG:
                return R.string.admin_language;
            case PANEL_HISTORY:
                return R.string.admin_history_view;
            case PANEL_RAFFLE_CREATE:
                return R.string.raffle_create_title;
            case PANEL_RAFFLE:
                return R.string.admin_raffle;
            case PANEL_CAMERA:
                return R.string.admin_camera_title;
            case PANEL_STATUS:
                return R.string.admin_status;
            case PANEL_STATS:
                return R.string.admin_stats;
            case PANEL_SYSTEM:
                return R.string.admin_system;
            case PANEL_DEBUG:
                return R.string.admin_debug_title;
            case PANEL_DANCE:
                return R.string.admin_dance_settings;
            case PANEL_NAV:
                return R.string.admin_navigation_settings;
            case PANEL_PROFILES:
                return R.string.admin_profiles;
            case PANEL_PROFILE_EDIT:
                return R.string.profile_edit_title;
            case PANEL_MODELS:
                return R.string.admin_models;
            case PANEL_ACTOR:
                return R.string.admin_actor;
            default:
                return R.string.admin_menu_title;
        }
    }

    interface OnPanelShown {
        void onPanelShown(int which);
    }
}
