package com.buhler.funktionierender_pepper.action.names;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

public class SiriAction extends Action {
    @Override
    public void execute(QiContext context, String input) {
        SpeechManager.getInstance().systemSay(context, "Siri ist meine hochnässige Cousine. Ich bin Pepper.");
    }

    @Override
    public String getDescription() {
        return "";
    }
}
