package com.buhlergroup.pepper.action.selfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.camera.CameraSettings;
import com.buhlergroup.pepper.action.camera.WifiCameraManager;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.config.Env;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class SelfieController {

    private static final String TAG = "Selfie";
    private static final int SERVER_PORT = 8080;
    private static final long DISPLAY_MS = 22000;
    private static final long START_TIMEOUT_MS = 15000;
    private static final SelfieController INSTANCE = new SelfieController();

    public interface StateListener {
        void onSelfieStateChanged(boolean active);
    }

    private volatile SelfieView view;
    private volatile LocalImageServer server;
    private volatile boolean running = false;
    private volatile StateListener stateListener;

    private SelfieController() {
    }

    public static SelfieController get() {
        return INSTANCE;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    private void notifyState(boolean active) {
        StateListener l = stateListener;
        if (l != null) {
            l.onSelfieStateChanged(active);
        }
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

    public SelfieEntity takeSelfie(QiContext context) {
        return takeSelfie(context, true);
    }

    public SelfieEntity takeSelfieForRaffle(QiContext context) {
        return takeSelfie(context, false);
    }

    private SelfieEntity takeSelfie(QiContext context, boolean offerRaffle) {
        SelfieView board = view;
        if (board == null) {
            say(context, "Mein Tablet ist gerade nicht bereit, deshalb kann ich kein Selfie machen.");
            return null;
        }
        if (running) {
            say(context, "Wir machen doch gerade schon ein Selfie!");
            return null;
        }

        running = true;
        notifyState(true);
        try {
            if (CameraSettings.isActive(context)) {
                say(context, "Klar, machen wir ein Selfie! Stell dich bitte vor die Kamera.");
                say(context, "Sag «Start», wenn du bereit bist.");
                waitForStart(context);
                say(context, "Drei… Zwei… Eins!");
            } else {
                say(context, "Klar, machen wir ein Selfie! Stell dich vor mich und schau in meine Augen.");
                say(context, "Drei… zwei… eins… lächeln!");
            }

            Bitmap photo = capture(context);
            if (photo == null) {
                if (CameraSettings.isActive(context)) {
                    say(context, "Ich kann die externe Kamera nicht erreichen. Bitte deaktiviere sie im Admin-Dashboard, wenn du meine Kamera verwenden möchtest.");
                } else {
                    say(context, "Hoppla, das Foto hat nicht geklappt. Versuchen wir es später nochmal.");
                }
                return null;
            }

            Bitmap composed = addOverlay(context, photo);
            SelfieEntity entity = SelfieRepository.get(context).save(toJpeg(composed));

            ensureServer(SelfieRepository.get(context).imagesDir());
            String ip = NetworkUtils.localIp(context);
            if (ip == null) {
                say(context, "Ich bin gerade nicht mit dem WLAN verbunden, deshalb kann ich das Selfie nicht teilen.");
                board.hide();
                return null;
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
            if (offerRaffle) {
                offerRaffleJoin(context, entity);
            }
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Selfie failed", e);
            say(context, "Da ist etwas schiefgelaufen mit dem Selfie.");
            board.hide();
        } finally {
            running = false;
            notifyState(false);
        }
        return null;
    }

    public void ensureServerStarted(Context context) {
        try {
            ensureServer(SelfieRepository.get(context).imagesDir());
        } catch (IOException e) {
            Log.w(TAG, "Could not start image server: " + e.getMessage());
        }
    }

    public int serverPort() {
        return SERVER_PORT;
    }

    private void ensureServer(File imagesDir) throws IOException {
        if (server == null) {
            LocalImageServer started = new LocalImageServer(imagesDir, SERVER_PORT);
            started.start();
            server = started;
        }
    }

    private Bitmap capture(QiContext context) {
        if (CameraSettings.isActive(context)) {
            return new WifiCameraManager()
                    .capture(CameraSettings.getIp(context), CameraSettings.getPort(context));
        }
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

    private void waitForStart(QiContext context) {
        try {
            PhraseSet phrases = PhraseSetBuilder.with(context)
                    .withTexts("start", "los", "los geht's", "bereit", "ready").build();
            Listen listen = ListenBuilder.with(context).withPhraseSet(phrases).build();
            Future<ListenResult> future = listen.async().run();
            long deadline = System.currentTimeMillis() + START_TIMEOUT_MS;
            while (!future.isDone() && System.currentTimeMillis() < deadline) {
                Thread.sleep(150);
            }
            if (!future.isDone()) {
                future.requestCancellation();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.w(TAG, "waitForStart failed: " + e.getMessage());
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

    private void offerRaffleJoin(QiContext context, SelfieEntity selfie) {
        try {
            RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();
            if (raffle == null || raffle.status != RaffleStatus.ACTIVE) {
                return;
            }
            say(context, "Übrigens, gerade läuft unsere Verlosung. Wenn du teilnehmen möchtest, "
                    + "trag dich einfach auf meinem Tablet ein – oder tippe auf Abbrechen.");
            RaffleJoinController.get().join(context, raffle, selfie.id);
        } catch (Exception e) {
            Log.w(TAG, "Raffle offer after selfie failed: " + e.getMessage());
        }
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }
}
