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
import com.buhlergroup.pepper.llm.LlmService;
import com.buhlergroup.pepper.llm.OpenAiCompatibleLlmService;
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

public class OpenAIService {

    private static final String TAG = "OpenAIService";
    private static final int MAX_OUTPUT_TOKENS = 600;
    private static final int RESPONSE_TIMEOUT_MS = 60000;
    private static final Pattern LANG_TAG =
            Pattern.compile("\\[\\[\\s*lang\\s*:\\s*([A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?)\\s*\\]\\]");
    private static final Pattern ACTION_TAG =
            Pattern.compile("\\[\\[\\s*action\\s*:\\s*([A-Za-z0-9_]+)\\s*\\]\\]");
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
        messages.add(message("system", formDefaultSystemPrompt(context)));
        messages.addAll(historyManager.toInput());

        Map<String, Object> body = new HashMap<>();
        body.put("messages", messages);
        body.put("max_tokens", MAX_OUTPUT_TOKENS);
        body.put("reasoning_effort", "low");

        long started = System.currentTimeMillis();
        try {
            String text = extractLanguageTag(parseChatContent(llm.chat(ModelTask.CONVERSATION, body, RESPONSE_TIMEOUT_MS)));
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
        if (text == null) {
            lastLanguageTag = null;
            return null;
        }
        Matcher matcher = LANG_TAG.matcher(text);
        lastLanguageTag = matcher.find() ? matcher.group(1) : null;
        String cleaned = LANG_TAG.matcher(text).replaceAll("");
        return ACTION_TAG.matcher(cleaned).replaceAll("").trim();
    }

    @Nullable
    public String lastLanguageTag() {
        return lastLanguageTag;
    }

    public String getResponseStreaming(HistoryManager historyManager, QiContext context,
                                       String userMessage, StreamListener listener) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", formRoutingSystemPrompt(context)));
        messages.addAll(historyManager.toInput());
        messages.add(message("user", userMessage));
        lastLanguageTag = null;
        return llm.streamChat(ModelTask.CONVERSATION, messages, MAX_OUTPUT_TOKENS, listener);
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
        } catch (Exception e) {
            Log.d(TAG, "appendRaffleHint failed", e);
        }
    }

    public void setC(Context c) {
        llm.setContext(c);
    }

    public interface StreamListener {
        boolean onAction(String actionName);

        void onSentence(String sentence, String languageTag);
    }
}
