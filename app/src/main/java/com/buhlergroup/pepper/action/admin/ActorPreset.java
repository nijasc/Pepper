package com.buhlergroup.pepper.action.admin;

import java.util.List;

/**
 * One button in the Actor deck. Bundles what Pepper shows ({@link ActorState}, may be
 * {@code null} for the clean branded idle backdrop), an optional looping gesture/pose
 * (a {@code .qianim} raw res — celebrations move, B/C/D beats hold a posture), and an
 * optional spoken line ({@code speech}) so Pepper says the countdown/greeting instead of
 * only showing it. For a hands-free beat, a timed {@link Step} sequence auto-advances.
 */
final class ActorPreset {

    final String label;
    final String group;
    final ActorState state;
    final int gestureRaw;      // 0 = no gesture/pose
    final String speech;       // null = silent
    final List<Step> sequence; // null = single state
    final boolean picksImage;

    private ActorPreset(String label, String group, ActorState state, int gestureRaw,
                        String speech, List<Step> sequence, boolean picksImage) {
        this.label = label;
        this.group = group;
        this.state = state;
        this.gestureRaw = gestureRaw;
        this.speech = speech;
        this.sequence = sequence;
        this.picksImage = picksImage;
    }

    boolean isSequence() {
        return sequence != null && !sequence.isEmpty();
    }

    static ActorPreset gesture(String label, String group, int gestureRaw, String speech) {
        return new ActorPreset(label, group, null, gestureRaw, speech, null, false);
    }

    static ActorPreset display(String label, String group, ActorState state, String speech) {
        return new ActorPreset(label, group, state, 0, speech, null, false);
    }

    static ActorPreset pose(String label, String group, ActorState state, int gestureRaw, String speech) {
        return new ActorPreset(label, group, state, gestureRaw, speech, null, false);
    }

    static ActorPreset sequence(String label, String group, List<Step> steps) {
        return new ActorPreset(label, group, null, 0, null, steps, false);
    }

    static ActorPreset imagePicker(String label, String group) {
        return new ActorPreset(label, group, null, 0, null, null, true);
    }

    /** One timed beat of an auto-sequence: a state, how long to hold, and an optional line. */
    static final class Step {
        final ActorState state;
        final int holdMs;
        final String speech;

        Step(ActorState state, int holdMs, String speech) {
            this.state = state;
            this.holdMs = holdMs;
            this.speech = speech;
        }
    }
}
