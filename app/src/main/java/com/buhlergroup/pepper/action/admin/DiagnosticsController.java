package com.buhlergroup.pepper.action.admin;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.debug.DebugLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class DiagnosticsController {

    private static final String TAG = "AdminView";

    private final View root;
    private final PanelNavigator panelNav;
    private final TextView devLogText;
    private final ScrollView devLogScroll;
    private final CheckBox debugEnabled;
    private final TextView debugStatus;
    private final TextView debugLogText;
    private final ScrollView debugLogScroll;

    DiagnosticsController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.devLogText = root.findViewById(R.id.adminDevLogText);
        this.devLogScroll = root.findViewById(R.id.adminDevLogScroll);
        this.debugEnabled = root.findViewById(R.id.debugEnabled);
        this.debugStatus = root.findViewById(R.id.debugStatus);
        this.debugLogText = root.findViewById(R.id.adminDebugText);
        this.debugLogScroll = root.findViewById(R.id.adminDebugScroll);

        debugEnabled.setOnClickListener(v ->
                DebugLog.get().setEnabled(root.getContext(), debugEnabled.isChecked()));
        root.findViewById(R.id.debugRefresh).setOnClickListener(v -> renderDebugLog());
        root.findViewById(R.id.debugExport).setOnClickListener(v -> exportDebugLog());
        root.findViewById(R.id.debugClear).setOnClickListener(v -> clearDebugLog());
    }

    void showDevLog() {
        List<String> log = AdminController.get().getDevLog();
        StringBuilder text = new StringBuilder();
        if (log.isEmpty()) {
            text.append(root.getContext().getString(R.string.admin_devlog_empty));
        } else {
            for (String entry : log) {
                text.append(entry).append('\n');
            }
        }
        devLogText.setText(text.toString());
        panelNav.show(PanelNavigator.PANEL_DEVLOG);
        devLogScroll.post(() -> devLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    void showDebug() {
        debugEnabled.setChecked(DebugLog.get().isEnabled());
        renderDebugLog();
        panelNav.show(PanelNavigator.PANEL_DEBUG);
    }

    private void renderDebugLog() {
        String status = DebugLog.get().getStatus();
        debugStatus.setText(root.getContext().getString(R.string.debug_status_label) + " "
                + (status.isEmpty() ? "—" : status));
        List<String> log = DebugLog.get().snapshot();
        if (log.isEmpty()) {
            debugLogText.setText(R.string.debug_empty);
        } else {
            StringBuilder text = new StringBuilder();
            for (String entry : log) {
                text.append(entry).append('\n');
            }
            debugLogText.setText(text.toString());
        }
        debugLogScroll.post(() -> debugLogScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void clearDebugLog() {
        DebugLog.get().clear();
        renderDebugLog();
    }

    private void exportDebugLog() {
        String content = DebugLog.get().export();
        File file = new File(root.getContext().getCacheDir(), "pepper_debug_log.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.w(TAG, "Debug-Export fehlgeschlagen: " + e.getMessage());
            Toast.makeText(root.getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        shareDebugLog(file);
    }

    private void shareDebugLog(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(root.getContext(),
                    root.getContext().getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, root.getContext().getString(R.string.debug_export_title));
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(share, root.getContext().getString(R.string.debug_export));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            root.getContext().startActivity(chooser);
        } catch (Exception e) {
            Log.w(TAG, "Debug-Log teilen fehlgeschlagen: " + e.getMessage());
            Toast.makeText(root.getContext(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
