package com.buhlergroup.pepper.openai;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.lang.SupportedLanguage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class SystemSpeechRewriter {

    private static final String TAG = "SysSpeech";
    private static final String MODEL =
            ModelSelector.modelFor(ModelSelector.ModelTask.REWRITE);
    private static final long TIMEOUT_MS = 1500;
    private static final int MIN_LENGTH = 12;
    private static final int MAX_CACHE_SIZE = 256;

    private static final SystemSpeechRewriter INSTANCE = new SystemSpeechRewriter();

    private final Map<String, String> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, String>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OpenAIService openAi = OpenAIService.shared();

    private SystemSpeechRewriter() {
    }

    public static SystemSpeechRewriter get() {
        return INSTANCE;
    }

    public String rewrite(Context context, String original, SupportedLanguage targetLang) {
        if (original == null) {
            return null;
        }
        if (original.trim().length() < MIN_LENGTH) {
            return original;
        }

        String key = targetLang.getAbbreviation() + "|" + original;
        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Future<String> future = executor.submit(() -> callModel(context, original, targetLang));
        try {
            String result = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (result != null && !result.trim().isEmpty()) {
                cache.put(key, result);
                return result;
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            Log.w(TAG, "Rewrite timed out after " + TIMEOUT_MS + "ms, speaking original");
        } catch (Exception e) {
            Log.w(TAG, "Rewrite failed, speaking original: " + e.getMessage());
        }
        return original;
    }

    private String callModel(Context context, String original, SupportedLanguage targetLang) throws Exception {
        openAi.setC(context);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt(targetLang)));
        messages.add(message("user", original));

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", messages);

        String response = openAi.chat(ModelSelector.ModelTask.REWRITE, body);
        return parseContent(response);
    }

    private String systemPrompt(SupportedLanguage targetLang) {
        return "You rewrite a robot's fixed system sentence so it sounds natural, warm and fluent "
                + "when spoken aloud in " + targetLang.getDisplayName() + " (" + targetLang.getAbbreviation() + "). "
                + "Preserve the exact meaning, keep it equally short, do not add or remove information, "
                + "do not add quotation marks or any extra formatting. "
                + "Reply with only the rewritten sentence.";
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private String parseContent(String response) throws Exception {
        String content = new JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
        if (content.length() >= 2 && content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1).trim();
        }
        return content;
    }
}
