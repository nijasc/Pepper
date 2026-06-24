package com.buhlergroup.pepper.llm;

public enum LlmProvider {
    OPENAI("OpenAI", "https://api.openai.com/v1", true, "gpt-5.5", "xhigh", new LlmModel[]{
            new LlmModel("gpt-5.5", "GPT-5.5 · Flaggschiff",
                    "Höchste Qualität und Logik – mehr Kosten und Latenz"),
            new LlmModel("gpt-5.4", "GPT-5.4 · Stark",
                    "Sehr stark und merklich schneller als 5.5 – guter Standard"),
            new LlmModel("gpt-5.4-mini", "GPT-5.4 mini · Schnell",
                    "Schnell und günstig für einfache Aufgaben"),
            new LlmModel("gpt-4.1-mini", "GPT-4.1 mini · Günstig",
                    "Sehr günstig, solide für Klassifikation und Umformulierung"),
            new LlmModel("gpt-4o-mini", "GPT-4o mini · Sparsam",
                    "Günstigste Option, breit verfügbar")
    }),
    GROK("Grok (xAI)", "https://api.x.ai/v1", true, "grok-4.3", "high", new LlmModel[]{
            new LlmModel("grok-4.3", "Grok 4.3 · Flaggschiff",
                    "Intelligentestes und schnellstes Grok, 1M Kontext"),
            new LlmModel("grok-4.1-fast", "Grok 4.1 Fast · Schnell",
                    "Sehr günstig und schnell, riesiger Kontext"),
            new LlmModel("grok-4", "Grok 4 · Stark",
                    "Bewährtes starkes Modell mit Echtzeit-Suche")
    }),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", false, "gemini-3.5-flash", null, new LlmModel[]{
            new LlmModel("gemini-3.5-flash", "Gemini 3.5 Flash · Flaggschiff",
                    "Frontier-Leistung bei Flash-Tempo"),
            new LlmModel("gemini-3.1-pro-preview", "Gemini 3.1 Pro · Stark",
                    "Tiefe Logik für komplexe Aufgaben (Preview)"),
            new LlmModel("gemini-3.1-flash-lite", "Gemini 3.1 Flash-Lite · Schnell",
                    "Günstigste und latenzärmste Option"),
            new LlmModel("gemini-2.5-pro", "Gemini 2.5 Pro · Stabil",
                    "Bewährtes starkes Modell"),
            new LlmModel("gemini-2.5-flash", "Gemini 2.5 Flash · Stabil",
                    "Bewährt schnell und günstig")
    });

    public final String displayName;
    public final String baseUrl;
    public final boolean supportsReasoningEffort;
    public final String flagshipModel;
    public final String maxReasoningEffort;
    public final LlmModel[] models;

    LlmProvider(String displayName, String baseUrl, boolean supportsReasoningEffort,
                String flagshipModel, String maxReasoningEffort, LlmModel[] models) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.supportsReasoningEffort = supportsReasoningEffort;
        this.flagshipModel = flagshipModel;
        this.maxReasoningEffort = maxReasoningEffort;
        this.models = models;
    }

    public String[] modelIds() {
        String[] ids = new String[models.length];
        for (int i = 0; i < models.length; i++) {
            ids[i] = models[i].id;
        }
        return ids;
    }

    public static LlmProvider fromName(String name, LlmProvider fallback) {
        if (name != null) {
            for (LlmProvider provider : values()) {
                if (provider.name().equals(name)) {
                    return provider;
                }
            }
        }
        return fallback;
    }
}
