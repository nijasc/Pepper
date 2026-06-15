package com.buhlergroup.pepper.action.quiz;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.lang.SupportedLanguage;
import com.buhlergroup.pepper.net.Connectivity;
import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class QuizGenerator {

    private static final String TAG = "Quiz";
    private static final int MAX_TOKENS = 900;

    private QuizGenerator() {
    }

    public static List<QuizQuestion> generate(Context context, SupportedLanguage lang, int count) {
        if (!Connectivity.isOnline(context)) {
            return null;
        }
        try {
            OpenAIService service = new OpenAIService(new ArrayList<>());
            service.setC(context);
            String languageName = lang == SupportedLanguage.ENGLISH ? "English" : "German";
            String raw = service.generateText(instructions(languageName, count),
                    "Generate " + count + " quiz questions in " + languageName + ".", MAX_TOKENS);
            return parse(raw);
        } catch (Exception e) {
            Log.w(TAG, "LLM quiz generation failed: " + e.getMessage());
            return null;
        }
    }

    private static String instructions(String languageName, int count) {
        return "You are a friendly quiz master for the Swiss technology company Bühler at a trade "
                + "fair. Generate exactly " + count + " short multiple-choice quiz questions about "
                + "Bühler, industry, technology and careers, suitable for a general audience. "
                + "Write everything in " + languageName + ". Output ONLY valid JSON, no markdown "
                + "and no extra text, in exactly this form: "
                + "{\"questions\":[{\"question\":\"...\",\"options\":[\"a\",\"b\",\"c\",\"d\"],"
                + "\"correctIndex\":0}]}. Every question must have exactly four options with exactly "
                + "one correct answer; correctIndex is the 0-based index of the correct option. "
                + "Keep questions and options concise and factual.";
    }

    private static List<QuizQuestion> parse(String raw) {
        if (raw == null) {
            return null;
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(raw.substring(start, end + 1));
            JSONArray items = root.getJSONArray("questions");
            List<QuizQuestion> result = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONArray opts = item.optJSONArray("options");
                if (opts == null) {
                    continue;
                }
                List<String> options = new ArrayList<>();
                for (int j = 0; j < opts.length(); j++) {
                    options.add(opts.optString(j, ""));
                }
                QuizQuestion question = new QuizQuestion(
                        item.optString("question", ""), options, item.optInt("correctIndex", -1));
                if (question.isValid()) {
                    result.add(question);
                }
            }
            return result.isEmpty() ? null : result;
        } catch (Exception e) {
            Log.w(TAG, "Could not parse quiz JSON: " + e.getMessage());
            return null;
        }
    }
}
