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
import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.lang.SystemMessages;
import com.buhlergroup.pepper.net.Connectivity;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionHandler {
    private static final int MAX_ATTEMPTS = 2;
    private static final long RETRY_BACKOFF_MS = 600;

    private enum CombinedResult {
        HANDLED,
        NOT_HANDLED,
        NETWORK_ERROR
    }

    private static final List<Action> actions = new ArrayList<>();
    private final Map<String, Action> actionsByName = new HashMap<>();
    private final IntentEngine intentEngine;
    private final HistoryManager historyManager;
    private final LanguageManager languageManager;
    private final OpenAIService routingService;

    public ActionHandler(LanguageManager languageManager, HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.languageManager = languageManager;
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
            CombinedResult result = handleCombined(context, input);
            if (result == CombinedResult.HANDLED) {
                return;
            }
            if (result == CombinedResult.NETWORK_ERROR) {
                announceOffline(context);
                return;
            }
            handleLegacy(context, input);
        } finally {
            ThinkingController.get().stop();
        }
    }

    private void announceOffline(QiContext context) {
        SupportedLanguage lang = languageManager.getCurrent();
        SpeechManager.getInstance().say(context, SystemMessages.offline(lang));
    }

    private CombinedResult handleCombined(QiContext context, String input) {
        routingService.setC(context);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            CombinedResult result = attemptCombined(context, input, attempt);
            if (result != CombinedResult.NETWORK_ERROR) {
                return result;
            }
            if (attempt < MAX_ATTEMPTS && !backoff()) {
                break;
            }
        }
        return CombinedResult.NETWORK_ERROR;
    }

    private boolean backoff() {
        try {
            Thread.sleep(RETRY_BACKOFF_MS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private CombinedResult attemptCombined(QiContext context, String input, int attempt) {
        final Action[] routed = new Action[1];
        final boolean[] spokeAny = new boolean[1];
        try {
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
                return CombinedResult.HANDLED;
            }
            if (full != null && !full.trim().isEmpty()) {
                historyManager.addUser(input);
                historyManager.addAssistant(full.trim(), actionsByName.get("SayAction"));
                return CombinedResult.HANDLED;
            }
            return spokeAny[0] ? CombinedResult.HANDLED : CombinedResult.NOT_HANDLED;
        } catch (IOException e) {
            Log.w(this.getClass().getSimpleName(), "Combined turn attempt " + attempt
                    + " failed with network error: " + e.getMessage());
            return spokeAny[0] ? CombinedResult.HANDLED : CombinedResult.NETWORK_ERROR;
        } catch (Exception e) {
            Log.w(this.getClass().getSimpleName(),
                    "Combined turn failed, falling back to intent engine: " + e.getMessage());
            return spokeAny[0] ? CombinedResult.HANDLED : CombinedResult.NOT_HANDLED;
        }
    }

    public void runAction(QiContext context, String actionName, String input) {
        Action action = actionsByName.get(actionName);
        if (action == null) {
            Log.w(this.getClass().getSimpleName(), "Unknown tile action: " + actionName);
            return;
        }
        Log.i(this.getClass().getSimpleName(), "Tile action: " + actionName);
        historyManager.addDeveloper("Action started (tile): " + actionName, action);
        action.execute(context, input == null ? "" : input);
    }

    private void handleLegacy(QiContext context, String input) {
        long intentStart = System.currentTimeMillis();
        Action intent = intentEngine.getIntent(input);
        long intentEnd = System.currentTimeMillis();
        Log.i("LATENCY", "getIntent took " + (intentEnd - intentStart) + "ms");
        if (intent == null) {
            Log.w(this.getClass().getSimpleName(), "No intent resolved for input: " + input);
            SpeechManager.getInstance().say(context,
                    SystemMessages.notUnderstood(languageManager.getCurrent()));
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
