package com.buhler.funktionierender_pepper.action.memory;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;

public class MemoryGameAction extends Action {

    @Override
    public void execute(QiContext context, String input) {
        MemoryGameConfig config = MemoryGameConfig.fromInput(input);
        MemoryGameController.get().play(context, config);
    }

    @Override
    public String getDescription() {
        return "Starts the Simon-style memory minigame where the player repeats Pepper's color and sound sequence on the tablet.";
    }
}
