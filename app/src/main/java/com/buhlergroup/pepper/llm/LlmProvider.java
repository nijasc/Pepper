package com.buhlergroup.pepper.llm;

public enum LlmProvider {
    OPENAI("OpenAI", "https://api.openai.com/v1", true,
            new String[]{"gpt-4o-mini", "gpt-4o", "gpt-4.1", "gpt-4.1-mini"}),
    GROK("Grok (xAI)", "https://api.x.ai/v1", true,
            new String[]{"grok-2-latest", "grok-2", "grok-beta"}),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", false,
            new String[]{"gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash"});

    public final String displayName;
    public final String baseUrl;
    public final boolean supportsReasoningEffort;
    public final String[] fallbackModels;

    LlmProvider(String displayName, String baseUrl, boolean supportsReasoningEffort,
                String[] fallbackModels) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.supportsReasoningEffort = supportsReasoningEffort;
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
