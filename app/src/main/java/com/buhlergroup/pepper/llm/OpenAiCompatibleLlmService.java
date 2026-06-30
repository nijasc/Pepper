package com.buhlergroup.pepper.llm;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.debug.DebugLog;
import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.OpenAiCircuitBreaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OpenAiCompatibleLlmService implements LlmService {

    private static final String TAG = "LlmService";
    private static final int RESPONSE_TIMEOUT_MS = 60000;
    private static final int DEFAULT_TIMEOUT_MS = 20000;

    private final LlmHttpClient httpClient = new LlmHttpClient();
    private final OpenAiCircuitBreaker circuitBreaker = new OpenAiCircuitBreaker();
    private volatile Context context;

    @Override
    public void setContext(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    @Override
    public String chat(ModelTask task, Map<String, Object> body) throws IOException {
        return chat(task, body, DEFAULT_TIMEOUT_MS);
    }

    @Override
    public String chat(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException {
        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, task);
        String apiKey = ModelSettings.getKey(ctx, provider);
        applyReasoning(provider, body);

        List<String> candidates = candidateModels(provider, ModelSettings.getModel(ctx, task));
        LlmHttpException lastModelError = null;
        for (String model : candidates) {
            body.put("model", model);
            try {
                return httpClient.request(provider, apiKey, "/chat/completions", body, timeoutMs);
            } catch (LlmHttpException e) {
                if (e.isModelError()) {
                    DebugLog.get().w(TAG, "Modell '" + model + "' nicht verfügbar (HTTP "
                            + e.statusCode + ") – versuche Fallback-Modell");
                    lastModelError = e;
                    continue;
                }
                throw e;
            }
        }
        throw lastModelError != null
                ? lastModelError
                : new IOException("Kein verfügbares Modell");
    }

    @Override
    public String chatStrongest(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException {
        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, task);
        String apiKey = ModelSettings.getKey(ctx, provider);
        if (provider.maxReasoningEffort != null) {
            body.put("reasoning_effort", provider.maxReasoningEffort);
        } else {
            body.remove("reasoning_effort");
            body.remove("reasoning");
        }
        List<String> candidates = candidateModels(provider, provider.flagshipModel);
        LlmHttpException lastModelError = null;
        for (String model : candidates) {
            body.put("model", model);
            try {
                return httpClient.request(provider, apiKey, "/chat/completions", body, timeoutMs);
            } catch (LlmHttpException e) {
                if (e.isModelError()) {
                    DebugLog.get().w(TAG, "Stärkstes Modell '" + model + "' nicht verfügbar (HTTP "
                            + e.statusCode + ") – versuche Fallback-Modell");
                    lastModelError = e;
                    continue;
                }
                throw e;
            }
        }
        throw lastModelError != null
                ? lastModelError
                : new IOException("Kein verfügbares Modell");
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
        return chat(task, body, RESPONSE_TIMEOUT_MS);
    }

    @Override
    public String streamChat(ModelTask task, List<Map<String, String>> messages, int maxTokens,
                             OpenAIService.StreamListener listener) throws IOException {
        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, task);
        String apiKey = ModelSettings.getKey(ctx, provider);

        DebugLog.get().setStatus(provider.displayName + " – Anfrage läuft …");
        if (circuitBreaker.isOpen()) {
            DebugLog.get().w(TAG, "LLM-Circuit offen – schneller Fallback");
            throw new IOException("LLM circuit open, failing fast to fallback");
        }

        List<String> candidates = candidateModels(provider, ModelSettings.getModel(ctx, task));
        LlmHttpException lastModelError = null;
        for (String model : candidates) {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("max_tokens", maxTokens);
            body.put("stream", true);
            applyReasoning(provider, body);

            long started = System.currentTimeMillis();
            LlmHttpClient.EventStream stream = null;
            try {
                stream = httpClient.openChatStream(provider, apiKey, body);
                ChatStreamParser parser = new ChatStreamParser();
                String result = parser.parse(stream.reader, listener, started);
                circuitBreaker.recordSuccess();
                if (result == null) {
                    return null;
                }
                Log.i("LATENCY", "streamed response complete after "
                        + (System.currentTimeMillis() - started) + "ms");
                DebugLog.get().setStatus(provider.displayName + " – Antwort erhalten");
                return result;
            } catch (LlmHttpException e) {
                if (e.isModelError()) {
                    DebugLog.get().w(TAG, "Modell '" + model + "' nicht verfügbar (HTTP "
                            + e.statusCode + ") – versuche Fallback-Modell");
                    lastModelError = e;
                    continue;
                }
                circuitBreaker.recordFailure();
                DebugLog.get().w(TAG, "LLM-Streaming fehlgeschlagen: " + e.getMessage());
                throw e;
            } catch (IOException e) {
                circuitBreaker.recordFailure();
                DebugLog.get().w(TAG, "LLM-Streaming fehlgeschlagen: " + e.getMessage());
                throw e;
            } finally {
                if (stream != null) {
                    stream.disconnect();
                }
            }
        }

        circuitBreaker.recordFailure();
        DebugLog.get().w(TAG, "Kein verfügbares Modell für " + provider.displayName);
        throw lastModelError != null
                ? lastModelError
                : new IOException("Kein verfügbares Modell");
    }

    private List<String> candidateModels(LlmProvider provider, String preferred) {
        List<String> candidates = new ArrayList<>();
        if (preferred != null && !preferred.trim().isEmpty()) {
            candidates.add(preferred.trim());
        }
        for (String fallback : provider.modelIds()) {
            if (!candidates.contains(fallback)) {
                candidates.add(fallback);
            }
        }
        return candidates;
    }

    private void applyReasoning(LlmProvider provider, Map<String, Object> body) {
        if (!provider.supportsReasoningEffort) {
            body.remove("reasoning_effort");
            body.remove("reasoning");
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private Context ctx() {
        return context != null ? context : ModelSettings.app();
    }
}
