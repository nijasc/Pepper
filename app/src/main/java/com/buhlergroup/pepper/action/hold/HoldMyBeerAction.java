package com.buhlergroup.pepper.action.hold;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;

public class HoldMyBeerAction extends Action {

    public HoldMyBeerAction(com.buhlergroup.pepper.openai.history.HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        HoldController.get().requestHold(context);
    }

    @Override
    public String getDescription() {
        return "Makes Pepper hold a small, light object such as a drink, cup or bottle in its right hand "
                + "for the visitor, e.g. \"hold my beer\", \"halt mal mein Bier\", \"hold this for me\", "
                + "\"halt das mal kurz\". Pepper raises its hand, takes the object and keeps holding it "
                + "until the visitor says stop or presses the stop button. Only for light objects, never "
                + "for heavy items.";
    }
}
