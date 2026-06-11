package com.buhler.funktionierender_pepper.action.selfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.buhler.funktionierender_pepper.R;
import com.buhler.funktionierender_pepper.action.selfie.data.SelfieEntity;
import com.buhler.funktionierender_pepper.config.Env;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class SelfieController {

    private static final String TAG = "Selfie";
    private static final int SERVER_PORT = 8080;
    private static final long DISPLAY_MS = 22000;
    private static final SelfieController INSTANCE = new SelfieController();

    private volatile SelfieView view;
    private volatile LocalImageServer server;
    private volatile boolean running = false;

    private SelfieController() {
    }

    public static SelfieController get() {
        return INSTANCE;
    }

    public void attachView(SelfieView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void stopServer() {
        LocalImageServer current = server;
        if (current != null) {
            current.stop();
            server = null;
        }
    }

    public void takeSelfie(QiContext context) {
        SelfieView board = view;
        if (board == null) {
            say(context, "Mein Tablet ist gerade nicht bereit, deshalb kann ich kein Selfie machen.");
            return;
        }
        if (running) {
            say(context, "Wir machen doch gerade schon ein Selfie!");
            return;
        }

        running = true;
        try {
            say(context, "Klar, machen wir ein Selfie! Stell dich neben mich und schau in meine Augen.");
            say(context, "Drei… zwei… eins… lächeln!");

            Bitmap photo = capture(context);
            if (photo == null) {
                say(context, "Hoppla, das Foto hat nicht geklappt. Versuchen wir es später nochmal.");
                return;
            }

            Bitmap composed = addOverlay(context, photo);
            SelfieEntity entity = SelfieRepository.get(context).save(toJpeg(composed));

            ensureServer(SelfieRepository.get(context).imagesDir());
            String ip = localIp(context);
            if (ip == null) {
                say(context, "Ich bin gerade nicht mit dem WLAN verbunden, deshalb kann ich das Selfie nicht teilen.");
                board.hide();
                return;
            }

            String url = "http://" + ip + ":" + SERVER_PORT + "/" + entity.filename;
            Bitmap qr = QrGenerator.encode(url, 600);
            Bitmap wifiQr = buildWifiQr(context);

            CountDownLatch dismiss = new CountDownLatch(1);
            board.setOnCloseListener(dismiss::countDown);
            board.setStatus("Dein Selfie ist bereit!");
            board.show(composed, qr, wifiQr);
            Log.i(TAG, "Selfie #" + entity.number + " at " + url);

            if (wifiQr != null) {
                say(context, "Fertig! Scanne den oberen QR-Code für dein Bild. "
                        + "Über den unteren Code kannst du dich mit meinem WLAN verbinden. "
                        + "Tippe auf Okay, wenn du fertig bist.");
            } else {
                say(context, "Fertig! Scanne den QR-Code auf meinem Tablet, dann kannst du dein Selfie herunterladen. "
                        + "Tippe auf Okay, wenn du fertig bist.");
            }

            try {
                dismiss.await(DISPLAY_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            board.setOnCloseListener(null);
            board.hide();
        } catch (Exception e) {
            Log.e(TAG, "Selfie failed", e);
            say(context, "Da ist etwas schiefgelaufen mit dem Selfie.");
            board.hide();
        } finally {
            running = false;
        }
    }

    private void ensureServer(File imagesDir) throws IOException {
        if (server == null) {
            LocalImageServer started = new LocalImageServer(imagesDir, SERVER_PORT);
            started.start();
            server = started;
        }
    }

    private Bitmap capture(QiContext context) {
        try {
            TakePicture takePicture = TakePictureBuilder.with(context).build();
            TimestampedImageHandle handle = takePicture.run();
            EncodedImage encoded = handle.getImage().getValue();
            ByteBuffer buffer = encoded.getData();
            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Capture failed", e);
            return null;
        }
    }

    private Bitmap addOverlay(Context context, Bitmap photo) {
        Bitmap result = photo.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.pepper_selfie_overlay);
        if (overlay == null) {
            return result;
        }

        Canvas canvas = new Canvas(result);
        int targetWidth = Math.round(result.getWidth() * 0.34f);
        float scale = targetWidth / (float) overlay.getWidth();
        int targetHeight = Math.round(overlay.getHeight() * scale);
        int margin = Math.round(result.getWidth() * 0.03f);
        int left = result.getWidth() - targetWidth - margin;
        int top = result.getHeight() - targetHeight - margin;
        Rect dst = new Rect(left, top, left + targetWidth, top + targetHeight);
        canvas.drawBitmap(overlay, null, dst, null);
        overlay.recycle();
        return result;
    }

    private byte[] toJpeg(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        return bos.toByteArray();
    }

    private Bitmap buildWifiQr(Context context) {
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

    private String escapeWifi(String value) {
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:")
                .replace("\"", "\\\"");
    }

    private String localIp(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                int ip = wm.getConnectionInfo().getIpAddress();
                if (ip != 0) {
                    return String.format(Locale.US, "%d.%d.%d.%d",
                            ip & 0xff, (ip >> 8) & 0xff, (ip >> 16) & 0xff, (ip >> 24) & 0xff);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "WifiManager IP lookup failed: " + e.getMessage());
        }

        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "NetworkInterface IP lookup failed: " + e.getMessage());
        }
        return null;
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }
}
