package com.buhlergroup.pepper.action.admin;

import java.util.List;

/**
 * One button in the Actor deck. Bundles what Pepper shows ({@link ActorState}, may be
 * {@code null} for a clean black gesture loop) with an optional looping gesture, or — for
 * a hands-free beat — a timed {@link Step} sequence that auto-advances on its own.
 */
final class ActorPreset {

    final String label;
    final String group;
    final ActorState state;
    final int gestureRaw;     // 0 = no gesture
    final List<Step> sequence; // null = single state
    final boolean picksImage;  // true = special "import own image" button

    private ActorPreset(String label, String group, ActorState state, int gestureRaw,
                        List<Step> sequence, boolean picksImage) {
        this.label = label;
        this.group = group;
        this.state = state;
        this.gestureRaw = gestureRaw;
        this.sequence = sequence;
        this.picksImage = picksImage;
    }

    boolean isSequence() {
        return sequence != null && !sequence.isEmpty();
    }

    static ActorPreset gesture(String label, String group, int gestureRaw) {
        return new ActorPreset(label, group, null, gestureRaw, null, false);
    }

    static ActorPreset display(String label, String group, ActorState state) {
        return new ActorPreset(label, group, state, 0, null, false);
    }

    static ActorPreset displayWithGesture(String label, String group, ActorState state, int gestureRaw) {
        return new ActorPreset(label, group, state, gestureRaw, null, false);
    }

    static ActorPreset sequence(String label, String group, List<Step> steps) {
        return new ActorPreset(label, group, null, 0, steps, false);
    }

    static ActorPreset imagePicker(String label, String group) {
        return new ActorPreset(label, group, null, 0, null, true);
    }

    /** One timed beat of an auto-sequence. */
    static final class Step {
        final ActorState state;
        final int holdMs;

        Step(ActorState state, int holdMs) {
            this.state = state;
            this.holdMs = holdMs;
        }
    }
}
