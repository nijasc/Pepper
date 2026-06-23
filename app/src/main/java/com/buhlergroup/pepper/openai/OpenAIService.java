package com.buhlergroup.pepper.openai;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.profile.ProfileRepository;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.llm.ChatStreamParser;
import com.buhlergroup.pepper.llm.LlmHttpClient;
import com.buhlergroup.pepper.llm.LlmProvider;
import com.buhlergroup.pepper.llm.LlmService;
import com.buhlergroup.pepper.llm.ModelSettings;
import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;
import com.buhlergroup.pepper.openai.history.HistoryManager;
import com.buhlergroup.pepper.perception.EmotionReader;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIService implements LlmService {

    private static final String TAG = "OpenAIService";
    private static final int MAX_OUTPUT_TOKENS = 600;
    private static final int RESPONSE_TIMEOUT_MS = 60000;
    private static final int DEFAULT_TIMEOUT_MS = 20000;
    private static final OpenAiCircuitBreaker circuitBreaker = new OpenAiCircuitBreaker();
    private static final Pattern LANG_TAG =
            Pattern.compile("\\[\\[\\s*lang\\s*:\\s*([A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?)\\s*\\]\\]");
    private static final Pattern ACTION_TAG =
            Pattern.compile("\\[\\[\\s*action\\s*:\\s*([A-Za-z0-9_]+)\\s*\\]\\]");
    private static volatile OpenAIService shared;
    private final EmotionReader emotionReader = new EmotionReader();
    private final List<Action> actions;
    private final LlmHttpClient llmClient = new LlmHttpClient();
    private volatile Context c;
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
        messages.add(message("system", formDefaultSystemPrompt(context)));
        messages.addAll(historyManager.toInput());

        Map<String, Object> body = new HashMap<>();
        body.put("messages", messages);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("reasoning_effort", "low");

        long started = System.currentTimeMillis();
        try {
            String res = chat(ModelTask.CONVERSATION, body, RESPONSE_TIMEOUT_MS);
            String text = extractLanguageTag(parseChatContent(res));
            Log.i("LATENCY", "getResponse took " + (System.currentTimeMillis() - started) + "ms");
            return text;
        } catch (IOException e) {
            DebugLog.get().w(TAG, "getResponse fehlgeschlagen: " + e.getMessage());
        }
        return "Etwas ist unerwartet schief gelaufen.";
    }

    @Override
    public String generate(ModelTask task, String systemInstructions, String userInput, int maxTokens)
            throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemInstructions));
        messages.add(message("user", userInput));

        Map<String, Object> body = new HashMap<>();
        body.put("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("reasoning_effort", "low");

        return parseChatContent(chat(task, body, RESPONSE_TIMEOUT_MS));
    }

    public String generateText(String instructions, String userInput, int maxTokens) throws IOException {
        return generate(ModelTask.GENERATION, instructions, userInput, maxTokens);
    }

    public String extractLanguageTag(String text) {
        if (text == null) {
            lastLanguageTag = null;
            return null;
        }
        Matcher matcher = LANG_TAG.matcher(text);
        lastLanguageTag = matcher.find() ? matcher.group(1) : null;
        String cleaned = LANG_TAG.matcher(text).replaceAll("");
        return ACTION_TAG.matcher(cleaned).replaceAll("").trim();
    }

    public String lastLanguageTag() {
        return lastLanguageTag;
    }

    public String getResponseStreaming(HistoryManager historyManager, QiContext context,
                                       String userMessage, StreamListener listener) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", formRoutingSystemPrompt(context)));
        messages.addAll(historyManager.toInput());
        messages.add(message("user", userMessage));

        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, ModelTask.CONVERSATION);
        Map<String, Object> body = new HashMap<>();
        body.put("model", ModelSettings.getModel(ctx, ModelTask.CONVERSATION));
        body.put("messages", messages);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("stream", true);
        if (provider.supportsReasoningEffort) {
            body.put("reasoning_effort", "low");
        }

        long started = System.currentTimeMillis();
        lastLanguageTag = null;
        DebugLog.get().setStatus(provider.displayName + " – Anfrage läuft …");
        DebugLog.get().d(TAG, "Streaming-Anfrage gestartet (" + provider.displayName + ")");

        if (circuitBreaker.isOpen()) {
            DebugLog.get().w(TAG, "LLM-Circuit offen – schneller Fallback");
            throw new IOException("LLM circuit open, failing fast to fallback");
        }

        boolean failed = false;
        LlmHttpClient.EventStream stream = null;
        try {
            stream = llmClient.openChatStream(provider, ModelSettings.getKey(ctx, provider), body);
            ChatStreamParser parser = new ChatStreamParser();
            String result = parser.parse(stream.reader, listener, started);
            lastLanguageTag = parser.lastLanguageTag();
            if (result == null) {
                return null;
            }
            Log.i("LATENCY", "streamed response complete after "
                    + (System.currentTimeMillis() - started) + "ms");
            DebugLog.get().setStatus(provider.displayName + " – Antwort erhalten");
            return result;
        } catch (IOException e) {
            failed = true;
            DebugLog.get().w(TAG, "LLM-Streaming fehlgeschlagen: " + e.getMessage());
            throw e;
        } finally {
            if (stream != null) {
                stream.disconnect();
            }
            if (failed) {
                circuitBreaker.recordFailure();
            } else {
                circuitBreaker.recordSuccess();
            }
        }
    }

    @Override
    public String chat(ModelTask task, Map<String, Object> body) throws IOException {
        return chat(task, body, DEFAULT_TIMEOUT_MS);
    }

    public String chat(ModelTask task, Map<String, Object> body, int readTimeoutMs) throws IOException {
        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, task);
        body.put("model", ModelSettings.getModel(ctx, task));
        if (!provider.supportsReasoningEffort) {
            body.remove("reasoning_effort");
            body.remove("reasoning");
        }
        return llmClient.request(provider, ModelSettings.getKey(ctx, provider),
                "/chat/completions", body, readTimeoutMs);
    }

    private String parseChatContent(String responseJson) throws IOException {
        try {
            return new JSONObject(responseJson)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            throw new IOException("Antwort ohne Inhalt: " + snippet(responseJson));
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private String snippet(String json) {
        if (json == null) {
            return "null";
        }
        return json.length() <= 300 ? json : json.substring(0, 300) + "…";
    }

    public String formDefaultSystemPrompt(QiContext context) {
        return buildSystemPrompt(context, false);
    }

    public String formRoutingSystemPrompt(QiContext context) {
        return buildSystemPrompt(context, true);
    }

    private String buildSystemPrompt(QiContext context, boolean withRouting) {
        ProfileRepository profiles = ProfileRepository.get(context);
        String instructions = profiles.getActiveInstructions(context);
        StringBuilder prompt = new StringBuilder(instructions);
        String contentSummary = profiles.getActiveContentSummary(context);
        if (contentSummary != null && !contentSummary.trim().isEmpty()) {
            prompt.append("\n## Wissensbasis\n").append(contentSummary).append("\n");
        }
        for (Action action : actions) {
            prompt.append("- ").append(action.getDescription()).append("\n");
        }

        prompt.append("\n## Internal Language Marker\n")
                .append("Begin every reply with a machine marker of the exact form [[lang:CODE]] where CODE is the ")
                .append("ISO 639-1 code of the language you are replying in (for example [[lang:de]], [[lang:en]], ")
                .append("[[lang:ja]]). Write the marker exactly once at the very start with nothing before it, then ")
                .append("your normal spoken reply. The marker is removed automatically before your reply is spoken ")
                .append("and must never appear inside the reply or influence its wording.\n");

        if (withRouting) {
            prompt.append("\n## Action Routing\n")
                    .append("Immediately after the language marker, decide which of these actions handles the ")
                    .append("user's message and write a second machine marker of the exact form [[action:NAME]]:\n");
            for (Action action : actions) {
                prompt.append("- ").append(action.getClass().getSimpleName()).append(": ")
                        .append(action.getDescription()).append('\n');
            }
            prompt.append("If you answer the user yourself with a normal spoken reply, use [[action:SayAction]] ")
                    .append("and then write the reply. For ANY other action output ONLY the two markers and ")
                    .append("nothing else - no reply text. Both markers are removed automatically and must ")
                    .append("never appear inside the spoken reply.\n");
        }

        String moodHint = emotionReader.moodHintForPrompt(context);
        if (moodHint != null) {
            prompt.append("\n## Visitor Emotion\n")
                    .append("The person in front of you right now ").append(moodHint).append(". ")
                    .append("You may occasionally and subtly acknowledge this if it fits the ")
                    .append("conversation, but never mention it in every reply and never force it.\n");
        }

        appendRaffleHint(context, prompt);

        return prompt.toString();
    }

    private void appendRaffleHint(Context context, StringBuilder prompt) {
        try {
            RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();
            if (raffle == null) {
                return;
            }
            if (raffle.status == RaffleStatus.ACTIVE) {
                String end = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                        .format(new Date(raffle.endDate));
                prompt.append("\n## Active Raffle\n")
                        .append("There is currently an active raffle the visitor can join: \"")
                        .append(raffle.title).append("\". ");
                if (!raffle.description.isEmpty()) {
                    prompt.append(raffle.description).append(' ');
                }
                prompt.append("It ends on ").append(end).append(". ")
                        .append("To take part the visitor gives their name and a valid e-mail address");
                if (raffle.requiresPhone) {
                    prompt.append(", and a phone number");
                }
                if (raffle.requiresSelfie) {
                    prompt.append(", and takes a selfie with you");
                }
                prompt.append(". Proactively and naturally invite the visitor to take part when it fits ")
                        .append("the conversation, but do not repeat it in every single reply.\n");
            } else if (raffle.status == RaffleStatus.ENDED) {
                prompt.append("\n## Raffle Ended\n")
                        .append("If the visitor asks about the raffle, tell them it has unfortunately ")
                        .append("already ended. Do not invite them to take part.\n");
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body) throws IOException {
        return chat(ModelTask.CLASSIFICATION, body == null ? new HashMap<>() : body);
    }

    public void setC(Context c) {
        this.c = c == null ? null : c.getApplicationContext();
    }

    private Context ctx() {
        return c != null ? c : ModelSettings.app();
    }

    public interface StreamListener {
        boolean onAction(String actionName);

        void onSentence(String sentence, String languageTag);
    }
}
