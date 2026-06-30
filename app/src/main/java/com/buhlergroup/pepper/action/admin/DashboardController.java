package com.buhlergroup.pepper.action.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.PepperApplication;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.net.Connectivity;
import com.buhlergroup.pepper.net.NetworkUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;

final class DashboardController {

    private static final long DASH_REFRESH_MS = 15000;

    private final View root;
    private final Executor executor;
    private final Handler dashHandler = new Handler(Looper.getMainLooper());
    private final TextView dashWifi;
    private final TextView dashOpenAi;
    private final TextView dashBattery;
    private final TextView dashUptime;
    private final Runnable dashRefresh = new Runnable() {
        @Override
        public void run() {
            refreshDashboard();
            dashHandler.postDelayed(this, DASH_REFRESH_MS);
        }
    };
    private final TextView statusWifi;
    private final TextView statusOpenAi;
    private final TextView statusBattery;
    private final TextView statusUptime;

    DashboardController(View root, Executor executor) {
        this.root = root;
        this.executor = executor;
        this.dashWifi = root.findViewById(R.id.dashWifi);
        this.dashOpenAi = root.findViewById(R.id.dashOpenAi);
        this.dashBattery = root.findViewById(R.id.dashBattery);
        this.dashUptime = root.findViewById(R.id.dashUptime);
        this.statusWifi = root.findViewById(R.id.statusWifi);
        this.statusOpenAi = root.findViewById(R.id.statusOpenAi);
        this.statusBattery = root.findViewById(R.id.statusBattery);
        this.statusUptime = root.findViewById(R.id.statusUptime);
    }

    private Context ctx() {
        return root.getContext();
    }

    void startRefresh() {
        dashHandler.post(dashRefresh);
    }

    void stopRefresh() {
        dashHandler.removeCallbacks(dashRefresh);
    }

    @SuppressLint("SetTextI18n")
    void refreshDashboard() {
        boolean online = Connectivity.isOnline(ctx());
        dashWifi.setText(online ? R.string.status_dash_connected : R.string.status_dash_disconnected);
        dashWifi.setTextColor(ContextCompat.getColor(ctx(),
                online ? R.color.status_ok : R.color.status_bad));

        int pct = batteryPercent();
        if (pct < 0) {
            dashBattery.setText("–");
            dashBattery.setTextColor(ContextCompat.getColor(ctx(), R.color.text_primary));
        } else {
            dashBattery.setText(pct + "%");
            int color = pct >= 30 ? R.color.status_ok : pct >= 15 ? R.color.status_warn : R.color.status_bad;
            dashBattery.setTextColor(ContextCompat.getColor(ctx(), color));
        }

        dashUptime.setText(uptimeText());
        dashUptime.setTextColor(ContextCompat.getColor(ctx(), R.color.text_primary));

        dashOpenAi.setText("…");
        dashOpenAi.setTextColor(ContextCompat.getColor(ctx(), R.color.text_muted));
        executor.execute(() -> {
            boolean reachable = isOpenAiReachable();
            root.post(() -> {
                dashOpenAi.setText(reachable ? R.string.status_dash_ok : R.string.status_dash_fail);
                dashOpenAi.setTextColor(ContextCompat.getColor(ctx(),
                        reachable ? R.color.status_ok : R.color.status_bad));
            });
        });
    }

    void refreshStatus() {
        String ip = NetworkUtils.localIp(ctx());
        boolean online = Connectivity.isOnline(ctx());
        statusWifi.setText(online && ip != null
                ? ctx().getString(R.string.status_wifi_connected, ip)
                : ctx().getString(R.string.status_wifi_disconnected));
        statusBattery.setText(batteryStatusText());
        statusUptime.setText(ctx().getString(R.string.status_uptime, uptimeText()));
        statusOpenAi.setText(R.string.status_openai_checking);
        executor.execute(() -> {
            boolean reachable = isOpenAiReachable();
            root.post(() -> statusOpenAi.setText(
                    reachable ? R.string.status_openai_ok : R.string.status_openai_fail));
        });
    }

    private int batteryPercent() {
        Intent battery = ctx().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) {
            return -1;
        }
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) {
            return -1;
        }
        return Math.round(level * 100f / scale);
    }

    private String batteryStatusText() {
        Intent battery = ctx().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null) {
            return ctx().getString(R.string.status_battery_unknown);
        }
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) {
            return ctx().getString(R.string.status_battery_unknown);
        }
        return ctx().getString(R.string.status_battery, Math.round(level * 100f / scale));
    }

    private String uptimeText() {
        long elapsedMs = SystemClock.elapsedRealtime() - PepperApplication.startElapsedMs();
        long totalMinutes = Math.max(0, elapsedMs / 60000);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + " h " + minutes + " min";
    }

    private boolean isOpenAiReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("api.openai.com", 443), 4000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
