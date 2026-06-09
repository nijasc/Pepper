package com.buhler.funktionierender_pepper.openai;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.util.IOUtils;
import com.buhler.funktionierender_pepper.R;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.openai.history.HistoryManager;
import com.buhler.funktionierender_pepper.perception.EmotionReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenAIService {

    public static final String DEFAULT_MODEL = "gpt-4";
    private static final String URL = "https://api.openai.com/v1";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmotionReader emotionReader = new EmotionReader();
    private final List<Action> actions;
    private static String cachedToken;
    private Context c;

    public OpenAIService(List<Action> actions) {
        this.actions = actions;
    }

    public String getResponse(HistoryManager historyManager, QiContext context) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", DEFAULT_MODEL);
        body.put("input", historyManager.toInput());
        body.put("instructions", formDefaultSystemPrompt(context));

        try {
            String res = sendOpenAiRequest("/responses", body);
            res = parseOutput(res);
            return sanitizeResponse(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Etwas ist unerwartet schief gelaufen.";
    }

    private String parseOutput(String responseJson) throws JsonProcessingException {
        OpenAiResponse res = objectMapper.readValue(responseJson, OpenAiResponse.class);
        Content out = null;
        for (Output currOut : res.getOutput()) {
            if (!currOut.getContent().isEmpty()) {
                out = currOut.getContent().get(0);
            }
        }
        return out.getText();
    }

    private String sanitizeResponse(String originalResponse) {
        return originalResponse;
    }

    public String formDefaultSystemPrompt(QiContext context) {
        String instructions = IOUtils.fromRaw(context, R.raw.instructions);
        StringBuilder prompt = new StringBuilder(instructions);
        for (Action action : actions) {
            prompt.append("- ").append(action.getDescription()).append("\n");
        }

        String moodHint = emotionReader.moodHintForPrompt(context);
        if (moodHint != null) {
            prompt.append("\n## Visitor Emotion\n")
                    .append("The person in front of you right now ").append(moodHint).append(". ")
                    .append("You may occasionally and subtly acknowledge this if it fits the ")
                    .append("conversation, but never mention it in every reply and never force it.\n");
        }

        return prompt.toString();
    }

    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body) throws IOException {
        URL url = new URL(URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestProperty("Authorization", "Bearer " + getAuthToken(c));
        con.setRequestProperty("Content-Type", "application/json");

        if (body != null) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            String json = objectMapper.writeValueAsString(body);
            Map<String, Object> f = body;
            f.remove("instructions");
            String js = objectMapper.writeValueAsString(f);
            Log.i("OPENREQ", js);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        } else {
            con.setRequestMethod("GET");
        }

        int code = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(code >= 400 ? con.getErrorStream() : con.getInputStream())
        );

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();
        System.out.println("OpenAI Response: " + content);
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
        Log.i("TOKENAUTH", cachedToken);
        return cachedToken;
    }

    public void setC(Context c) {
        this.c = c;
    }
}
