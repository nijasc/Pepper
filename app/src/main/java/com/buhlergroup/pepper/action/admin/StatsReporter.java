package com.buhlergroup.pepper.action.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.stats.Stats;

import java.util.Map;

final class StatsReporter {

    private final View root;
    private final TextView statsText;

    StatsReporter(View root) {
        this.root = root;
        this.statsText = root.findViewById(R.id.adminStatsText);
    }

    private Context ctx() {
        return root.getContext();
    }

    @SuppressLint("SetTextI18n")
    void refreshStats() {
        statsText.setText(buildStatsReport());
    }

    void exportStats() {
        String report = buildStatsReport();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, ctx().getString(R.string.stats_share_title));
        share.putExtra(Intent.EXTRA_TEXT, report);
        Intent chooser = Intent.createChooser(share, ctx().getString(R.string.stats_export));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx().startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(ctx(), R.string.admin_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private String buildStatsReport() {
        String date = Stats.today();
        Map<String, Integer> day = Stats.forDay(ctx(), date);
        StringBuilder sb = new StringBuilder("Tagesreport " + date + "\n\n");
        sb.append("Interaktionen: ").append(value(day, Stats.INTERACTIONS)).append('\n');
        sb.append("Selfies: ").append(value(day, Stats.SELFIES)).append('\n');
        sb.append("Verlosungs-Beitritte: ").append(value(day, Stats.RAFFLE_JOINS)).append('\n');
        sb.append("Fehler: ").append(value(day, Stats.ERRORS)).append("\n\n");
        sb.append("Aktionen:\n");
        boolean anyAction = false;
        for (Map.Entry<String, Integer> entry : day.entrySet()) {
            if (entry.getKey().startsWith(Stats.ACTION_PREFIX)) {
                anyAction = true;
                sb.append("  ").append(entry.getKey().substring(Stats.ACTION_PREFIX.length()))
                        .append(": ").append(entry.getValue()).append('\n');
            }
        }
        if (!anyAction) {
            sb.append("  –\n");
        }
        return sb.toString();
    }

    private int value(Map<String, Integer> day, String key) {
        Integer v = day.get(key);
        return v != null ? v : 0;
    }
}
