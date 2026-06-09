package com.buhler.funktionierender_pepper.action;


import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.dance.DanceAction;
import com.buhler.funktionierender_pepper.action.documentation.DocumentationAction;
import com.buhler.funktionierender_pepper.action.follow.FollowMeAction;
import com.buhler.funktionierender_pepper.action.highfive.HighFiveAction;
import com.buhler.funktionierender_pepper.action.lang.ChangeLanguageAction;
import com.buhler.funktionierender_pepper.action.memory.MemoryGameAction;
import com.buhler.funktionierender_pepper.action.names.SiriAction;
import com.buhler.funktionierender_pepper.action.saxophone.SaxophoneAction;
import com.buhler.funktionierender_pepper.action.say.SayAction;
import com.buhler.funktionierender_pepper.action.system.SystemInfoAction;
import com.buhler.funktionierender_pepper.action.test.TestAction;
import com.buhler.funktionierender_pepper.action.volume.ChangeVolumeAction;
import com.buhler.funktionierender_pepper.lang.LanguageManager;
import com.buhler.funktionierender_pepper.lang.SpeechManager;
import com.buhler.funktionierender_pepper.openai.history.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public class ActionHandler {
    private static final List<Action> actions = new ArrayList<>();
    private final IntentEngine intentEngine;
    private final HistoryManager historyManager;

    public ActionHandler(LanguageManager languageManager, HistoryManager historyManager) {
        this.historyManager = historyManager;
        initActions(languageManager);
        this.intentEngine = new IntentEngine(actions, historyManager);
    }

    public void handleInput(QiContext context, String input) {
        if (input.isEmpty()) {
            //SpeechManager.getInstance().say(context, "Pepper ist jetzt startklar!");
            return;
        }

        Action intent = intentEngine.getIntent(input);
        Log.i(this.getClass().getSimpleName(), "Found intent: " + intent.getClass().getSimpleName());
        intent.execute(context, input);
    }

    private void initActions(LanguageManager languageManager) {
        actions.add(new SayAction(actions));
        actions.add(new DanceAction());
        actions.add(new SiriAction());
        actions.add(new SaxophoneAction());
        actions.add(new HighFiveAction());
        actions.add(new ChangeLanguageAction(languageManager));
        actions.add(new ChangeVolumeAction());
        actions.add(new DocumentationAction(actions));
        actions.add(new TestAction());
        actions.add(new SystemInfoAction(languageManager, actions));
        actions.add(new FollowMeAction());
        actions.add(new MemoryGameAction());

        for (Action action : actions) {
            Log.i("ActionHandler", "Registered action: " + action.getClass().getSimpleName());
            action.setHistoryManager(historyManager);
        }
    }
}
