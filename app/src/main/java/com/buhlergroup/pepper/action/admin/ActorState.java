package com.buhlergroup.pepper.action.admin;

import android.graphics.Color;

/**
 * One thing Pepper shows on its tablet during an Actor take. Either a rendered graphic
 * (big number, clock, text banner, emoji), an animated effect (fireworks, confetti), a
 * face/eyes mood, a selfie frame — or {@code null}, which means "nothing, just black"
 * (used for clean gesture-only loops).
 *
 * <p>All states are drawn in-app (no imported assets needed) so the four Sommerferien
 * Drehbücher can be filmed without preparing graphics on the shoot day. See
 * {@link ActorPresets} for the ready-made deck.</p>
 */
final class ActorState {

    enum Type { TEXT, EMOJI, FIREWORKS, CONFETTI, FACE_NEUTRAL, FACE_THINKING, FACE_HAPPY, SELFIE_FRAME }

    // Bühler-ish warm palette for the summer states.
    static final int BLACK = Color.BLACK;
    static final int SUN = Color.parseColor("#FFC107");
    static final int SUN_DEEP = Color.parseColor("#FF8F00");
    static final int NIGHT = Color.parseColor("#0D1B2A");

    final Type type;
    final String text;
    final int fg;
    final int bg;
    final float textSizeSp;
    final boolean monospace;
    final boolean blink;

    private ActorState(Type type, String text, int fg, int bg,
                       float textSizeSp, boolean monospace, boolean blink) {
        this.type = type;
        this.text = text;
        this.fg = fg;
        this.bg = bg;
        this.textSizeSp = textSizeSp;
        this.monospace = monospace;
        this.blink = blink;
    }

    /** Huge centred countdown digit (Drehbuch A & D). */
    static ActorState number(String digit) {
        return new ActorState(Type.TEXT, digit, Color.WHITE, BLACK, 320f, true, false);
    }

    /** Large clock time, e.g. "16:00" (Drehbuch D jump-cuts). */
    static ActorState clock(String time) {
        return new ActorState(Type.TEXT, time, Color.WHITE, NIGHT, 160f, true, false);
    }

    /** Big text banner — FERIENMODUS, FEIERABEND, GLEICH … */
    static ActorState banner(String t, int fg, int bg) {
        return new ActorState(Type.TEXT, t, fg, bg, 84f, false, false);
    }

    /** Like {@link #banner} but blinking — "GLEICH" (Drehbuch A hook). */
    static ActorState blinkBanner(String t, int fg, int bg) {
        return new ActorState(Type.TEXT, t, fg, bg, 84f, false, true);
    }

    /** Full-screen emoji/symbol: 🌞 ❤️ 🎉 */
    static ActorState emoji(String e) {
        return new ActorState(Type.EMOJI, e, Color.WHITE, BLACK, 240f, false, false);
    }

    static ActorState fireworks() {
        return new ActorState(Type.FIREWORKS, null, Color.WHITE, NIGHT, 0f, false, false);
    }

    static ActorState confetti() {
        return new ActorState(Type.CONFETTI, null, Color.WHITE, SUN, 0f, false, false);
    }

    static ActorState faceNeutral() {
        return new ActorState(Type.FACE_NEUTRAL, null, Color.WHITE, NIGHT, 0f, false, false);
    }

    static ActorState faceThinking() {
        return new ActorState(Type.FACE_THINKING, "?", Color.WHITE, NIGHT, 0f, false, false);
    }

    static ActorState faceHappy() {
        return new ActorState(Type.FACE_HAPPY, null, SUN_DEEP, SUN, 0f, false, false);
    }

    static ActorState selfieFrame() {
        return new ActorState(Type.SELFIE_FRAME, "📸", Color.WHITE, BLACK, 0f, false, false);
    }
}
