package com.buhlergroup.pepper.action.admin;

/**
 * One thing Pepper shows on its tablet during an Actor take. Every state is drawn in-app
 * on a branded Bühler backdrop (teal gradient + real Bühler logo wordmark) so the four
 * Sommerferien Drehbücher look like one polished campaign — no flat black screens, no
 * assets to prepare on the shoot day. {@code null} state = the branded idle backdrop
 * alone (clean gesture/pose loops). See {@link ActorPresets}.
 */
final class ActorState {

    enum Type { TEXT, EMOJI, FIREWORKS, CONFETTI, FACE_NEUTRAL, FACE_THINKING, FACE_HAPPY, SELFIE_FRAME, BRAND }

    // Bühler brand palette.
    static final int TEAL = 0xFF009E93;
    static final int TEAL_DARK = 0xFF00736B;
    static final int INK = 0xFF061318;       // deep near-black with a teal undertone
    static final int INK_2 = 0xFF0A2128;
    static final int WHITE = 0xFFFFFFFF;
    static final int SUN = 0xFFFFC857;       // warm summer accent, used sparingly

    final Type type;
    final String text;
    final int fg;
    final int gradTop;
    final int gradBottom;
    final int accent;          // glow / particle / bracket tint
    final float textSizeSp;
    final boolean monospace;
    final boolean blink;
    final boolean wordmark;    // show the small Bühler logo bottom-centre

    private ActorState(Type type, String text, int fg, int gradTop, int gradBottom,
                       int accent, float textSizeSp, boolean monospace, boolean blink, boolean wordmark) {
        this.type = type;
        this.text = text;
        this.fg = fg;
        this.gradTop = gradTop;
        this.gradBottom = gradBottom;
        this.accent = accent;
        this.textSizeSp = textSizeSp;
        this.monospace = monospace;
        this.blink = blink;
        this.wordmark = wordmark;
    }

    /** The branded idle backdrop (teal gradient + logo) with nothing on it. */
    static ActorState idle() {
        return new ActorState(Type.BRAND, null, WHITE, TEAL_DARK, INK, TEAL, 0f, false, false, false);
    }

    /** Huge centred countdown digit (Drehbuch A & D). */
    static ActorState number(String digit) {
        return new ActorState(Type.TEXT, digit, WHITE, TEAL_DARK, INK, TEAL, 300f, true, false, true);
    }

    /** Large clock time, e.g. "16:00" (Drehbuch D jump-cuts). */
    static ActorState clock(String time) {
        return new ActorState(Type.TEXT, time, WHITE, INK_2, INK, TEAL, 170f, true, false, true);
    }

    /** Big text banner on Bühler teal — FERIENMODUS, FEIERABEND, Letzter Tag … */
    static ActorState banner(String t) {
        return new ActorState(Type.TEXT, t, WHITE, TEAL, TEAL_DARK, SUN, 92f, false, false, true);
    }

    /** Blinking banner — "GLEICH" (Drehbuch A hook). */
    static ActorState blinkBanner(String t) {
        return new ActorState(Type.TEXT, t, SUN, INK, TEAL_DARK, SUN, 96f, false, true, true);
    }

    /** Full-screen emoji/symbol: 🌞 ❤️ 🎉 */
    static ActorState emoji(String e) {
        return new ActorState(Type.EMOJI, e, WHITE, INK_2, INK, TEAL, 240f, false, false, false);
    }

    static ActorState fireworks() {
        return new ActorState(Type.FIREWORKS, null, WHITE, 0xFF02292A, INK, TEAL, 0f, false, false, false);
    }

    static ActorState confetti() {
        return new ActorState(Type.CONFETTI, null, WHITE, TEAL_DARK, INK, SUN, 0f, false, false, false);
    }

    static ActorState faceNeutral() {
        return new ActorState(Type.FACE_NEUTRAL, null, WHITE, INK_2, INK, TEAL, 0f, false, false, false);
    }

    static ActorState faceThinking() {
        return new ActorState(Type.FACE_THINKING, null, WHITE, INK_2, INK, TEAL, 0f, false, false, false);
    }

    static ActorState faceHappy() {
        return new ActorState(Type.FACE_HAPPY, null, WHITE, TEAL, TEAL_DARK, SUN, 0f, false, false, false);
    }

    static ActorState selfieFrame() {
        return new ActorState(Type.SELFIE_FRAME, "📸", WHITE, INK, INK_2, TEAL, 0f, false, false, false);
    }

    /** Branded closing frame: big Bühler logo + tagline (Drehbuch end-cards). */
    static ActorState brand(String tagline) {
        return new ActorState(Type.BRAND, tagline, WHITE, TEAL, TEAL_DARK, SUN, 56f, false, false, false);
    }
}
