package com.buhlergroup.pepper.action.admin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
 * Full-screen host that renders one {@link ActorState} on a branded Bühler backdrop. The
 * visual half of the Actor deck — designed to look good on camera: teal gradient with a
 * warm sun glow, soft vignette, real Bühler logo, glowing typography and lively particle
 * effects. Everything is drawn in-app; nothing has to be imported on the shoot day.
 */
final class ActorDisplayView extends FrameLayout {

    private final BrandBackgroundView background;
    private final ImageView imageView;   // imported photo or big brand logo
    private final ImageView wordmark;     // small Bühler logo, bottom centre
    private final TextView textView;
    private View effect;
    private ValueAnimator blink;

    ActorDisplayView(Context context) {
        super(context);

        background = new BrandBackgroundView(context);
        addView(background, full());

        imageView = new ImageView(context);
        imageView.setVisibility(GONE);
        addView(imageView, full());

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(48, 48, 48, 48);
        textView.setVisibility(GONE);
        addView(textView, full());

        wordmark = new ImageView(context);
        wordmark.setImageResource(R.drawable.buhler_logo);
        wordmark.setAlpha(0.9f);
        wordmark.setVisibility(GONE);
        int h = Math.max(48, (int) (getResources().getDisplayMetrics().heightPixels * 0.055f));
        LayoutParams wp = new LayoutParams(LayoutParams.WRAP_CONTENT, h);
        wp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        wp.bottomMargin = (int) (h * 0.7f);
        wordmark.setLayoutParams(wp);
        wordmark.setAdjustViewBounds(true);
        addView(wordmark);

        render(ActorState.idle());
    }

    void render(ActorState state) {
        stopBlink();
        removeEffect();
        textView.setVisibility(GONE);
        imageView.setVisibility(GONE);
        wordmark.setVisibility(GONE);

        if (state == null) {
            background.set(ActorState.TEAL_DARK, ActorState.INK, ActorState.TEAL);
            return;
        }
        background.set(state.gradTop, state.gradBottom, state.accent);
        if (state.wordmark) {
            wordmark.setVisibility(VISIBLE);
            wordmark.bringToFront();
        }
        switch (state.type) {
            case TEXT:
            case EMOJI:
                showText(state);
                break;
            case FIREWORKS:
                addEffect(new ParticleView(getContext(), false, state.accent));
                break;
            case CONFETTI:
                addEffect(new ParticleView(getContext(), true, state.accent));
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
                addEffect(new FrameView(getContext(), state.text, state.accent));
                break;
            case BRAND:
                showBrand(state);
                break;
        }
    }

    /** Show an imported bitmap (the "Eigenes Bild" preset). */
    void showImage(Bitmap bitmap) {
        stopBlink();
        removeEffect();
        textView.setVisibility(GONE);
        wordmark.setVisibility(GONE);
        background.set(ActorState.INK, Color.BLACK, ActorState.TEAL);
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
        textView.setShadowLayer(s.textSizeSp * 0.25f, 0f, 0f, withAlpha(s.accent, 0xCC));
        textView.setVisibility(VISIBLE);
        textView.bringToFront();
        if (s.blink) {
            startBlink();
        }
    }

    private void showBrand(ActorState s) {
        if (s.text == null) {
            return; // idle backdrop only
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
        // restore default text layout (BRAND/text states tweak it)
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(48, 48, 48, 48);
    }

    private void addEffect(View v) {
        effect = v;
        addView(v, full());
        v.bringToFront();
        if (wordmark.getVisibility() == VISIBLE) {
            wordmark.bringToFront();
        }
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

    // ----------------------------------------------------------- branded backdrop

    /** Teal gradient + warm sun glow (when accent is sun) + soft cinematic vignette. */
    private static final class BrandBackgroundView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int top = ActorState.TEAL_DARK;
        private int bottom = ActorState.INK;
        private int accent = ActorState.TEAL;

        BrandBackgroundView(Context c) {
            super(c);
        }

        void set(int top, int bottom, int accent) {
            this.top = top;
            this.bottom = bottom;
            this.accent = accent;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) {
                return;
            }
            paint.setShader(new LinearGradient(0, 0, 0, h, top, bottom, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);

            if (accent == ActorState.SUN) {
                paint.setShader(new RadialGradient(w * 0.5f, h * 0.32f, Math.min(w, h) * 0.7f,
                        new int[]{withAlpha(ActorState.SUN, 0x55), withAlpha(ActorState.SUN, 0x00)},
                        null, Shader.TileMode.CLAMP));
                canvas.drawRect(0, 0, w, h, paint);
            }

            // soft vignette for depth on camera
            paint.setShader(new RadialGradient(w * 0.5f, h * 0.5f, Math.max(w, h) * 0.75f,
                    new int[]{0x00000000, 0x66000000}, new float[]{0.6f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
        }
    }

    // ---------------------------------------------------------------- particles

    /** Looping fireworks (Drehbuch A) or falling confetti (Drehbuch D), tinted to brand. */
    private static final class ParticleView extends View {

        private final boolean confetti;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<Particle> particles = new ArrayList<>();
        private final Random rnd = new Random();
        private final int[] colors;
        private long last;
        private long nextBurst;

        ParticleView(Context c, boolean confetti, int accent) {
            super(c);
            this.confetti = confetti;
            this.colors = new int[]{
                    ActorState.TEAL, 0xFF4FD8CE, ActorState.SUN,
                    0xFFFFE3A3, Color.WHITE, 0xFF7CE0D8};
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

    /** Display "mimik" for Drehbuch B: 0 = neutral, 1 = thinking, 2 = happy. */
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
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(eyeR * 0.45f);
                for (int i = 0; i < 12; i++) {
                    double a = Math.PI * 2 * i / 12;
                    float rIn = Math.min(w, h) * 0.33f;
                    float rOut = Math.min(w, h) * 0.41f;
                    canvas.drawLine(cx + (float) Math.cos(a) * rIn, cy + (float) Math.sin(a) * rIn,
                            cx + (float) Math.cos(a) * rOut, cy + (float) Math.sin(a) * rOut, paint);
                }
            }

            paint.setColor(Color.WHITE);
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

    /** Selfie viewfinder overlay for Drehbuch C — teal corner brackets + camera glyph. */
    private static final class FrameView extends View {

        private final String glyph;
        private final int accent;
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

        FrameView(Context c, String glyph, int accent) {
            super(c);
            this.glyph = glyph == null ? "" : glyph;
            this.accent = accent;
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeCap(Paint.Cap.ROUND);
            text.setColor(Color.WHITE);
            text.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            float inset = Math.min(w, h) * 0.10f;
            float len = Math.min(w, h) * 0.14f;
            line.setColor(accent);
            line.setStrokeWidth(Math.min(w, h) * 0.014f);
            float l = inset, t = inset, r = w - inset, b = h - inset;
            canvas.drawLine(l, t, l + len, t, line);
            canvas.drawLine(l, t, l, t + len, line);
            canvas.drawLine(r, t, r - len, t, line);
            canvas.drawLine(r, t, r, t + len, line);
            canvas.drawLine(l, b, l + len, b, line);
            canvas.drawLine(l, b, l, b - len, line);
            canvas.drawLine(r, b, r - len, b, line);
            canvas.drawLine(r, b, r, b - len, line);

            if (!glyph.isEmpty()) {
                text.setTextSize(Math.min(w, h) * 0.2f);
                float ty = h / 2f - (text.descent() + text.ascent()) / 2f;
                canvas.drawText(glyph, w / 2f, ty, text);
            }
        }
    }
}
