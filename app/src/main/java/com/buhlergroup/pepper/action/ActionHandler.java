package com.buhlergroup.pepper.action;


import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.admin.OpenAdminAction;
import com.buhlergroup.pepper.action.dance.DanceAction;
import com.buhlergroup.pepper.action.documentation.DocumentationAction;
import com.buhlergroup.pepper.action.dynamicanim.DynamicAnimationAction;
import com.buhlergroup.pepper.action.follow.FollowMeAction;
import com.buhlergroup.pepper.action.highfive.HighFiveAction;
import com.buhlergroup.pepper.action.hold.HoldMyBeerAction;
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
import com.buhlergroup.pepper.net.Connectivity;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionHandler {
    private static final List<Action> actions = new ArrayList<>();
    private final Map<String, Action> actionsByName = new HashMap<>();
    private final IntentEngine intentEngine;
    private final HistoryManager historyManager;
    private final OpenAIService routingService;

    public ActionHandler(LanguageManager languageManager, HistoryManager historyManager) {
        this.historyManager = historyManager;
        initActions(languageManager);
        this.intentEngine = new IntentEngine(actions, historyManager);
        this.routingService = new OpenAIService(actions);
        for (Action action : actions) {
            actionsByName.put(action.getClass().getSimpleName(), action);
        }
    }

    public void handleInput(QiContext context, String input) {
        if (input.isEmpty()) {
            return;
        }

        historyManager.addDeveloper("User input captured: \"" + input + "\"");

        if (!Connectivity.isOnline(context)) {
            Log.w(this.getClass().getSimpleName(), "No connectivity, skipping OpenAI call");
            announceOffline(context);
            return;
        }

        ThinkingController.get().start(context);
        try {
            if (!handleCombined(context, input)) {
                handleLegacy(context, input);
            }
        } finally {
            ThinkingController.get().stop();
        }
    }

    private void announceOffline(QiContext context) {
        SpeechManager.getInstance().say(context,
                "Ich habe gerade keine Verbindung. Bitte versuche es gleich noch einmal.");
    }

    private boolean handleCombined(QiContext context, String input) {
        final Action[] routed = new Action[1];
        final boolean[] spokeAny = new boolean[1];
        try {
            routingService.setC(context);
            String full = routingService.getResponseStreaming(historyManager, context, input,
                    new OpenAIService.StreamListener() {
                        @Override
                        public boolean onAction(String actionName) {
                            if (actionName == null || "SayAction".equals(actionName)) {
                                return true;
                            }
                            Action match = actionsByName.get(actionName);
                            if (match == null) {
                                return true;
                            }
                            routed[0] = match;
                            return false;
                        }

                        @Override
                        public void onSentence(String sentence, String languageTag) {
                            spokeAny[0] = true;
                            SpeechManager.getInstance().say(context, sentence, languageTag);
                        }
                    });

            Action target = routed[0];
            if (target != null) {
                Log.i(this.getClass().getSimpleName(),
                        "Routed intent: " + target.getClass().getSimpleName());
                historyManager.addDeveloper(
                        "Action started: " + target.getClass().getSimpleName(), target);
                target.execute(context, input);
                return true;
            }
            if (full != null && !full.trim().isEmpty()) {
                historyManager.addUser(input);
                historyManager.addAssistant(full.trim(), actionsByName.get("SayAction"));
                return true;
            }
            return spokeAny[0];
        } catch (Exception e) {
            Log.w(this.getClass().getSimpleName(),
                    "Combined turn failed, falling back to intent engine: " + e.getMessage());
            return spokeAny[0];
        }
    }

    private void handleLegacy(QiContext context, String input) {
        long intentStart = System.currentTimeMillis();
        Action intent = intentEngine.getIntent(input);
        long intentEnd = System.currentTimeMillis();
        Log.i("LATENCY", "getIntent took " + (intentEnd - intentStart) + "ms");
        if (intent == null) {
            Log.w(this.getClass().getSimpleName(), "No intent resolved for input: " + input);
            SpeechManager.getInstance().systemSay(context, "Entschuldige, das habe ich gerade nicht verstanden.");
            return;
        }
        Log.i(this.getClass().getSimpleName(), "Found intent: " + intent.getClass().getSimpleName());

        historyManager.addDeveloper("Action started: " + intent.getClass().getSimpleName(), intent);
        intent.execute(context, input);
        Log.i("LATENCY", "action " + intent.getClass().getSimpleName()
                + " took " + (System.currentTimeMillis() - intentEnd) + "ms");
    }

    private void initActions(LanguageManager languageManager) {
        actions.add(new SayAction(actions));
        actions.add(new DanceAction());
        actions.add(new DynamicAnimationAction());
        actions.add(new SiriAction(languageManager));
        actions.add(new SaxophoneAction());
        actions.add(new HighFiveAction());
        actions.add(new HoldMyBeerAction());
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
