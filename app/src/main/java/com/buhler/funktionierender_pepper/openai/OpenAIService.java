package com.buhler.funktionierender_pepper.openai;


import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.util.IOUtils;
import com.buhler.funktionierender_pepper.R;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.openai.history.HistoryManager;
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
    private final List<Action> actions;

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
        StringBuilder skills = new StringBuilder();
        for (Action action : actions) {
            skills.append("- ").append(action.getDescription()).append("\n");
        }
        return instructions + skills;
    }

    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body) throws IOException {
        URL url = new URL(URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestProperty("Authorization", "Bearer " + getAuthToken());
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

    public String getAuthToken() {
        Dotenv dotenv = Dotenv.configure()
                .directory("/assets")
                .filename("env")
                .load();

        String tok = dotenv.get("OPENAI_API_TOKEN");
        Log.i("TOKENAUTH", tok);
        return tok;
    }
}
