package com.buhlergroup.pepper.action.admin;

/**
 * One thing Pepper shows on its tablet during an Actor take. The background is always
 * black (except the "own image" preset); on top sits one consistently-styled, hand-drawn
 * vector illustration — flat shapes, rounded caps, one palette (white · Bühler teal ·
 * warm sun · coral). Everything is drawn in-app so the four Sommerferien Drehbücher share
 * one clean look with nothing to import on the shoot day. See {@link ActorPresets}.
 */
final class ActorState {

    enum Type { TEXT, ICON, FIREWORKS, CONFETTI, FACE_NEUTRAL, FACE_THINKING, FACE_HAPPY, SELFIE_FRAME, BRAND }

    /** Hand-drawn illustrations, all in the same flat style. */
    enum Icon { SUN, HEART, PARTY, PALM }

    // Shared palette — the only colours any drawing uses.
    static final int BLACK = 0xFF000000;
    static final int WHITE = 0xFFFFFFFF;
    static final int TEAL = 0xFF009E93;
    static final int SUN = 0xFFFFC857;
    static final int CORAL = 0xFFFF6B7A;

    final Type type;
    final String text;
    final Icon icon;
    final int fg;
    final int accent;          // glow / illustration tint
    final float textSizeSp;
    final boolean monospace;
    final boolean blink;

    private ActorState(Type type, String text, Icon icon, int fg, int accent,
                       float textSizeSp, boolean monospace, boolean blink) {
        this.type = type;
        this.text = text;
        this.icon = icon;
        this.fg = fg;
        this.accent = accent;
        this.textSizeSp = textSizeSp;
        this.monospace = monospace;
        this.blink = blink;
    }

    /** Plain black screen (clean gesture/pose loops). */
    static ActorState idle() {
        return new ActorState(Type.BRAND, null, null, WHITE, TEAL, 0f, false, false);
    }

    /** Huge countdown digit, white with a teal glow (Drehbuch A & D). */
    static ActorState number(String digit) {
        return new ActorState(Type.TEXT, digit, null, WHITE, TEAL, 300f, true, false);
    }

    /** Large clock time, e.g. "16:00" (Drehbuch D). */
    static ActorState clock(String time) {
        return new ActorState(Type.TEXT, time, null, WHITE, TEAL, 170f, true, false);
    }

    /** Big text banner with a warm glow — FERIENMODUS, FEIERABEND, Letzter Tag … */
    static ActorState banner(String t) {
        return new ActorState(Type.TEXT, t, null, WHITE, SUN, 92f, false, false);
    }

    /** Blinking banner — "GLEICH" (Drehbuch A hook). */
    static ActorState blinkBanner(String t) {
        return new ActorState(Type.TEXT, t, null, SUN, SUN, 96f, false, true);
    }

    static ActorState sun() {
        return new ActorState(Type.ICON, null, Icon.SUN, WHITE, SUN, 0f, false, false);
    }

    static ActorState heart() {
        return new ActorState(Type.ICON, null, Icon.HEART, WHITE, CORAL, 0f, false, false);
    }

    static ActorState party() {
        return new ActorState(Type.ICON, null, Icon.PARTY, WHITE, SUN, 0f, false, false);
    }

    static ActorState palm() {
        return new ActorState(Type.ICON, null, Icon.PALM, WHITE, TEAL, 0f, false, false);
    }

    static ActorState fireworks() {
        return new ActorState(Type.FIREWORKS, null, null, WHITE, TEAL, 0f, false, false);
    }

    static ActorState confetti() {
        return new ActorState(Type.CONFETTI, null, null, WHITE, SUN, 0f, false, false);
    }

    static ActorState faceNeutral() {
        return new ActorState(Type.FACE_NEUTRAL, null, null, WHITE, TEAL, 0f, false, false);
    }

    static ActorState faceThinking() {
        return new ActorState(Type.FACE_THINKING, null, null, WHITE, TEAL, 0f, false, false);
    }

    static ActorState faceHappy() {
        return new ActorState(Type.FACE_HAPPY, null, null, WHITE, SUN, 0f, false, false);
    }

    static ActorState selfieFrame() {
        return new ActorState(Type.SELFIE_FRAME, null, null, WHITE, TEAL, 0f, false, false);
    }

    /** Branded closing frame: Bühler logo + tagline on black (Drehbuch end-cards). */
    static ActorState brand(String tagline) {
        return new ActorState(Type.BRAND, tagline, null, WHITE, SUN, 56f, false, false);
    }
}
