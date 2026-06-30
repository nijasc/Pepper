package com.buhlergroup.pepper.action.selfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.buhlergroup.pepper.config.Env;
import com.buhlergroup.pepper.util.QrGenerator;

/**
 * Builds the WiFi-join QR payload shown next to a finished selfie. Pure string
 * assembly plus QR encoding; extracted from {@link SelfieController}.
 */
final class WifiQrFactory {

    private static final String TAG = "Selfie";

    private WifiQrFactory() {
    }

    static Bitmap buildWifiQr(Context context) {
        String ssid = Env.get(context, "PEPPER_WIFI_SSID", "");
        if (ssid.isEmpty()) {
            return null;
        }
        String password = Env.get(context, "PEPPER_WIFI_PASSWORD", "");
        String type = password.isEmpty() ? "nopass" : "WPA";
        StringBuilder payload = new StringBuilder("WIFI:T:").append(type)
                .append(";S:").append(escapeWifi(ssid)).append(";");
        if (!password.isEmpty()) {
            payload.append("P:").append(escapeWifi(password)).append(";");
        }
        payload.append(";");
        try {
            return QrGenerator.encode(payload.toString(), 400);
        } catch (Exception e) {
            Log.w(TAG, "WiFi QR generation failed: " + e.getMessage());
            return null;
        }
    }

    static String escapeWifi(String value) {
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:")
                .replace("\"", "\\\"");
    }
}
