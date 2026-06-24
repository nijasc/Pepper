package com.buhlergroup.pepper.llm;

public enum LlmProvider {
    OPENAI("OpenAI", "https://api.openai.com/v1", true, "gpt-5.5", "xhigh",
            new String[]{"gpt-5.5", "gpt-5.5-instant", "gpt-5.4", "gpt-4o-mini"}),
    GROK("Grok (xAI)", "https://api.x.ai/v1", true, "grok-4.3", "high",
            new String[]{"grok-4.3", "grok-4.1", "grok-4", "grok-3-mini"}),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", false, "gemini-3.5-pro", null,
            new String[]{"gemini-3.5-pro", "gemini-3.5-flash", "gemini-3.1-flash-lite", "gemini-2.5-flash"});

    public final String displayName;
    public final String baseUrl;
    public final boolean supportsReasoningEffort;
    public final String flagshipModel;
    public final String maxReasoningEffort;
    public final String[] fallbackModels;

    LlmProvider(String displayName, String baseUrl, boolean supportsReasoningEffort,
                String flagshipModel, String maxReasoningEffort, String[] fallbackModels) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.supportsReasoningEffort = supportsReasoningEffort;
        this.flagshipModel = flagshipModel;
        this.maxReasoningEffort = maxReasoningEffort;
        this.fallbackModels = fallbackModels;
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
