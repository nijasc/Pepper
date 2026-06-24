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
import com.buhlergroup.pepper.action.navigation.NavigationManager;
import com.buhlergroup.pepper.action.raffle.RaffleJoinController;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.config.Env;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SelfieController {

    private static final String TAG = "Selfie";
    private static final long DISPLAY_MS = 22000;
    private static final long START_TIMEOUT_MS = 15000;
    private static final int MAX_CAPTURES = 3;
    private static final long PREVIEW_TIMEOUT_MS = 20000;
    private static final int MAX_PHOTO_EDGE = 1920;
    private static final SelfieController INSTANCE = new SelfieController();
    private final SelfieShareServer shareServer = new SelfieShareServer();
    private volatile SelfieView view;
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
        shareServer.stop();
    }

    public void acquireServer(Context context) {
        shareServer.acquire(context);
    }

    public void releaseServer() {
        shareServer.release();
    }

    public void takeSelfie(QiContext context) {
        takeSelfie(context, true);
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
        DebugLog.get().setStatus("Selfie – gestartet");
        DebugLog.get().i(TAG, "Selfie gestartet");
        boolean serverAcquired = false;
        try {
            if (NavigationManager.get().hasFotostand(context)) {
                say(context, "Komm mit, ich fahre uns kurz zum Fotostand.");
                NavigationManager.get().driveToFotostandIfPossible(context);
            }
            boolean externalCam = CameraSettings.isActive(context);
            if (externalCam) {
                say(context, "Klar, machen wir ein Selfie! Stell dich bitte vor die Kamera.");
            } else {
                say(context, "Klar, machen wir ein Selfie! Stell dich vor mich und schau in meine Augen.");
            }

            SelfieEntity entity;
            while (true) {
                Bitmap composed = captureConfirmed(context, board, externalCam);
                if (composed == null) {
                    board.hide();
                    return null;
                }

                entity = SelfieRepository.get(context).save(toJpeg(composed));

                String ip = NetworkUtils.localIp(context);
                if (ip == null) {
                    say(context, "Ich bin gerade nicht mit dem WLAN verbunden, deshalb kann ich das Selfie nicht teilen.");
                    board.hide();
                    return null;
                }
                acquireServer(context);
                serverAcquired = true;

                String url = downloadUrl(context, entity.filename);
                Bitmap qr = QrGenerator.encode(url, 600);
                Bitmap wifiQr = buildWifiQr(context);

                AtomicBoolean retake = new AtomicBoolean(false);
                CountDownLatch dismiss = new CountDownLatch(1);
                board.setOnCloseListener(dismiss::countDown);
                board.setStatus("Dein Selfie ist bereit!");
                board.show(composed, qr, wifiQr, () -> {
                    retake.set(true);
                    dismiss.countDown();
                });
                Log.i(TAG, "Selfie #" + entity.number + " at " + url);
                DebugLog.get().setStatus("Selfie #" + entity.number + " bereit");
                DebugLog.get().i(TAG, "Selfie #" + entity.number + " bereit");

                if (wifiQr != null) {
                    say(context, "Fertig! Scanne den oberen QR-Code für dein Bild. "
                            + "Über den unteren Code kannst du dich mit meinem WLAN verbinden. "
                            + "Tippe auf Okay, wenn du fertig bist – oder auf Nochmal für ein neues Foto.");
                } else {
                    say(context, "Fertig! Scanne den QR-Code auf meinem Tablet, dann kannst du dein Selfie herunterladen. "
                            + "Tippe auf Okay, wenn du fertig bist – oder auf Nochmal für ein neues Foto.");
                }

                try {
                    dismiss.await(DISPLAY_MS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                board.setOnCloseListener(null);
                releaseServer();
                serverAcquired = false;

                if (retake.get()) {
                    board.hide();
                    SelfieRepository.get(context).delete(entity.id);
                    say(context, "Kein Problem, wir machen ein neues Foto!");
                    continue;
                }
                board.hide();
                break;
            }

            com.buhlergroup.pepper.stats.Stats.increment(context,
                    com.buhlergroup.pepper.stats.Stats.SELFIES);
            if (offerRaffle) {
                offerRaffleJoin(context, entity);
            }
            return entity;
        } catch (Exception e) {
            Log.e(TAG, "Selfie failed", e);
            DebugLog.get().e(TAG, "Selfie fehlgeschlagen", e);
            say(context, "Da ist etwas schiefgelaufen mit dem Selfie.");
            board.hide();
        } finally {
            if (serverAcquired) {
                releaseServer();
            }
            running = false;
            notifyState(false);
        }
        return null;
    }

    public int serverPort() {
        return shareServer.port();
    }

    public String tokenFor(String filename) {
        return shareServer.tokenFor(filename);
    }

    public String downloadUrl(Context context, String filename) {
        return shareServer.downloadUrl(context, filename);
    }

    private Bitmap captureConfirmed(QiContext context, SelfieView board, boolean externalCam) {
        Bitmap composed = null;
        for (int attempt = 1; attempt <= MAX_CAPTURES; attempt++) {
            countdown(context, externalCam);
            Bitmap photo = capture(context);
            if (photo == null) {
                announceCaptureFailure(context, externalCam);
                return null;
            }
            composed = addOverlay(context, photo);
            boolean canRetake = attempt < MAX_CAPTURES;
            PreviewDecision decision = askPreview(context, board, composed, canRetake);
            if (decision != PreviewDecision.RETAKE) {
                break;
            }
            if (canRetake) {
                say(context, "Kein Problem, wir machen es nochmal!");
            }
        }
        return composed;
    }

    private void countdown(QiContext context, boolean externalCam) {
        if (externalCam) {
            say(context, "Sag «Start», wenn du bereit bist.");
            waitForStart(context);
            say(context, "Drei… Zwei… Eins!");
        } else {
            say(context, "Drei… zwei… eins… lächeln!");
        }
    }

    private void announceCaptureFailure(QiContext context, boolean externalCam) {
        if (externalCam) {
            say(context, "Ich kann die externe Kamera nicht erreichen. Bitte deaktiviere sie im "
                    + "Admin-Dashboard, wenn du meine Kamera verwenden möchtest.");
        } else {
            say(context, "Hoppla, das Foto hat nicht geklappt. Versuchen wir es später nochmal.");
        }
    }

    private PreviewDecision askPreview(QiContext context, SelfieView board, Bitmap composed,
                                       boolean canRetake) {
        board.setStatus("Passt das Foto?");
        AtomicReference<PreviewDecision> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        board.showPreview(composed, canRetake,
                () -> {
                    ref.compareAndSet(null, PreviewDecision.SAVE);
                    latch.countDown();
                },
                () -> {
                    ref.compareAndSet(null, PreviewDecision.RETAKE);
                    latch.countDown();
                });
        if (canRetake) {
            say(context, "Sag «Passt» oder tippe auf Speichern, wenn es dir gefällt – "
                    + "oder sag «Nochmal» für ein neues Foto.");
        } else {
            say(context, "Das ist unser letzter Versuch, ich speichere dieses Foto.");
        }
        Future<ListenResult> voice = startPreviewVoiceListener(context, ref, latch, canRetake);
        try {
            if (!latch.await(PREVIEW_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                ref.compareAndSet(null, PreviewDecision.TIMEOUT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ref.compareAndSet(null, PreviewDecision.TIMEOUT);
        } finally {
            if (voice != null && !voice.isDone()) {
                voice.requestCancellation();
            }
        }
        PreviewDecision decision = ref.get();
        return decision == null ? PreviewDecision.TIMEOUT : decision;
    }

    private Future<ListenResult> startPreviewVoiceListener(QiContext context,
                                                           AtomicReference<PreviewDecision> ref, CountDownLatch latch, boolean canRetake) {
        try {
            List<String> texts = new ArrayList<>(Arrays.asList(
                    "passt", "speichern", "ja", "perfekt", "super", "behalten", "save", "yes"));
            if (canRetake) {
                texts.addAll(Arrays.asList("nochmal", "noch mal", "noch einmal", "neu",
                        "wiederholen", "again", "retake"));
            }
            PhraseSet phrases = PhraseSetBuilder.with(context)
                    .withTexts(texts.toArray(new String[0])).build();
            Listen listen = ListenBuilder.with(context).withPhraseSet(phrases).build();
            Future<ListenResult> future = listen.async().run();
            future.thenConsume(f -> {
                if (f.isCancelled() || f.hasError()) {
                    return;
                }
                ListenResult result = f.get();
                if (result == null || result.getHeardPhrase() == null) {
                    return;
                }
                String heard = result.getHeardPhrase().getText().toLowerCase(Locale.ROOT);
                if (heard.isEmpty()) {
                    return;
                }
                ref.compareAndSet(null,
                        isRetakePhrase(heard) ? PreviewDecision.RETAKE : PreviewDecision.SAVE);
                latch.countDown();
            });
            return future;
        } catch (Exception e) {
            Log.w(TAG, "preview voice listener failed: " + e.getMessage());
            return null;
        }
    }

    private boolean isRetakePhrase(String heard) {
        return heard.contains("noch") || heard.contains("neu")
                || heard.contains("wiederhol") || heard.contains("again")
                || heard.contains("retake");
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
            return decodeScaled(bytes);
        } catch (Exception e) {
            Log.e(TAG, "Capture failed", e);
            return null;
        }
    }

    private Bitmap decodeScaled(byte[] bytes) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_PHOTO_EDGE);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private int sampleSizeFor(int width, int height, int maxEdge) {
        int sample = 1;
        int longer = Math.max(width, height);
        while (longer / sample > maxEdge) {
            sample *= 2;
        }
        return sample;
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
        if (result != photo) {
            photo.recycle();
        }
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

    private enum PreviewDecision {
        SAVE,
        RETAKE,
        TIMEOUT
    }

    public interface StateListener {
        void onSelfieStateChanged(boolean active);
    }
}
