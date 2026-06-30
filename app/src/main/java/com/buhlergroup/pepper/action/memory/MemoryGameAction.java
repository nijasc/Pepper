package com.buhlergroup.pepper.action.memory;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.openai.history.HistoryManager;

public class MemoryGameAction extends Action {

    public MemoryGameAction(HistoryManager historyManager) {
        super(historyManager);
    }

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
