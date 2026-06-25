package com.buhlergroup.pepper.action.admin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.buhlergroup.pepper.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Full-screen host that renders one {@link ActorState} on a pure black screen. On top sits
 * one consistently-styled, in-app drawing — flat shapes, rounded caps, one shared palette
 * — or white glowing typography. Black background for every Actor action except the "own
 * image" preset. Designed to look clean and cohesive on camera.
 */
final class ActorDisplayView extends FrameLayout {

    private final ImageView imageView;   // imported photo or big brand logo
    private final TextView textView;
    private View effect;
    private ValueAnimator blink;

    ActorDisplayView(Context context) {
        super(context);
        setBackgroundColor(ActorState.BLACK);

        imageView = new ImageView(context);
        imageView.setVisibility(GONE);
        addView(imageView, full());

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(48, 48, 48, 48);
        textView.setVisibility(GONE);
        addView(textView, full());

        render(ActorState.idle());
    }

    void render(ActorState state) {
        stopBlink();
        removeEffect();
        textView.setVisibility(GONE);
        imageView.setVisibility(GONE);
        setBackgroundColor(ActorState.BLACK);

        if (state == null) {
            return;
        }
        switch (state.type) {
            case TEXT:
                showText(state);
                break;
            case ICON:
                addEffect(new IconView(getContext(), state.icon, state.accent));
                break;
            case FIREWORKS:
                addEffect(new ParticleView(getContext(), false));
                break;
            case CONFETTI:
                addEffect(new ParticleView(getContext(), true));
                break;
            case FACE_NEUTRAL:
                addEffect(new FaceView(getContext(), 0));
                break;
            case FACE_THINKING:
                addEffect(new FaceView(getContext(), 1));
                break;
            case FACE_HAPPY:
                addEffect(new FaceView(getContext(), 2));
                break;
            case SELFIE_FRAME:
                addEffect(new FrameView(getContext(), state.accent));
                break;
            case BRAND:
                showBrand(state);
                break;
        }
    }

    /** Show an imported bitmap (the "Eigenes Bild" preset) — the only non-black state. */
    void showImage(Bitmap bitmap) {
        stopBlink();
        removeEffect();
        textView.setVisibility(GONE);
        setBackgroundColor(ActorState.BLACK);
        imageView.setLayoutParams(full());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(VISIBLE);
        imageView.bringToFront();
    }

    void release() {
        stopBlink();
        removeEffect();
    }

    private void showText(ActorState s) {
        textView.setText(s.text);
        textView.setTextColor(s.fg);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, s.textSizeSp);
        textView.setTypeface(s.monospace
                ? Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                : Typeface.create("sans-serif-black", Typeface.BOLD));
        textView.setShadowLayer(s.textSizeSp * 0.28f, 0f, 0f, withAlpha(s.accent, 0xDD));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(48, 48, 48, 48);
        textView.setVisibility(VISIBLE);
        textView.bringToFront();
        if (s.blink) {
            startBlink();
        }
    }

    private void showBrand(ActorState s) {
        if (s.text == null) {
            return; // plain black
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(R.drawable.buhler_logo);
        LayoutParams ip = new LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.5f),
                LayoutParams.WRAP_CONTENT);
        ip.gravity = Gravity.CENTER;
        ip.bottomMargin = (int) (getResources().getDisplayMetrics().heightPixels * 0.16f);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(ip);
        imageView.setVisibility(VISIBLE);
        imageView.bringToFront();

        textView.setText(s.text);
        textView.setTextColor(s.fg);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, s.textSizeSp);
        textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textView.setShadowLayer(s.textSizeSp * 0.2f, 0f, 0f, withAlpha(s.accent, 0xAA));
        textView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        textView.setPadding(48, 48, 48, (int) (getResources().getDisplayMetrics().heightPixels * 0.12f));
        textView.setVisibility(VISIBLE);
        textView.bringToFront();
    }

    private void startBlink() {
        blink = ValueAnimator.ofFloat(1f, 0.15f);
        blink.setDuration(440);
        blink.setRepeatMode(ValueAnimator.REVERSE);
        blink.setRepeatCount(ValueAnimator.INFINITE);
        blink.addUpdateListener(a -> textView.setAlpha((float) a.getAnimatedValue()));
        blink.start();
    }

    private void stopBlink() {
        if (blink != null) {
            blink.cancel();
            blink = null;
        }
        textView.setAlpha(1f);
    }

    private void addEffect(View v) {
        effect = v;
        addView(v, full());
        v.bringToFront();
    }

    private void removeEffect() {
        if (effect != null) {
            removeView(effect);
            effect = null;
        }
    }

    private LayoutParams full() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    // ----------------------------------------------------- consistent illustrations

    /** Flat, single-style drawings: sun, heart, party, palm. White · teal · sun · coral. */
    private static final class IconView extends View {

        private final ActorState.Icon icon;
        private final int accent;
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        IconView(Context c, ActorState.Icon icon, int accent) {
            super(c);
            this.icon = icon;
            this.accent = accent;
            fill.setStyle(Paint.Style.FILL);
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float u = Math.min(w, h);   // base unit
            switch (icon) {
                case SUN:
                    drawSun(canvas, cx, cy, u);
                    break;
                case HEART:
                    drawHeart(canvas, cx, cy, u);
                    break;
                case PARTY:
                    drawParty(canvas, w, h, u);
                    break;
                case PALM:
                    drawPalm(canvas, cx, cy, u);
                    break;
            }
        }

        private void drawSun(Canvas canvas, float cx, float cy, float u) {
            float r = u * 0.16f;
            stroke.setColor(ActorState.SUN);
            stroke.setStrokeWidth(u * 0.045f);
            for (int i = 0; i < 12; i++) {
                double a = Math.PI * 2 * i / 12;
                float rIn = r * 1.6f;
                float rOut = r * 2.4f;
                canvas.drawLine(cx + (float) Math.cos(a) * rIn, cy + (float) Math.sin(a) * rIn,
                        cx + (float) Math.cos(a) * rOut, cy + (float) Math.sin(a) * rOut, stroke);
            }
            fill.setColor(ActorState.SUN);
            canvas.drawCircle(cx, cy, r, fill);
            fill.setColor(withAlpha(ActorState.WHITE, 0x33));
            canvas.drawCircle(cx - r * 0.25f, cy - r * 0.25f, r * 0.45f, fill);
        }

        private void drawHeart(Canvas canvas, float cx, float cy, float u) {
            float s = u * 0.34f;
            path.reset();
            path.moveTo(cx, cy + s * 0.32f);
            path.cubicTo(cx - s * 0.62f, cy - s * 0.10f, cx - s * 0.30f, cy - s * 0.60f, cx, cy - s * 0.22f);
            path.cubicTo(cx + s * 0.30f, cy - s * 0.60f, cx + s * 0.62f, cy - s * 0.10f, cx, cy + s * 0.32f);
            path.close();
            fill.setColor(ActorState.CORAL);
            canvas.drawPath(path, fill);
            fill.setColor(withAlpha(ActorState.WHITE, 0x40));
            canvas.drawCircle(cx - s * 0.22f, cy - s * 0.18f, s * 0.12f, fill);
        }

        private void drawParty(Canvas canvas, float w, float h, float u) {
            int[] palette = {ActorState.TEAL, ActorState.SUN, ActorState.CORAL, ActorState.WHITE};
            Random r = new Random(42);
            float size = u * 0.05f;
            for (int i = 0; i < 22; i++) {
                float x = w * (0.12f + r.nextFloat() * 0.76f);
                float y = h * (0.12f + r.nextFloat() * 0.76f);
                fill.setColor(palette[i % palette.length]);
                canvas.save();
                canvas.rotate(r.nextInt(360), x, y);
                if (i % 3 == 0) {
                    canvas.drawCircle(x, y, size * 0.6f, fill);
                } else {
                    canvas.drawRect(x - size, y - size * 0.5f, x + size, y + size * 0.5f, fill);
                }
                canvas.restore();
            }
        }

        private void drawPalm(Canvas canvas, float cx, float cy, float u) {
            // sun
            fill.setColor(ActorState.SUN);
            canvas.drawCircle(cx + u * 0.28f, cy - u * 0.28f, u * 0.09f, fill);
            // trunk
            stroke.setColor(ActorState.TEAL);
            stroke.setStrokeWidth(u * 0.04f);
            float baseX = cx - u * 0.05f;
            float baseY = cy + u * 0.36f;
            float topX = cx - u * 0.12f;
            float topY = cy - u * 0.18f;
            path.reset();
            path.moveTo(baseX, baseY);
            path.quadTo(cx - u * 0.20f, cy + u * 0.05f, topX, topY);
            canvas.drawPath(path, stroke);
            // fronds
            stroke.setStrokeWidth(u * 0.035f);
            float[][] tips = {
                    {topX - u * 0.30f, topY - u * 0.02f},
                    {topX - u * 0.16f, topY - u * 0.26f},
                    {topX + u * 0.06f, topY - u * 0.30f},
                    {topX + u * 0.26f, topY - u * 0.16f},
                    {topX + u * 0.30f, topY + u * 0.04f}};
            for (float[] t : tips) {
                path.reset();
                path.moveTo(topX, topY);
                path.quadTo((topX + t[0]) / 2f, topY - u * 0.10f, t[0], t[1]);
                canvas.drawPath(path, stroke);
            }
            // sea / ground waves
            stroke.setStrokeWidth(u * 0.03f);
            for (int row = 0; row < 2; row++) {
                float y = cy + u * 0.40f + row * u * 0.08f;
                path.reset();
                path.moveTo(cx - u * 0.42f, y);
                for (int k = 0; k < 4; k++) {
                    float x0 = cx - u * 0.42f + k * u * 0.21f;
                    path.quadTo(x0 + u * 0.05f, y - u * 0.04f, x0 + u * 0.105f, y);
                    path.quadTo(x0 + u * 0.16f, y + u * 0.04f, x0 + u * 0.21f, y);
                }
                canvas.drawPath(path, stroke);
            }
        }
    }

    // ---------------------------------------------------------------- particles

    /** Looping fireworks (Drehbuch A) or falling confetti (Drehbuch D) on black. */
    private static final class ParticleView extends View {

        private final boolean confetti;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<Particle> particles = new ArrayList<>();
        private final Random rnd = new Random();
        private final int[] colors = {
                ActorState.TEAL, 0xFF4FD8CE, ActorState.SUN,
                ActorState.CORAL, Color.WHITE, 0xFF7CE0D8};
        private long last;
        private long nextBurst;

        ParticleView(Context c, boolean confetti) {
            super(c);
            this.confetti = confetti;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            long now = SystemClock.uptimeMillis();
            float dt = last == 0 ? 0.016f : Math.min((now - last) / 1000f, 0.05f);
            last = now;

            spawn();
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                p.vy += (confetti ? 220f : 300f) * dt;
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                p.life -= dt;
                p.angle += p.spin * dt;
                if (p.life <= 0 || p.y > getHeight() + 40) {
                    particles.remove(i);
                    continue;
                }
                int alpha = (int) (255 * Math.max(0f, Math.min(1f, p.life / p.maxLife)));
                paint.setColor(p.color);
                paint.setAlpha(confetti ? 255 : alpha);
                if (confetti) {
                    canvas.save();
                    canvas.rotate(p.angle, p.x, p.y);
                    canvas.drawRect(p.x - p.size, p.y - p.size * 0.55f,
                            p.x + p.size, p.y + p.size * 0.55f, paint);
                    canvas.restore();
                } else {
                    canvas.drawCircle(p.x, p.y, p.size, paint);
                }
            }
            postInvalidateOnAnimation();
        }

        private void spawn() {
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) {
                return;
            }
            if (confetti) {
                for (int i = 0; i < 3; i++) {
                    Particle p = new Particle();
                    p.x = rnd.nextInt(w);
                    p.y = -20;
                    p.vx = rnd.nextInt(120) - 60;
                    p.vy = rnd.nextInt(80) + 40;
                    p.size = rnd.nextInt(14) + 9;
                    p.angle = rnd.nextInt(360);
                    p.spin = rnd.nextInt(360) - 180;
                    p.life = 6f;
                    p.maxLife = 6f;
                    p.color = colors[rnd.nextInt(colors.length)];
                    particles.add(p);
                }
                return;
            }
            long now = SystemClock.uptimeMillis();
            if (now < nextBurst) {
                return;
            }
            nextBurst = now + 520 + rnd.nextInt(420);
            float cx = w * (0.2f + rnd.nextFloat() * 0.6f);
            float cy = h * (0.14f + rnd.nextFloat() * 0.42f);
            int color = colors[rnd.nextInt(colors.length)];
            int count = 30 + rnd.nextInt(18);
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2 * i / count;
                float speed = 170 + rnd.nextInt(170);
                Particle p = new Particle();
                p.x = cx;
                p.y = cy;
                p.vx = (float) Math.cos(a) * speed;
                p.vy = (float) Math.sin(a) * speed;
                p.size = 7 + rnd.nextInt(5);
                p.life = 1.0f + rnd.nextFloat() * 0.6f;
                p.maxLife = p.life;
                p.color = color;
                particles.add(p);
            }
        }

        private static final class Particle {
            float x, y, vx, vy, size, life, maxLife, angle, spin;
            int color;
        }
    }

    // --------------------------------------------------------------------- face

    /** Display "mimik" for Drehbuch B on black: 0 = neutral, 1 = thinking, 2 = happy. */
    private static final class FaceView extends View {

        private final int mood;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        FaceView(Context c, int mood) {
            super(c);
            this.mood = mood;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float eyeDx = w * 0.17f;
            float eyeR = Math.min(w, h) * 0.085f;

            if (mood == 2) {
                paint.setColor(ActorState.SUN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(eyeR * 0.45f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                for (int i = 0; i < 12; i++) {
                    double a = Math.PI * 2 * i / 12;
                    float rIn = Math.min(w, h) * 0.33f;
                    float rOut = Math.min(w, h) * 0.41f;
                    canvas.drawLine(cx + (float) Math.cos(a) * rIn, cy + (float) Math.sin(a) * rIn,
                            cx + (float) Math.cos(a) * rOut, cy + (float) Math.sin(a) * rOut, paint);
                }
            }

            paint.setColor(mood == 2 ? ActorState.SUN : Color.WHITE);
            if (mood == 2) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(eyeR * 0.75f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                drawSmileEye(canvas, cx - eyeDx, cy, eyeR);
                drawSmileEye(canvas, cx + eyeDx, cy, eyeR);
            } else {
                paint.setStyle(Paint.Style.FILL);
                float lift = mood == 1 ? -eyeR * 0.5f : 0f;
                canvas.drawCircle(cx - eyeDx, cy + lift, eyeR, paint);
                canvas.drawCircle(cx + eyeDx, cy + lift, eyeR, paint);
                if (mood == 1) {
                    paint.setColor(ActorState.TEAL);
                    float dotR = eyeR * 0.24f;
                    float dy = cy + eyeR * 1.7f;
                    for (int i = -1; i <= 1; i++) {
                        canvas.drawCircle(cx + i * eyeR * 1.2f, dy, dotR, paint);
                    }
                }
            }
        }

        private void drawSmileEye(Canvas canvas, float ex, float ey, float r) {
            RectF arc = new RectF(ex - r, ey - r, ex + r, ey + r);
            canvas.drawArc(arc, 200, 140, false, paint);
        }
    }

    // -------------------------------------------------------------- selfie frame

    /** Selfie viewfinder for Drehbuch C — teal corner brackets + a drawn camera. */
    private static final class FrameView extends View {

        private final int accent;
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);

        FrameView(Context c, int accent) {
            super(c);
            this.accent = accent;
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeCap(Paint.Cap.ROUND);
            line.setStrokeJoin(Paint.Join.ROUND);
            fill.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            float u = Math.min(w, h);
            float inset = u * 0.10f;
            float len = u * 0.14f;
            line.setColor(accent);
            line.setStrokeWidth(u * 0.014f);
            float l = inset, t = inset, r = w - inset, b = h - inset;
            canvas.drawLine(l, t, l + len, t, line);
            canvas.drawLine(l, t, l, t + len, line);
            canvas.drawLine(r, t, r - len, t, line);
            canvas.drawLine(r, t, r, t + len, line);
            canvas.drawLine(l, b, l + len, b, line);
            canvas.drawLine(l, b, l, b - len, line);
            canvas.drawLine(r, b, r - len, b, line);
            canvas.drawLine(r, b, r, b - len, line);

            // camera body
            float cx = w / 2f, cy = h / 2f;
            float bw = u * 0.34f, bh = u * 0.24f;
            RectF body = new RectF(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f);
            line.setStrokeWidth(u * 0.018f);
            line.setColor(Color.WHITE);
            canvas.drawRoundRect(body, u * 0.03f, u * 0.03f, line);
            // viewfinder bump
            RectF bump = new RectF(cx - bw * 0.18f, cy - bh / 2f - u * 0.05f, cx + bw * 0.05f, cy - bh / 2f);
            canvas.drawRoundRect(bump, u * 0.012f, u * 0.012f, line);
            // lens
            fill.setColor(accent);
            canvas.drawCircle(cx, cy + bh * 0.03f, u * 0.075f, fill);
            fill.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy + bh * 0.03f, u * 0.032f, fill);
            // flash
            fill.setColor(ActorState.SUN);
            canvas.drawCircle(cx + bw * 0.32f, cy - bh * 0.22f, u * 0.018f, fill);
        }
    }
}
