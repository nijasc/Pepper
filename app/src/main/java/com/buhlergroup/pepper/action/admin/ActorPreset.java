package com.buhlergroup.pepper.action.admin;

import java.util.List;


final class ActorPreset {

    final String label;
    final String group;
    final ActorState state;
    final int gestureRaw;
    final String speech;
    final List<Step> sequence;
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
