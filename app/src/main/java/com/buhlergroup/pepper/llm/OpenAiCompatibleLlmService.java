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
        body.put("model", ModelSettings.getModel(ctx, task));
        applyReasoning(provider, body);
        return httpClient.request(provider, ModelSettings.getKey(ctx, provider),
                "/chat/completions", body, timeoutMs);
    }

    @Override
    public String chatStrongest(ModelTask task, Map<String, Object> body, int timeoutMs) throws IOException {
        Context ctx = ctx();
        LlmProvider provider = ModelSettings.getProvider(ctx, task);
        body.put("model", provider.flagshipModel);
        if (provider.maxReasoningEffort != null) {
            body.put("reasoning_effort", provider.maxReasoningEffort);
        } else {
            body.remove("reasoning_effort");
            body.remove("reasoning");
        }
        applyWebSearch(provider, body);
        return httpClient.request(provider, ModelSettings.getKey(ctx, provider),
                "/chat/completions", body, timeoutMs);
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
        Map<String, Object> body = new HashMap<>();
        body.put("model", ModelSettings.getModel(ctx, task));
        body.put("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("stream", true);
        applyReasoning(provider, body);

        long started = System.currentTimeMillis();
        DebugLog.get().setStatus(provider.displayName + " – Anfrage läuft …");
        if (circuitBreaker.isOpen()) {
            DebugLog.get().w(TAG, "LLM-Circuit offen – schneller Fallback");
            throw new IOException("LLM circuit open, failing fast to fallback");
        }

        boolean failed = false;
        LlmHttpClient.EventStream stream = null;
        try {
            stream = httpClient.openChatStream(provider, ModelSettings.getKey(ctx, provider), body);
            ChatStreamParser parser = new ChatStreamParser();
            String result = parser.parse(stream.reader, listener, started);
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

    private void applyReasoning(LlmProvider provider, Map<String, Object> body) {
        if (!provider.supportsReasoningEffort) {
            body.remove("reasoning_effort");
            body.remove("reasoning");
        }
    }

    private void applyWebSearch(LlmProvider provider, Map<String, Object> body) {
        switch (provider) {
            case OPENAI:
                body.put("web_search_options", new HashMap<>());
                break;
            case GROK:
                Map<String, Object> search = new HashMap<>();
                search.put("mode", "auto");
                body.put("search_parameters", search);
                break;
            case GEMINI:
            default:
                break;
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
