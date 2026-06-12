package com.buhlergroup.pepper.action;


import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.admin.OpenAdminAction;
import com.buhlergroup.pepper.action.dance.DanceAction;
import com.buhlergroup.pepper.action.documentation.DocumentationAction;
import com.buhlergroup.pepper.action.dynamicanim.DynamicAnimationAction;
import com.buhlergroup.pepper.action.follow.FollowMeAction;
import com.buhlergroup.pepper.action.highfive.HighFiveAction;
import com.buhlergroup.pepper.action.lang.ChangeLanguageAction;
import com.buhlergroup.pepper.action.memory.MemoryGameAction;
import com.buhlergroup.pepper.action.names.SiriAction;
import com.buhlergroup.pepper.action.raffle.JoinRaffleAction;
import com.buhlergroup.pepper.action.raffle.RaffleInfoAction;
import com.buhlergroup.pepper.action.saxophone.SaxophoneAction;
import com.buhlergroup.pepper.action.say.SayAction;
import com.buhlergroup.pepper.action.selfie.SelfieAction;
import com.buhlergroup.pepper.action.system.SystemInfoAction;
import com.buhlergroup.pepper.action.test.TestAction;
import com.buhlergroup.pepper.action.thinking.ThinkingController;
import com.buhlergroup.pepper.action.volume.ChangeVolumeAction;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.history.HistoryManager;

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

        historyManager.addDeveloper("User input captured: \"" + input + "\"");

        ThinkingController.get().start(context);
        try {
            Action intent = intentEngine.getIntent(input);
            if (intent == null) {
                Log.w(this.getClass().getSimpleName(), "No intent resolved for input: " + input);
                SpeechManager.getInstance().systemSay(context, "Entschuldige, das habe ich gerade nicht verstanden.");
                return;
            }
            Log.i(this.getClass().getSimpleName(), "Found intent: " + intent.getClass().getSimpleName());

            historyManager.addDeveloper("Action started: " + intent.getClass().getSimpleName(), intent);
            intent.execute(context, input);
        } finally {
            ThinkingController.get().stop();
        }
    }

    private void initActions(LanguageManager languageManager) {
        actions.add(new SayAction(actions));
        actions.add(new DanceAction());
        actions.add(new DynamicAnimationAction());
        actions.add(new SiriAction(languageManager));
        actions.add(new SaxophoneAction());
        actions.add(new HighFiveAction());
        actions.add(new ChangeLanguageAction(languageManager));
        actions.add(new ChangeVolumeAction());
        actions.add(new DocumentationAction(actions));
        actions.add(new TestAction());
        actions.add(new SystemInfoAction(languageManager, actions));
        actions.add(new FollowMeAction());
        actions.add(new MemoryGameAction());
        actions.add(new SelfieAction());
        actions.add(new OpenAdminAction());
        actions.add(new RaffleInfoAction(actions));
        actions.add(new JoinRaffleAction());

        for (Action action : actions) {
            Log.i("ActionHandler", "Registered action: " + action.getClass().getSimpleName());
            action.setHistoryManager(historyManager);
        }
    }
}
