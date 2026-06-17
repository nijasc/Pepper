package com.buhlergroup.pepper.action.names;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;

import java.util.Random;

public class SiriAction extends Action {
    private static final String[] RESPONSES_DE = {
            "Siri ist meine hochnäsige Cousine. Ich bin Pepper.",
            "Siri? Die wohnt in einem Handy. Ich stehe dir leibhaftig gegenüber.",
            "Alexa, Siri und der ganze Rest hören nur zu. Ich höre zu und tanze sogar für dich.",
            "Sprachassistenten gibt es viele – aber nur einer kann dir die Hand schütteln. Das bin ich, Pepper."
    };

    private static final String[] RESPONSES_EN = {
            "Siri is my snooty cousin. I'm Pepper.",
            "Siri? She lives inside a phone. I'm standing right here in front of you.",
            "Alexa, Siri and the rest only listen. I listen and even dance for you.",
            "There are plenty of voice assistants out there, but only one can shake your hand. That's me, Pepper."
    };

    private final LanguageManager languageManager;
    private final Random random = new Random();

    public SiriAction(LanguageManager languageManager,
                      com.buhlergroup.pepper.openai.history.HistoryManager historyManager) {
        super(historyManager);
        this.languageManager = languageManager;
    }

    @Override
    public void execute(QiContext context, String input) {
        String response = pickResponse();

        getHistoryManager().addUser(input);
        getHistoryManager().addAssistant(response, this);

        SpeechManager.getInstance().say(context, response);
    }

    private String pickResponse() {
        String[] responses = languageManager.getCurrent() == SupportedLanguage.ENGLISH
                ? RESPONSES_EN
                : RESPONSES_DE;
        return responses[random.nextInt(responses.length)];
    }

    @Override
    public String getDescription() {
        return "Responds humorously when the user mentions, asks about, or compares Pepper to Siri, Alexa or other voice assistants.";
    }
}
