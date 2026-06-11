package com.buhlergroup.pepper.action.say;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.OpenAIService;

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
