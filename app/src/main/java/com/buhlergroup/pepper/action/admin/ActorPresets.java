package com.buhlergroup.pepper.action.admin;

import android.content.Context;

import com.buhlergroup.pepper.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The ready-made Actor deck for the 2026 Sommerferien video. Every beat of the four
 * Drehbücher (A Countdown, B Ehrlicher Roboter, C Team-Ferien, D Längster Countdown) is a
 * tap-to-fire preset — no graphics to prepare on the shoot day. Grouped by Drehbuch so
 * Nils can shoot one script top-to-bottom.
 */
final class ActorPresets {

    private ActorPresets() {
    }

    static List<ActorPreset> build(Context c) {
        List<ActorPreset> p = new ArrayList<>();

        // --- Gesten (alle Drehbücher) ------------------------------------------------
        String g = c.getString(R.string.actor_group_basics);
        p.add(ActorPreset.gesture(c.getString(R.string.actor_wave), g, R.raw.gesture_wave_hand));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_welcome), g, R.raw.gesture_welcome_arm));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_highfive), g, R.raw.pepper_highfive));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_hands_up), g, R.raw.gesture_hands_up));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_open_arms), g, R.raw.gesture_open_arms));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_raise_hand), g, R.raw.raise_right_hand_b001));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_present), g, R.raw.gesture_present));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_eagle), g, R.raw.gesture_eagle));

        // --- Display-States (generisch) ----------------------------------------------
        String d = c.getString(R.string.actor_group_display);
        p.add(ActorPreset.imagePicker(c.getString(R.string.actor_pick_image), d));
        p.add(ActorPreset.display("🌞 Sonne", d, ActorState.emoji("🌞")));
        p.add(ActorPreset.display("❤️ Herz", d, ActorState.emoji("❤️")));
        p.add(ActorPreset.display("🎉 Party", d, ActorState.emoji("🎉")));
        p.add(ActorPreset.display("Schwarz (clean)", d, null));

        // --- Drehbuch A — Countdown --------------------------------------------------
        String a = c.getString(R.string.actor_group_a);
        p.add(ActorPreset.display("„GLEICH\" (blinkt)", a, ActorState.blinkBanner("GLEICH", ActorState.SUN, ActorState.BLACK)));
        p.add(ActorPreset.display("3", a, ActorState.number("3")));
        p.add(ActorPreset.display("2", a, ActorState.number("2")));
        p.add(ActorPreset.display("1", a, ActorState.number("1")));
        p.add(ActorPreset.displayWithGesture("Feuerwerk + Jubel", a, ActorState.fireworks(), R.raw.gesture_hands_up));
        p.add(ActorPreset.display("🌞 FERIENMODUS", a, ActorState.banner("🌞 FERIENMODUS", ActorState.NIGHT, ActorState.SUN)));
        p.add(ActorPreset.sequence("▶ Auto: 3·2·1·Feuerwerk·Ferienmodus", a, Arrays.asList(
                new ActorPreset.Step(ActorState.blinkBanner("GLEICH", ActorState.SUN, ActorState.BLACK), 1000),
                new ActorPreset.Step(ActorState.number("3"), 900),
                new ActorPreset.Step(ActorState.number("2"), 900),
                new ActorPreset.Step(ActorState.number("1"), 900),
                new ActorPreset.Step(ActorState.fireworks(), 2600),
                new ActorPreset.Step(ActorState.banner("🌞 FERIENMODUS", ActorState.NIGHT, ActorState.SUN), 6000))));

        // --- Drehbuch B — Ehrlicher Roboter ------------------------------------------
        String b = c.getString(R.string.actor_group_b);
        p.add(ActorPreset.display("Standby / neutral", b, ActorState.faceNeutral()));
        p.add(ActorPreset.display("„?\" denkt nach", b, ActorState.faceThinking()));
        p.add(ActorPreset.display("Lächelnde Augen 🌞", b, ActorState.faceHappy()));

        // --- Drehbuch C — Team-Ferien ------------------------------------------------
        String cc = c.getString(R.string.actor_group_c);
        p.add(ActorPreset.display("„Letzter Tag!\"", cc, ActorState.banner("Letzter Tag!", ActorState.SUN, ActorState.NIGHT)));
        p.add(ActorPreset.display("Selfie-Rahmen 📸", cc, ActorState.selfieFrame()));
        p.add(ActorPreset.displayWithGesture("Winken + 🌞", cc, ActorState.emoji("🌞"), R.raw.gesture_wave_hand));
        p.add(ActorPreset.display("„Bis im August!\"", cc, ActorState.banner("Bis im August!", ActorState.NIGHT, ActorState.SUN)));

        // --- Drehbuch D — Längster Countdown -----------------------------------------
        String dd = c.getString(R.string.actor_group_d);
        p.add(ActorPreset.display("Uhr 13:37", dd, ActorState.clock("13:37")));
        p.add(ActorPreset.display("Uhr 13:39", dd, ActorState.clock("13:39")));
        p.add(ActorPreset.display("Uhr 15:59", dd, ActorState.clock("15:59")));
        p.add(ActorPreset.display("❤️ Sehnsucht", dd, ActorState.emoji("❤️")));
        p.add(ActorPreset.display("Uhr 16:00", dd, ActorState.clock("16:00")));
        p.add(ActorPreset.displayWithGesture("FEIERABEND! + Jubel", dd, ActorState.banner("FEIERABEND!", ActorState.SUN, ActorState.SUN_DEEP), R.raw.gesture_hands_up));
        p.add(ActorPreset.display("Konfetti 🎉", dd, ActorState.confetti()));
        p.add(ActorPreset.sequence("▶ Auto: Uhr kriecht · FEIERABEND", dd, Arrays.asList(
                new ActorPreset.Step(ActorState.clock("13:37"), 1100),
                new ActorPreset.Step(ActorState.clock("13:38"), 1100),
                new ActorPreset.Step(ActorState.clock("13:39"), 1100),
                new ActorPreset.Step(ActorState.emoji("❤️"), 900),
                new ActorPreset.Step(ActorState.clock("15:59"), 1400),
                new ActorPreset.Step(ActorState.banner("FEIERABEND!", ActorState.SUN, ActorState.SUN_DEEP), 2200),
                new ActorPreset.Step(ActorState.confetti(), 6000))));

        return p;
    }
}
