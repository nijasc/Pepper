package com.buhlergroup.pepper.llm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelCatalog {

    private static final LlmHttpClient httpClient = new LlmHttpClient();
    private static final Map<LlmProvider, List<String>> cache = new ConcurrentHashMap<>();

    private ModelCatalog() {
    }

    public static Result validate(LlmProvider provider, String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new Result(false, fallback(provider), "Kein API-Key");
        }
        try {
            List<String> models = httpClient.listModels(provider, apiKey.trim());
            if (models.isEmpty()) {
                return new Result(false, fallback(provider), "Keine Modelle erhalten");
            }
            cache.put(provider, models);
            return new Result(true, models, null);
        } catch (Exception e) {
            return new Result(false, fallback(provider), e.getMessage());
        }
    }

    public static List<String> models(LlmProvider provider) {
        List<String> cached = cache.get(provider);
        return cached != null && !cached.isEmpty() ? cached : fallback(provider);
    }

    private static List<String> fallback(LlmProvider provider) {
        return new ArrayList<>(Arrays.asList(provider.modelIds()));
    }

    public static final class Result {
        public final boolean ok;
        public final List<String> models;
        public final String error;

        Result(boolean ok, List<String> models, String error) {
            this.ok = ok;
            this.models = models;
            this.error = error;
        }
    }
}
