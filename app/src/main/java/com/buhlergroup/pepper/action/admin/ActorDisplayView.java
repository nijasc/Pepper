package com.buhlergroup.pepper.action.admin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Full-screen host that renders one {@link ActorState} on Pepper's tablet — the visual
 * half of the Actor deck. Everything is drawn in-app (text, emoji, fireworks, confetti,
 * face moods, selfie frame); nothing has to be imported on the shoot day.
 */
final class ActorDisplayView extends FrameLayout {

    private final TextView textView;
    private final ImageView imageView;
    private View effect;
    private ValueAnimator blink;

    ActorDisplayView(Context context) {
        super(context);
        setBackgroundColor(ActorState.BLACK);

        imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setVisibility(GONE);
        addView(imageView);

        textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(40, 40, 40, 40);
        textView.setVisibility(GONE);
        addView(textView);
    }

    /** Render a state. {@code null} = nothing but black (clean gesture-only loop). */
    void render(ActorState state) {
        stopBlink();
        removeEffect();
        if (state == null) {
            setBackgroundColor(ActorState.BLACK);
            textView.setVisibility(GONE);
            imageView.setVisibility(GONE);
            return;
        }
        setBackgroundColor(state.bg);
        imageView.setVisibility(GONE);
        switch (state.type) {
            case TEXT:
            case EMOJI:
                showText(state);
                break;
            case FIREWORKS:
                textView.setVisibility(GONE);
                addEffect(new ParticleView(getContext(), false));
                break;
            case CONFETTI:
                textView.setVisibility(GONE);
                addEffect(new ParticleView(getContext(), true));
                break;
            case FACE_NEUTRAL:
                textView.setVisibility(GONE);
                addEffect(new FaceView(getContext(), 0));
                break;
            case FACE_THINKING:
                textView.setVisibility(GONE);
                addEffect(new FaceView(getContext(), 1));
                break;
            case FACE_HAPPY:
                textView.setVisibility(GONE);
                addEffect(new FaceView(getContext(), 2));
                break;
            case SELFIE_FRAME:
                textView.setVisibility(GONE);
                addEffect(new FrameView(getContext(), state.text));
                break;
        }
    }

    /** Show an imported bitmap (the "Eigenes Bild" preset). */
    void showImage(Bitmap bitmap) {
        stopBlink();
        removeEffect();
        textView.setVisibility(GONE);
        setBackgroundColor(ActorState.BLACK);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(VISIBLE);
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
                : Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textView.setVisibility(VISIBLE);
        textView.bringToFront();
        if (s.blink) {
            startBlink();
        }
    }

    private void startBlink() {
        blink = ValueAnimator.ofFloat(1f, 0f);
        blink.setDuration(420);
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
        addView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        v.bringToFront();
    }

    private void removeEffect() {
        if (effect != null) {
            removeView(effect);
            effect = null;
        }
    }

    // ---------------------------------------------------------------- particles

    /** Looping fireworks (Drehbuch A payoff) or falling confetti (Drehbuch D feierabend). */
    private static final class ParticleView extends View {

        private final boolean confetti;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<Particle> particles = new ArrayList<>();
        private final Random rnd = new Random();
        private final int[] colors = {
                Color.parseColor("#FFD54F"), Color.parseColor("#FF8A65"),
                Color.parseColor("#4FC3F7"), Color.parseColor("#AED581"),
                Color.parseColor("#F06292"), Color.WHITE };
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

            spawn(now);
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                p.vy += (confetti ? 220f : 320f) * dt;
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
                    canvas.drawRect(p.x - p.size, p.y - p.size * 0.6f,
                            p.x + p.size, p.y + p.size * 0.6f, paint);
                    canvas.restore();
                } else {
                    canvas.drawCircle(p.x, p.y, p.size, paint);
                }
            }
            postInvalidateOnAnimation();
        }

        private void spawn(long now) {
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
                    p.size = rnd.nextInt(14) + 8;
                    p.angle = rnd.nextInt(360);
                    p.spin = rnd.nextInt(360) - 180;
                    p.life = 6f;
                    p.maxLife = 6f;
                    p.color = colors[rnd.nextInt(colors.length)];
                    particles.add(p);
                }
                return;
            }
            if (now < nextBurst) {
                return;
            }
            nextBurst = now + 600 + rnd.nextInt(500);
            float cx = w * (0.2f + rnd.nextFloat() * 0.6f);
            float cy = h * (0.15f + rnd.nextFloat() * 0.45f);
            int color = colors[rnd.nextInt(colors.length)];
            int count = 28 + rnd.nextInt(16);
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2 * i / count;
                float speed = 160 + rnd.nextInt(160);
                Particle p = new Particle();
                p.x = cx;
                p.y = cy;
                p.vx = (float) Math.cos(a) * speed;
                p.vy = (float) Math.sin(a) * speed;
                p.size = 6 + rnd.nextInt(4);
                p.life = 1.0f + rnd.nextFloat() * 0.5f;
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
            float eyeDx = w * 0.16f;
            float eyeR = Math.min(w, h) * 0.08f;
            int eyeColor = mood == 2 ? ActorState.SUN_DEEP : Color.WHITE;

            if (mood == 2) {
                // sun rays behind happy eyes
                paint.setColor(ActorState.SUN_DEEP);
                paint.setStrokeWidth(eyeR * 0.4f);
                for (int i = 0; i < 12; i++) {
                    double a = Math.PI * 2 * i / 12;
                    float rIn = Math.min(w, h) * 0.34f;
                    float rOut = Math.min(w, h) * 0.42f;
                    canvas.drawLine(cx + (float) Math.cos(a) * rIn, cy + (float) Math.sin(a) * rIn,
                            cx + (float) Math.cos(a) * rOut, cy + (float) Math.sin(a) * rOut, paint);
                }
            }

            paint.setColor(eyeColor);
            if (mood == 2) {
                // happy: two upward arcs ( ∪ ∪ ) = closed smiling eyes
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(eyeR * 0.7f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                drawSmileEye(canvas, cx - eyeDx, cy, eyeR);
                drawSmileEye(canvas, cx + eyeDx, cy, eyeR);
            } else {
                // neutral / thinking: round eyes (thinking looks up a touch)
                paint.setStyle(Paint.Style.FILL);
                float lift = mood == 1 ? -eyeR * 0.5f : 0f;
                canvas.drawCircle(cx - eyeDx, cy + lift, eyeR, paint);
                canvas.drawCircle(cx + eyeDx, cy + lift, eyeR, paint);
                if (mood == 1) {
                    // pondering dots under the eyes  • • •
                    float dotR = eyeR * 0.22f;
                    float dy = cy + eyeR * 1.6f;
                    for (int i = -1; i <= 1; i++) {
                        canvas.drawCircle(cx + i * eyeR * 1.1f, dy, dotR, paint);
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

    /** Selfie viewfinder overlay for Drehbuch C — corner brackets + camera glyph. */
    private static final class FrameView extends View {

        private final String glyph;
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

        FrameView(Context c, String glyph) {
            super(c);
            this.glyph = glyph == null ? "" : glyph;
            line.setColor(Color.WHITE);
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
            line.setStrokeWidth(Math.min(w, h) * 0.012f);
            float l = inset, t = inset, r = w - inset, b = h - inset;
            // top-left
            canvas.drawLine(l, t, l + len, t, line);
            canvas.drawLine(l, t, l, t + len, line);
            // top-right
            canvas.drawLine(r, t, r - len, t, line);
            canvas.drawLine(r, t, r, t + len, line);
            // bottom-left
            canvas.drawLine(l, b, l + len, b, line);
            canvas.drawLine(l, b, l, b - len, line);
            // bottom-right
            canvas.drawLine(r, b, r - len, b, line);
            canvas.drawLine(r, b, r, b - len, line);

            if (!glyph.isEmpty()) {
                text.setTextSize(Math.min(w, h) * 0.18f);
                float ty = h / 2f - (text.descent() + text.ascent()) / 2f;
                canvas.drawText(glyph, w / 2f, ty, text);
            }
        }
    }
}
