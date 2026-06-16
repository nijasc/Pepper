package com.buhlergroup.pepper.openai;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.util.IOUtils;
import com.buhlergroup.pepper.BuildConfig;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.openai.history.HistoryManager;
import com.buhlergroup.pepper.perception.EmotionReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenAIService {

    private static final String TAG = "OpenAIService";
    private static final int MAX_OUTPUT_TOKENS = 600;
    private static final int RESPONSE_TIMEOUT_MS = 60000;
    private static final String URL = "https://api.openai.com/v1";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmotionReader emotionReader = new EmotionReader();
    private final List<Action> actions;
    private static String cachedToken;
    private Context c;

    private static final int CIRCUIT_FAILURE_THRESHOLD = 3;
    private static final long CIRCUIT_COOLDOWN_MS = 30000;
    private static volatile int consecutiveFailures;
    private static volatile long circuitOpenUntilMs;

    private static synchronized boolean isCircuitOpen() {
        return System.currentTimeMillis() < circuitOpenUntilMs;
    }

    private static synchronized void recordSuccess() {
        consecutiveFailures = 0;
        circuitOpenUntilMs = 0;
    }

    private static synchronized void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= CIRCUIT_FAILURE_THRESHOLD) {
            circuitOpenUntilMs = System.currentTimeMillis() + CIRCUIT_COOLDOWN_MS;
            Log.w(TAG, "OpenAI circuit opened after " + consecutiveFailures
                    + " consecutive failures; failing fast for " + CIRCUIT_COOLDOWN_MS + "ms");
        }
    }

    private static final Pattern LANG_TAG =
            Pattern.compile("\\[\\[\\s*lang\\s*:\\s*([A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?)\\s*\\]\\]");
    private static final Pattern ACTION_TAG =
            Pattern.compile("\\[\\[\\s*action\\s*:\\s*([A-Za-z0-9_]+)\\s*\\]\\]");
    private static final Pattern LEADING_MARKER =
            Pattern.compile("^\\s*\\[\\[\\s*(lang|action)\\s*:\\s*([^\\]\\s]+)\\s*\\]\\]");
    private static final Pattern LANG_VALUE =
            Pattern.compile("[A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?");
    private String lastLanguageTag;

    public interface StreamListener {
        boolean onAction(String actionName);

        void onSentence(String sentence, String languageTag);
    }

    public OpenAIService(List<Action> actions) {
        this.actions = actions;
    }

    public String getResponse(HistoryManager historyManager, QiContext context) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", ModelSelector.modelFor(ModelSelector.ModelTask.CONVERSATION));
        body.put("input", historyManager.toInput());
        body.put("instructions", formDefaultSystemPrompt(context));
        body.put("max_output_tokens", MAX_OUTPUT_TOKENS);
        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        body.put("reasoning", reasoning);

        long started = System.currentTimeMillis();
        try {
            String res = sendOpenAiRequest("/responses", body, RESPONSE_TIMEOUT_MS);
            res = parseOutput(res);
            res = extractLanguageTag(res);
            Log.i("LATENCY", "getResponse took " + (System.currentTimeMillis() - started) + "ms");
            return sanitizeResponse(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Etwas ist unerwartet schief gelaufen.";
    }

    public String generateText(String instructions, String userInput, int maxTokens) throws IOException {
        List<Map<String, String>> input = new java.util.ArrayList<>();
        Map<String, String> userEntry = new HashMap<>();
        userEntry.put("role", "user");
        userEntry.put("content", userInput);
        input.add(userEntry);

        Map<String, Object> body = new HashMap<>();
        body.put("model", ModelSelector.modelFor(ModelSelector.ModelTask.GENERATION));
        body.put("input", input);
        body.put("instructions", instructions);
        body.put("max_output_tokens", maxTokens);
        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        body.put("reasoning", reasoning);

        String res = sendOpenAiRequest("/responses", body, RESPONSE_TIMEOUT_MS);
        return parseOutput(res);
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
        List<Map<String, String>> input = new java.util.ArrayList<>(historyManager.toInput());
        Map<String, String> userEntry = new HashMap<>();
        userEntry.put("role", "user");
        userEntry.put("content", userMessage);
        input.add(userEntry);

        Map<String, Object> body = new HashMap<>();
        body.put("model", ModelSelector.modelFor(ModelSelector.ModelTask.CONVERSATION));
        body.put("input", input);
        body.put("instructions", formRoutingSystemPrompt(context));
        body.put("max_output_tokens", MAX_OUTPUT_TOKENS);
        body.put("stream", true);
        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        body.put("reasoning", reasoning);

        long started = System.currentTimeMillis();
        lastLanguageTag = null;
        DebugLog.get().setStatus("OpenAI – Anfrage läuft …");
        DebugLog.get().d(TAG, "Streaming-Anfrage gestartet");

        if (isCircuitOpen()) {
            DebugLog.get().w(TAG, "OpenAI-Circuit offen – schneller Fallback");
            throw new IOException("OpenAI circuit open, failing fast to fallback");
        }

        HttpURLConnection con = (HttpURLConnection) new URL(URL + "/responses").openConnection();
        con.setConnectTimeout(8000);
        con.setReadTimeout(30000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + getAuthToken(c));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "text/event-stream");

        boolean failed = false;
        try {
            try (OutputStream os = con.getOutputStream()) {
                os.write(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            if (code >= 400) {
                throw new IOException("Streaming request failed with HTTP " + code);
            }

            StringBuilder pending = new StringBuilder();
            StringBuilder full = new StringBuilder();
            boolean markerPhase = true;
            boolean firstSentence = true;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty() || "[DONE]".equals(data)) {
                    break;
                }
                org.json.JSONObject event;
                try {
                    event = new org.json.JSONObject(data);
                } catch (org.json.JSONException e) {
                    continue;
                }
                String type = event.optString("type", "");
                if (type.endsWith("output_text.delta")) {
                    pending.append(event.optString("delta", ""));
                    if (markerPhase) {
                        int state = consumeMarkers(pending, listener);
                        if (state < 0) {
                            return null;
                        }
                        if (state == 0) {
                            continue;
                        }
                        markerPhase = false;
                    }
                    String sentence;
                    while ((sentence = extractSentence(pending, false)) != null) {
                        if (firstSentence) {
                            Log.i("LATENCY", "first streamed sentence after "
                                    + (System.currentTimeMillis() - started) + "ms");
                            firstSentence = false;
                        }
                        listener.onSentence(sentence, lastLanguageTag);
                        full.append(sentence).append(' ');
                    }
                } else if ("response.completed".equals(type)) {
                    break;
                } else if ("response.failed".equals(type) || "error".equals(type)) {
                    throw new IOException("Streaming response failed: " + data);
                }
            }

            if (markerPhase && consumeMarkers(pending, listener) < 0) {
                return null;
            }
            String tail;
            while ((tail = extractSentence(pending, true)) != null) {
                listener.onSentence(tail, lastLanguageTag);
                full.append(tail).append(' ');
            }
            Log.i("LATENCY", "streamed response complete after "
                    + (System.currentTimeMillis() - started) + "ms");
            DebugLog.get().setStatus("OpenAI – Antwort erhalten");
            DebugLog.get().i(TAG, "Streaming-Antwort komplett nach "
                    + (System.currentTimeMillis() - started) + "ms");
            return ACTION_TAG.matcher(full.toString()).replaceAll("").trim();
        } catch (IOException e) {
            failed = true;
            DebugLog.get().w(TAG, "OpenAI-Streaming fehlgeschlagen: " + e.getMessage());
            throw e;
        } finally {
            con.disconnect();
            if (failed) {
                recordFailure();
            } else {
                recordSuccess();
            }
        }
    }

    private int consumeMarkers(StringBuilder pending, StreamListener listener) {
        while (true) {
            Matcher matcher = LEADING_MARKER.matcher(pending);
            if (matcher.find()) {
                String kind = matcher.group(1);
                String value = matcher.group(2);
                pending.delete(0, matcher.end());
                if ("lang".equals(kind)) {
                    if (LANG_VALUE.matcher(value).matches()) {
                        lastLanguageTag = value;
                    }
                } else if (!listener.onAction(value)) {
                    return -1;
                }
                continue;
            }
            String current = pending.toString();
            String trimmed = current.trim();
            if (trimmed.isEmpty()) {
                return 0;
            }
            if (trimmed.startsWith("[") && current.length() < 80) {
                return 0;
            }
            return 1;
        }
    }

    private String extractSentence(StringBuilder pending, boolean flush) {
        int length = pending.length();
        for (int i = 0; i < length; i++) {
            char ch = pending.charAt(i);
            if (ch == '.' || ch == '!' || ch == '?' || ch == '…') {
                boolean boundary = i == length - 1
                        ? flush
                        : Character.isWhitespace(pending.charAt(i + 1));
                if (boundary && i >= 2) {
                    String sentence = pending.substring(0, i + 1).trim();
                    pending.delete(0, i + 1);
                    if (!sentence.isEmpty()) {
                        return sentence;
                    }
                }
            }
        }
        if (flush) {
            String rest = pending.toString().trim();
            pending.setLength(0);
            return rest.isEmpty() ? null : rest;
        }
        if (length > 300) {
            String chunk = pending.substring(0, length).trim();
            pending.setLength(0);
            return chunk.isEmpty() ? null : chunk;
        }
        return null;
    }

    private String parseOutput(String responseJson) throws IOException {
        OpenAiResponse res = objectMapper.readValue(responseJson, OpenAiResponse.class);
        Content out = null;
        if (res.getOutput() != null) {
            for (Output currOut : res.getOutput()) {
                if (currOut.getContent() != null && !currOut.getContent().isEmpty()) {
                    out = currOut.getContent().get(0);
                }
            }
        }
        if (out == null || out.getText() == null) {
            throw new IOException("OpenAI-Antwort ohne Output-Content: " + snippet(responseJson));
        }
        return out.getText();
    }

    private String snippet(String json) {
        if (json == null) {
            return "null";
        }
        return json.length() <= 300 ? json : json.substring(0, 300) + "…";
    }

    private String sanitizeResponse(String originalResponse) {
        return originalResponse;
    }

    public String formDefaultSystemPrompt(QiContext context) {
        return buildSystemPrompt(context, false);
    }

    public String formRoutingSystemPrompt(QiContext context) {
        return buildSystemPrompt(context, true);
    }

    private String buildSystemPrompt(QiContext context, boolean withRouting) {
        String instructions = IOUtils.fromRaw(context, R.raw.instructions);
        StringBuilder prompt = new StringBuilder(instructions);
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
                if (raffle.description != null && !raffle.description.isEmpty()) {
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

    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body) throws IOException {
        return sendOpenAiRequest(path, body, 20000);
    }

    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body, int readTimeoutMs)
            throws IOException {
        URL url = new URL(URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setConnectTimeout(8000);
        con.setReadTimeout(readTimeoutMs);
        con.setRequestProperty("Authorization", "Bearer " + getAuthToken(c));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip");

        if (body != null) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            String json = objectMapper.writeValueAsString(body);
            if (BuildConfig.DEBUG) {
                Map<String, Object> f = body;
                f.remove("instructions");
                Log.i("OPENREQ", objectMapper.writeValueAsString(f));
            }

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        } else {
            con.setRequestMethod("GET");
        }

        int code = con.getResponseCode();
        InputStream rawStream = code >= 400 ? con.getErrorStream() : con.getInputStream();
        if ("gzip".equalsIgnoreCase(con.getContentEncoding()) && rawStream != null) {
            rawStream = new GZIPInputStream(rawStream);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(rawStream, StandardCharsets.UTF_8)
        );

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "OpenAI Response: " + content);
        }
        return content.toString();
    }

    public String getAuthToken(Context context) {
        if (cachedToken != null) {
            return cachedToken;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("env"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int eq = line.indexOf('=');
                if (eq < 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();

                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }

                if ("OPENAI_API_TOKEN".equals(key)) {
                    cachedToken = value;
                }
            }
        } catch (IOException e) {
            Log.e("TOKENAUTH", "Failed to read env asset", e);
        }
        Log.i("TOKENAUTH", cachedToken == null ? "no token" : "token loaded (" + mask(cachedToken) + ")");
        return cachedToken;
    }

    private String mask(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }

    public void setC(Context c) {
        this.c = c;
    }
}
