package com.buhlergroup.pepper.action.admin;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.openai.history.HistoryEntry;
import com.buhlergroup.pepper.openai.history.HistoryRole;

import java.util.List;

final class HistoryPanelController {

    private final View root;
    private final PanelNavigator panelNav;
    private final ScrollView historyScroll;
    private final LinearLayout historyContainer;

    HistoryPanelController(View root, PanelNavigator panelNav) {
        this.root = root;
        this.panelNav = panelNav;
        this.historyScroll = root.findViewById(R.id.adminHistoryScroll);
        this.historyContainer = root.findViewById(R.id.adminHistoryContainer);
        root.findViewById(R.id.adminClearHistory).setOnClickListener(v -> onClearHistory());
    }

    void showHistory() {
        historyContainer.removeAllViews();
        List<HistoryEntry> entries = AdminController.get().getConversation();
        if (entries.isEmpty()) {
            TextView empty = new TextView(root.getContext());
            empty.setText(R.string.admin_history_empty);
            empty.setTextColor(0xCCFFFFFF);
            int pad = dp(16);
            empty.setPadding(pad, pad, pad, pad);
            historyContainer.addView(empty);
        } else {
            for (HistoryEntry entry : entries) {
                historyContainer.addView(createBubble(entry));
            }
        }
        panelNav.show(PanelNavigator.PANEL_HISTORY);
        historyScroll.post(() -> historyScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void onClearHistory() {
        new AlertDialog.Builder(root.getContext())
                .setTitle(R.string.admin_history_clear_title)
                .setMessage(R.string.admin_history_clear_message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, w) -> clearHistoryConfirmed())
                .show();
    }

    private void clearHistoryConfirmed() {
        boolean cleared = AdminController.get().clearHistory();
        Toast.makeText(root.getContext(),
                cleared ? R.string.admin_history_cleared : R.string.admin_pin_error,
                Toast.LENGTH_SHORT).show();
        panelNav.show(PanelNavigator.PANEL_MENU);
    }

    private TextView createBubble(HistoryEntry entry) {
        boolean user = entry.getRole() == HistoryRole.USER;
        TextView bubble = new TextView(root.getContext());
        bubble.setText(entry.getContent());
        bubble.setTextColor(0xFFFFFFFF);
        bubble.setBackgroundResource(user ? R.drawable.bg_bubble_user : R.drawable.bg_bubble_assistant);
        bubble.setMaxWidth(dp(560));
        int ph = dp(16);
        int pv = dp(10);
        bubble.setPadding(ph, pv, ph, pv);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = user ? Gravity.END : Gravity.START;
        int margin = dp(6);
        params.setMargins(margin, margin, margin, margin);
        bubble.setLayoutParams(params);
        return bubble;
    }

    private int dp(int value) {
        return Math.round(value * root.getResources().getDisplayMetrics().density);
    }
}
