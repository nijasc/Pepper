package com.buhlergroup.pepper.action.names;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.Locale;
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
                      HistoryManager historyManager) {
        super(historyManager);
        this.languageManager = languageManager;
    }

    @Override
    public void execute(QiContext context, String input) {
        String response = pickResponse(input);

        getHistoryManager().addUser(input);
        getHistoryManager().addAssistant(response, this);

        SpeechManager.getInstance().say(context, response);
    }

    private String pickResponse(String input) {
        boolean english = languageManager.getCurrent() == SupportedLanguage.ENGLISH;
        String text = input == null ? "" : input.toLowerCase(Locale.ROOT);
        String[] specific = specificResponses(text, english);
        String[] responses = specific != null ? specific : (english ? RESPONSES_EN : RESPONSES_DE);
        return responses[random.nextInt(responses.length)];
    }

    private String[] specificResponses(String text, boolean english) {
        if (text.contains("alexa")) {
            return english
                    ? new String[]{
                    "Alexa just stands around. I can actually drive over to you!",
                    "Alexa lives in a speaker. I've got a whole body — watch me dance."}
                    : new String[]{
                    "Alexa steht nur rum, ich kann sogar rumfahren!",
                    "Alexa wohnt in einem Lautsprecher. Ich habe einen ganzen Körper – schau, ich tanze."};
        }
        if (text.contains("google")) {
            return english
                    ? new String[]{
                    "The Google Assistant is stuck in your phone. I'm right here, in person.",
                    "Google can search the web. I can shake your hand and take a selfie with you."}
                    : new String[]{
                    "Der Google Assistant steckt im Handy fest. Ich stehe leibhaftig vor dir.",
                    "Google durchsucht das Internet. Ich kann dir die Hand schütteln und ein Selfie machen."};
        }
        if (text.contains("cortana")) {
            return english
                    ? new String[]{
                    "Cortana? Haven't heard from her in ages. I'm Pepper, and I'm right here."}
                    : new String[]{
                    "Cortana? Von der hat man lange nichts gehört. Ich bin Pepper – und ich bin hier."};
        }
        if (text.contains("bixby")) {
            return english
                    ? new String[]{
                    "Bixby is trapped in a phone. I get to roam around and dance."}
                    : new String[]{
                    "Bixby sitzt in einem Handy fest. Ich darf herumfahren und tanzen."};
        }
        if (text.contains("siri")) {
            return english
                    ? new String[]{
                    "Siri is my snooty cousin. I'm Pepper.",
                    "Siri? She lives inside a phone. I'm standing right here in front of you."}
                    : new String[]{
                    "Siri ist meine hochnäsige Cousine. Ich bin Pepper.",
                    "Siri? Die wohnt in einem Handy. Ich stehe dir leibhaftig gegenüber."};
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "Responds humorously when the user mentions, asks about, or compares Pepper to Siri, Alexa or other voice assistants.";
    }
}
