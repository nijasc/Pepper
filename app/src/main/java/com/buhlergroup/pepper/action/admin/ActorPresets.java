package com.buhlergroup.pepper.action.admin;

import android.content.Context;

import com.buhlergroup.pepper.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The ready-made Actor deck for the 2026 Sommerferien video. Every beat of the four
 * Drehbücher is a tap-to-fire preset that combines a branded display-state, a gesture or
 * held pose, and a spoken line — so Pepper performs the moment (moves + says it), not just
 * shows it. Grouped by Drehbuch so Nils can shoot one script top-to-bottom.
 */
final class ActorPresets {

    private ActorPresets() {
    }

    static List<ActorPreset> build(Context c) {
        List<ActorPreset> p = new ArrayList<>();

        // --- Gesten (alle Drehbücher) ------------------------------------------------
        String g = c.getString(R.string.actor_group_basics);
        p.add(ActorPreset.gesture(c.getString(R.string.actor_wave), g, R.raw.gesture_wave_hand, "Hoi!"));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_welcome), g, R.raw.gesture_welcome_arm, "Komm zu uns!"));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_highfive), g, R.raw.pepper_highfive, "High five!"));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_hands_up), g, R.raw.gesture_celebrate, "Juhuu!"));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_open_arms), g, R.raw.gesture_open_arms, null));
        p.add(ActorPreset.gesture(c.getString(R.string.actor_present), g, R.raw.gesture_present, null));

        // --- Display-States (generisch) ----------------------------------------------
        String d = c.getString(R.string.actor_group_display);
        p.add(ActorPreset.imagePicker(c.getString(R.string.actor_pick_image), d));
        p.add(ActorPreset.display("🌞 Sonne", d, ActorState.emoji("🌞"), null));
        p.add(ActorPreset.display("❤️ Herz", d, ActorState.emoji("❤️"), null));
        p.add(ActorPreset.display("🎉 Party", d, ActorState.emoji("🎉"), null));
        p.add(ActorPreset.display("Bühler-Endbild", d, ActorState.brand("Schöne Sommerferien!"), "Schöne Sommerferien!"));
        p.add(ActorPreset.display("Sauberer Hintergrund", d, ActorState.idle(), null));

        // --- Drehbuch A — Countdown --------------------------------------------------
        String a = c.getString(R.string.actor_group_a);
        p.add(ActorPreset.display("„GLEICH\" (blinkt)", a, ActorState.blinkBanner("GLEICH"), "Achtung!"));
        p.add(ActorPreset.display("3", a, ActorState.number("3"), "Drei"));
        p.add(ActorPreset.display("2", a, ActorState.number("2"), "Zwei"));
        p.add(ActorPreset.display("1", a, ActorState.number("1"), "Eins"));
        p.add(ActorPreset.pose("Feuerwerk + Jubel", a, ActorState.fireworks(), R.raw.gesture_celebrate, "Ferien!"));
        p.add(ActorPreset.display("🌞 FERIENMODUS", a, ActorState.banner("🌞 FERIENMODUS"), "Ferienmodus aktiviert!"));
        p.add(ActorPreset.sequence("▶ Auto: 3·2·1·Feuerwerk·Ferienmodus", a, Arrays.asList(
                new ActorPreset.Step(ActorState.blinkBanner("GLEICH"), 1000, "Achtung!"),
                new ActorPreset.Step(ActorState.number("3"), 900, "Drei"),
                new ActorPreset.Step(ActorState.number("2"), 900, "Zwei"),
                new ActorPreset.Step(ActorState.number("1"), 900, "Eins"),
                new ActorPreset.Step(ActorState.fireworks(), 2600, "Ferien!"),
                new ActorPreset.Step(ActorState.banner("🌞 FERIENMODUS"), 6000, "Ferienmodus aktiviert!"))));

        // --- Drehbuch B — Ehrlicher Roboter (Posen + Mimik) --------------------------
        String b = c.getString(R.string.actor_group_b);
        p.add(ActorPreset.display("Standby: „Ich bin ein Roboter\"", b, ActorState.faceNeutral(), "Ich bin ein Roboter."));
        p.add(ActorPreset.pose("Denkt: „Sommer versteh ich nicht\"", b, ActorState.faceThinking(), R.raw.thinking_chin_a001, "Sommer verstehe ich nicht."));
        p.add(ActorPreset.pose("Wendung: „Aber Freude!\"", b, ActorState.faceHappy(), R.raw.pose_proud, "Aber Freude — die verstehe ich."));

        // --- Drehbuch C — Team-Ferien (Posen) ----------------------------------------
        String cc = c.getString(R.string.actor_group_c);
        p.add(ActorPreset.pose("„Letzter Tag!\"", cc, ActorState.banner("Letzter Tag!"), R.raw.pose_proud, "Letzter Tag!"));
        p.add(ActorPreset.gesture("High Five (Team)", cc, R.raw.pepper_highfive, "High five!"));
        p.add(ActorPreset.pose("Selfie-Rahmen 📸", cc, ActorState.selfieFrame(), R.raw.gesture_present, "Einen fürs Album!"));
        p.add(ActorPreset.pose("Winken + 🌞", cc, ActorState.emoji("🌞"), R.raw.gesture_wave_hand, "Tschüss, schöne Ferien!"));
        p.add(ActorPreset.pose("„Bis im August!\"", cc, ActorState.banner("Bis im August!"), R.raw.gesture_wave_hand, "Bis im August!"));

        // --- Drehbuch D — Längster Countdown (Posen) ---------------------------------
        String dd = c.getString(R.string.actor_group_d);
        p.add(ActorPreset.display("Uhr 13:37 (deadpan)", dd, ActorState.clock("13:37"), "Letzter Tag vor den Ferien."));
        p.add(ActorPreset.pose("Uhr 13:39 (sackt zusammen)", dd, ActorState.clock("13:39"), R.raw.pose_slump, "Die Zeit. Vergeht. Nicht."));
        p.add(ActorPreset.pose("Sehnsucht ❤️ (späht raus)", dd, ActorState.emoji("❤️"), R.raw.pose_lean_out, null));
        p.add(ActorPreset.display("Uhr 15:59 (Spannung)", dd, ActorState.clock("15:59"), null));
        p.add(ActorPreset.pose("FEIERABEND! + Jubel", dd, ActorState.banner("FEIERABEND!"), R.raw.gesture_celebrate, "Feierabend!"));
        p.add(ActorPreset.display("Konfetti 🎉", dd, ActorState.confetti(), "Schöne Sommerferien!"));
        p.add(ActorPreset.sequence("▶ Auto: Uhr kriecht · FEIERABEND", dd, Arrays.asList(
                new ActorPreset.Step(ActorState.clock("13:37"), 1100, "Letzter Tag vor den Ferien."),
                new ActorPreset.Step(ActorState.clock("13:38"), 1000, null),
                new ActorPreset.Step(ActorState.clock("13:39"), 1100, "Die Zeit. Vergeht. Nicht."),
                new ActorPreset.Step(ActorState.emoji("❤️"), 900, null),
                new ActorPreset.Step(ActorState.clock("15:59"), 1500, null),
                new ActorPreset.Step(ActorState.banner("FEIERABEND!"), 2200, "Feierabend!"),
                new ActorPreset.Step(ActorState.confetti(), 6000, "Schöne Sommerferien!"))));

        return p;
    }
}
