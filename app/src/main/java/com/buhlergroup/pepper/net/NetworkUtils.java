package com.buhlergroup.pepper.net;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Locale;

public final class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    private NetworkUtils() {
    }

    @Nullable
    public static String localIp(Context context) {
        InetAddress addr = localInetAddress(context);
        return addr != null ? addr.getHostAddress() : null;
    }

    public static InetAddress localInetAddress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                int ip = wm.getConnectionInfo().getIpAddress();
                if (ip != 0) {
                    String text = String.format(Locale.US, "%d.%d.%d.%d",
                            ip & 0xff, (ip >> 8) & 0xff, (ip >> 16) & 0xff, (ip >> 24) & 0xff);
                    return InetAddress.getByName(text);
                }
            }
        } catch (UnknownHostException | RuntimeException e) {
            Log.w(TAG, "WifiManager IP lookup failed: " + e.getMessage());
        }

        InetAddress preferred = null;
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (!(addr instanceof Inet4Address) || !addr.isSiteLocalAddress()) {
                        continue;
                    }
                    if (isAccessPointInterface(nif.getName())) {
                        return addr;
                    }
                    if (preferred == null) {
                        preferred = addr;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "NetworkInterface IP lookup failed: " + e.getMessage());
        }
        return preferred;
    }

    private static boolean isAccessPointInterface(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.US);
        return lower.startsWith("wlan") || lower.startsWith("ap") || lower.contains("softap") || lower.startsWith("swlan");
    }
}
