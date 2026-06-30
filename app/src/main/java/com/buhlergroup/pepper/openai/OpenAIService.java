package com.buhlergroup.pepper.openai;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.llm.ChatMessages;
import com.buhlergroup.pepper.llm.LlmService;
import com.buhlergroup.pepper.llm.OpenAiCompatibleLlmService;
import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;
import com.buhlergroup.pepper.openai.history.HistoryManager;
import com.buhlergroup.pepper.perception.EmotionReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAIService {

    private static final String TAG = "OpenAIService";
    private static final int MAX_OUTPUT_TOKENS = 600;
    private static final int RESPONSE_TIMEOUT_MS = 60000;
    private static volatile OpenAIService shared;
    private final EmotionReader emotionReader = new EmotionReader();
    private final List<Action> actions;
    private final LlmService llm = new OpenAiCompatibleLlmService();
    private String lastLanguageTag;

    public OpenAIService(List<Action> actions) {
        this.actions = actions;
    }

    public static OpenAIService shared() {
        OpenAIService instance = shared;
        if (instance == null) {
            synchronized (OpenAIService.class) {
                instance = shared;
                if (instance == null) {
                    instance = new OpenAIService(new ArrayList<>());
                    shared = instance;
                }
            }
        }
        return instance;
    }

    public String getResponse(HistoryManager historyManager, QiContext context) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(ChatMessages.of("system", formDefaultSystemPrompt(context)));
        messages.addAll(historyManager.toInput());

        Map<String, Object> body = new HashMap<>();
        body.put("messages", messages);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("reasoning_effort", "low");

        long started = System.currentTimeMillis();
        try {
            String text = extractLanguageTag(
                    ChatResponseParser.parseChatContent(llm.chat(ModelTask.CONVERSATION, body, RESPONSE_TIMEOUT_MS)));
            Log.i("LATENCY", "getResponse took " + (System.currentTimeMillis() - started) + "ms");
            return text;
        } catch (IOException e) {
            DebugLog.get().w(TAG, "getResponse fehlgeschlagen: " + e.getMessage());
        }
        return "Etwas ist unerwartet schief gelaufen.";
    }

    public String generateText(String instructions, String userInput, int maxTokens) throws IOException {
        return llm.generate(ModelTask.GENERATION, instructions, userInput, maxTokens);
    }

    public String chat(ModelTask task, Map<String, Object> body) throws IOException {
        return llm.chat(task, body);
    }

    public String chat(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException {
        return llm.chat(task, body, timeoutMs);
    }

    public String chatStrongest(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException {
        return llm.chatStrongest(task, body, timeoutMs);
    }

    @Nullable
    public String extractLanguageTag(String text) {
        ChatResponseParser.LanguageTagResult result = ChatResponseParser.extractLanguageTag(text);
        lastLanguageTag = result.languageTag;
        return result.text;
    }

    @Nullable
    public String lastLanguageTag() {
        return lastLanguageTag;
    }

    public String getResponseStreaming(HistoryManager historyManager, QiContext context,
                                       String userMessage, StreamListener listener) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(ChatMessages.of("system", formRoutingSystemPrompt(context)));
        messages.addAll(historyManager.toInput());
        messages.add(ChatMessages.of("user", userMessage));
        lastLanguageTag = null;
        return llm.streamChat(ModelTask.CONVERSATION, messages, MAX_OUTPUT_TOKENS, listener);
    }

    public String formDefaultSystemPrompt(QiContext context) {
        return SystemPromptBuilder.build(context, actions, emotionReader, false);
    }

    public String formRoutingSystemPrompt(QiContext context) {
        return SystemPromptBuilder.build(context, actions, emotionReader, true);
    }

    public void setC(Context c) {
        llm.setContext(c);
    }

    public interface StreamListener {
        boolean onAction(String actionName);

        void onSentence(String sentence, String languageTag);
    }
}
