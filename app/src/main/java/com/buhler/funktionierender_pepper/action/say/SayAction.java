package com.buhler.funktionierender_pepper.action.say;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.lang.SpeechManager;
import com.buhler.funktionierender_pepper.openai.OpenAIService;

import java.util.List;

public class SayAction extends Action {
    private final OpenAIService openAi;

    public SayAction(List<Action> actions) {
        this.openAi = new OpenAIService(actions);
    }

    @Override
    public void execute(QiContext context, String input) {
        getHistoryManager().addUser(input);
        String answer = openAi.getResponse(getHistoryManager(), context);
        getHistoryManager().addAssistant(answer, this);

        SpeechManager.getInstance().say(context, answer);
    }

    @Override
    public String getDescription() {
        return "Default action: answers general questions and handles anything no other action covers.";
    }
}
