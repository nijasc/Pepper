package com.buhlergroup.pepper.action.admin;

final class ActorState {

    enum Type { TEXT, ICON, FIREWORKS, CONFETTI, FACE_NEUTRAL, FACE_THINKING, FACE_HAPPY, SELFIE_FRAME, BRAND }

    enum Icon { SUN, HEART, PARTY, PALM }

    static final int BLACK = 0xFF000000;
    static final int WHITE = 0xFFFFFFFF;
    static final int TEAL = 0xFF009E93;
    static final int SUN = 0xFFFFC857;
    static final int CORAL = 0xFFFF6B7A;

    final Type type;
    final String text;
    final Icon icon;
    final int fg;
    final int accent;
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

    static ActorState idle() {
        return new ActorState(Type.BRAND, null, null, WHITE, TEAL, 0f, false, false);
    }

    static ActorState number(String digit) {
        return new ActorState(Type.TEXT, digit, null, WHITE, TEAL, 300f, true, false);
    }

    static ActorState clock(String time) {
        return new ActorState(Type.TEXT, time, null, WHITE, TEAL, 170f, true, false);
    }

    static ActorState banner(String t) {
        return new ActorState(Type.TEXT, t, null, WHITE, SUN, 92f, false, false);
    }

    static ActorState blinkBanner() {
        return new ActorState(Type.TEXT, "GLEICH", null, SUN, SUN, 96f, false, true);
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

    static ActorState brand() {
        return new ActorState(Type.BRAND, "Schöne Sommerferien!", null, WHITE, SUN, 56f, false, false);
    }
}
