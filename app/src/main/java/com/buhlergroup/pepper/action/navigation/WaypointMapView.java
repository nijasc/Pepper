package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.action.navigation.data.WaypointEntity;

import java.util.ArrayList;
import java.util.List;

public class WaypointMapView extends View {

    private final List<WaypointEntity> waypoints = new ArrayList<>();
    private final Paint originPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint waypointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fotostandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint robotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double[] robotPose;

    public WaypointMapView(Context context) {
        super(context);
        init();
    }

    public WaypointMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaypointMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        originPaint.setColor(Color.WHITE);
        waypointPaint.setColor(0xFF1FB5AD);
        fotostandPaint.setColor(0xFFE5534B);
        robotPaint.setColor(0xFFFFB020);
        robotPaint.setStrokeWidth(6f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        gridPaint.setColor(0x33FFFFFF);
        gridPaint.setStrokeWidth(1f);
        gridTextPaint.setColor(0x99FFFFFF);
        gridTextPaint.setTextSize(20f);
    }

    public void setWaypoints(List<WaypointEntity> newWaypoints) {
        waypoints.clear();
        if (newWaypoints != null) {
            waypoints.addAll(newWaypoints);
        }
        postInvalidate();
    }

    public void setRobotPose(double[] pose) {
        this.robotPose = pose;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float pad = 60f;

        canvas.drawColor(0x22000000);

        double[] pose = robotPose;
        if (waypoints.isEmpty() && pose == null) {
            canvas.drawText("Noch keine Wegpunkte", pad, h / 2f, textPaint);
            return;
        }

        float minX = 0f;
        float maxX = 0f;
        float minY = 0f;
        float maxY = 0f;
        for (WaypointEntity wp : waypoints) {
            minX = Math.min(minX, (float) wp.x);
            maxX = Math.max(maxX, (float) wp.x);
            minY = Math.min(minY, (float) wp.y);
            maxY = Math.max(maxY, (float) wp.y);
        }
        if (pose != null) {
            minX = Math.min(minX, (float) pose[0]);
            maxX = Math.max(maxX, (float) pose[0]);
            minY = Math.min(minY, (float) pose[1]);
            maxY = Math.max(maxY, (float) pose[1]);
        }
        minX -= 1f;
        maxX += 1f;
        minY -= 1f;
        maxY += 1f;
        float rangeX = Math.max(1f, maxX - minX);
        float rangeY = Math.max(1f, maxY - minY);
        float scale = Math.min((w - 2 * pad) / rangeX, (h - 2 * pad) / rangeY);

        drawGrid(canvas, minX, maxX, minY, maxY, scale, pad, w, h);

        drawMarker(canvas, originPaint, toX(0f, minX, scale, pad),
                toY(0f, minY, scale, h, pad), "Start (0,0)");

        for (WaypointEntity wp : waypoints) {
            Paint paint = WaypointEntity.TYPE_FOTOSTAND.equals(wp.type) ? fotostandPaint : waypointPaint;
            float px = toX((float) wp.x, minX, scale, pad);
            float py = toY((float) wp.y, minY, scale, h, pad);
            drawMarker(canvas, paint, px, py,
                    wp.name + " (" + fmt(wp.x) + ", " + fmt(wp.y) + ")");
        }

        if (pose != null) {
            float px = toX((float) pose[0], minX, scale, pad);
            float py = toY((float) pose[1], minY, scale, h, pad);
            canvas.drawCircle(px, py, 16f, robotPaint);
            float headingLen = 34f;
            float hx = px + (float) Math.cos(pose[2]) * headingLen;
            float hy = py - (float) Math.sin(pose[2]) * headingLen;
            canvas.drawLine(px, py, hx, hy, robotPaint);
            canvas.drawText("Pepper (" + fmt(pose[0]) + ", " + fmt(pose[1]) + ")",
                    px + 20f, py - 16f, textPaint);
        }
    }

    private void drawGrid(Canvas canvas, float minX, float maxX, float minY, float maxY,
                          float scale, float pad, int w, int h) {
        for (int gx = (int) Math.ceil(minX); gx <= (int) Math.floor(maxX); gx++) {
            float px = toX(gx, minX, scale, pad);
            canvas.drawLine(px, pad, px, h - pad, gridPaint);
            canvas.drawText(gx + "m", px + 2f, h - pad + 22f, gridTextPaint);
        }
        for (int gy = (int) Math.ceil(minY); gy <= (int) Math.floor(maxY); gy++) {
            float py = toY(gy, minY, scale, h, pad);
            canvas.drawLine(pad, py, w - pad, py, gridPaint);
            canvas.drawText(gy + "m", 8f, py - 4f, gridTextPaint);
        }
    }

    private String fmt(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private float toX(float worldX, float minX, float scale, float pad) {
        return pad + (worldX - minX) * scale;
    }

    private float toY(float worldY, float minY, float scale, int h, float pad) {
        return h - pad - (worldY - minY) * scale;
    }

    private void drawMarker(Canvas canvas, Paint paint, float x, float y, String label) {
        canvas.drawCircle(x, y, 12f, paint);
        canvas.drawText(label, x + 16f, y + 10f, textPaint);
    }
}
