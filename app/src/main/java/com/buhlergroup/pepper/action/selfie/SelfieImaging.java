package com.buhlergroup.pepper.action.selfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.buhlergroup.pepper.R;

import java.io.ByteArrayOutputStream;

/**
 * Stateless image helpers for the selfie flow: bitmap decode+scale, overlay
 * compositing and JPEG encoding. Extracted from {@link SelfieController} so the
 * controller keeps only stateful capture/state-machine logic.
 */
final class SelfieImaging {

    private static final int MAX_PHOTO_EDGE = 1920;

    private SelfieImaging() {
    }

    static Bitmap decodeScaled(byte[] bytes) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_PHOTO_EDGE);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    static int sampleSizeFor(int width, int height, int maxEdge) {
        int sample = 1;
        int longer = Math.max(width, height);
        while (longer / sample > maxEdge) {
            sample *= 2;
        }
        return sample;
    }

    static Bitmap addOverlay(Context context, Bitmap photo) {
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

    static byte[] toJpeg(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        return bos.toByteArray();
    }
}
