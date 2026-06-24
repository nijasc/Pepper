package com.buhlergroup.pepper.llm;

import android.content.Context;
import android.content.SharedPreferences;

import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;
import com.buhlergroup.pepper.openai.OpenAiTokenProvider;

public final class ModelSettings {

    public static final String DEFAULT_FAST = "gpt-4o-mini";
    public static final String DEFAULT_STRONG = "gpt-5.5";
    public static final String DEFAULT_GENERATION = "gpt-5.5";

    private static final String PREFS = "model_prefs";
    private static final String KEY_SEEDED = "seeded";

    private static volatile Context appContext;

    private ModelSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static Context app() {
        return appContext;
    }

    public static void ensureSeeded(Context context) {
        appContext = context.getApplicationContext();
        if (prefs(context).getBoolean(KEY_SEEDED, false)) {
            return;
        }
        String envToken = new OpenAiTokenProvider().getToken(context);
        if (envToken != null && !envToken.trim().isEmpty()) {
            setKey(context, LlmProvider.OPENAI, envToken.trim());
        }
        prefs(context).edit().putBoolean(KEY_SEEDED, true).apply();
    }

    public static String getKey(Context context, LlmProvider provider) {
        return prefs(context).getString("key_" + provider.name(), "");
    }

    public static void setKey(Context context, LlmProvider provider, String key) {
        prefs(context).edit().putString("key_" + provider.name(), key == null ? "" : key.trim()).apply();
    }

    public static boolean hasKey(Context context, LlmProvider provider) {
        return !getKey(context, provider).isEmpty();
    }

    public static LlmProvider getProvider(Context context, ModelTask task) {
        return LlmProvider.fromName(
                prefs(context).getString("task_" + task.name() + "_provider", null),
                LlmProvider.OPENAI);
    }

    public static String getModel(Context context, ModelTask task) {
        String stored = prefs(context).getString("task_" + task.name() + "_model", null);
        return stored != null && !stored.isEmpty() ? stored : defaultModel(task);
    }

    public static void setChoice(Context context, ModelTask task, LlmProvider provider, String model) {
        prefs(context).edit()
                .putString("task_" + task.name() + "_provider", provider.name())
                .putString("task_" + task.name() + "_model", model == null ? "" : model.trim())
                .apply();
    }

    public static String defaultModel(ModelTask task) {
        switch (task) {
            case CLASSIFICATION:
            case REWRITE:
                return DEFAULT_FAST;
            case GENERATION:
                return DEFAULT_GENERATION;
            case CONVERSATION:
            case DOCUMENTATION:
            default:
                return DEFAULT_STRONG;
        }
    }
}
