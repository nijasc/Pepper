package com.buhlergroup.pepper.action;

import com.buhlergroup.pepper.openai.ModelSelector;
import com.buhlergroup.pepper.openai.ModelSelector.ModelTask;
import com.buhlergroup.pepper.openai.OpenAIService;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntentEngine {
    private static final String MODEL =
            ModelSelector.modelFor(ModelSelector.ModelTask.CLASSIFICATION);
    private final Map<String, Action> intents = new HashMap<>();
    private final OpenAIService openAi;
    private final HistoryManager historyManager;

    public IntentEngine(List<Action> actions, HistoryManager historyManager) {
        initIntents(actions);
        openAi = new OpenAIService(actions);
        this.historyManager = historyManager;
    }

    public Action getIntent(String input) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        List<Message> history = new ArrayList<>();
        for (Map<String, String> entry : historyManager.toInput()) {
            history.add(new Message(entry.get("role"), entry.get("content")));
        }

        Message sysMsg = new Message("system", buildSystemPrompt());
        Message userMessage = new Message("user", input);

        history.add(sysMsg);
        history.add(userMessage);

        body.put("messages", history);

        body.put("response_format", buildResponseFormat());

        try {
            String response = openAi.chat(ModelTask.CLASSIFICATION, body);
            String intentKey = parseIntent(response);
            return intents.get(intentKey);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder(
                "Classify the user's message into exactly one of the available intents.\n"
                        + "Pick the intent whose description best matches the user's request.\n"
                        + "Available intents:\n");
        for (Map.Entry<String, Action> entry : intents.entrySet()) {
            sb.append("- ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getDescription())
                    .append('\n');
        }
        return sb.toString();
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> valueProp = new HashMap<>();
        valueProp.put("type", "string");
        valueProp.put("enum", new ArrayList<>(intents.keySet()));

        Map<String, Object> properties = new HashMap<>();
        properties.put("value", valueProp);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.singletonList("value"));
        schema.put("additionalProperties", false);

        Map<String, Object> jsonSchema = new HashMap<>();
        jsonSchema.put("name", "intent");
        jsonSchema.put("strict", true);
        jsonSchema.put("schema", schema);

        Map<String, Object> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_schema");
        responseFormat.put("json_schema", jsonSchema);
        return responseFormat;
    }

    private String parseIntent(String response) throws JSONException {
        String content = new JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
        return new JSONObject(content).getString("value");
    }

    private void initIntents(List<Action> actions) {
        for (Action action : actions) {
            intents.put(action.getClass().getSimpleName(), action);
        }
    }

    static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}