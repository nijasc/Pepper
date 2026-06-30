package com.buhlergroup.pepper.action.navigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;

import java.nio.ByteBuffer;

final class NavMapRenderer {

    private static final String TAG = "Navigation";

    private NavMapRenderer() {
    }

    @Nullable
    static Bitmap render(ExplorationMap map) {
        try {
            ByteBuffer buffer = map.getTopGraphicalRepresentation().getImage().getData();
            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Log.w(TAG, "renderMap failed: " + e.getMessage());
            return null;
        }
    }
}
