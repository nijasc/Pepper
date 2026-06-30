package com.buhlergroup.pepper.llm;

import android.util.Log;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class LlmHttpClient {

    private static final String TAG = "LlmHttpClient";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String request(LlmProvider provider, String apiKey, String path,
                          @Nullable Map<String, Object> body, int readTimeoutMs) throws IOException {
        HttpURLConnection con = open(provider, apiKey, path);
        try {
            con.setReadTimeout(readTimeoutMs);
            con.setRequestProperty("Accept-Encoding", "gzip");
            if (body != null) {
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                byte[] json = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json, 0, json.length);
                }
            } else {
                con.setRequestMethod("GET");
            }
            return readBody(con);
        } finally {
            con.disconnect();
        }
    }

    public EventStream openChatStream(LlmProvider provider, String apiKey, Map<String, Object> body)
            throws IOException {
        HttpURLConnection con = open(provider, apiKey, "/chat/completions");
        con.setReadTimeout(30000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Accept", "text/event-stream");
        try {
            try (OutputStream os = con.getOutputStream()) {
                os.write(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            if (code >= 400) {
                throw new LlmHttpException(code, "Streaming request failed with HTTP " + code);
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            return new EventStream(reader, con);
        } catch (IOException e) {
            con.disconnect();
            throw e;
        }
    }

    public List<String> listModels(LlmProvider provider, String apiKey) throws IOException {
        String response = request(provider, apiKey, "/models", null, 12000);
        List<String> models = new ArrayList<>();
        try {
            JSONArray data = new JSONObject(response).optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    String id = data.getJSONObject(i).optString("id", "");
                    if (!id.isEmpty()) {
                        models.add(id.startsWith("models/") ? id.substring("models/".length()) : id);
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Modell-Liste nicht lesbar: " + e.getMessage());
        }
        return models;
    }

    private HttpURLConnection open(LlmProvider provider, String apiKey, String path) throws IOException {
        URL url = new URL(provider.baseUrl + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(8000);
        con.setRequestProperty("Authorization", "Bearer " + apiKey);
        con.setRequestProperty("Content-Type", "application/json");
        return con;
    }

    private String readBody(HttpURLConnection con) throws IOException {
        int code = con.getResponseCode();
        InputStream rawStream = code >= 400 ? con.getErrorStream() : con.getInputStream();
        if ("gzip".equalsIgnoreCase(con.getContentEncoding()) && rawStream != null) {
            rawStream = new GZIPInputStream(rawStream);
        }
        if (rawStream == null) {
            throw new IOException("Leere Antwort (HTTP " + code + ")");
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(rawStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
        }
        if (code >= 400) {
            Log.w(TAG, "HTTP " + code + ": " + content);
            throw new LlmHttpException(code, "HTTP " + code);
        }
        return content.toString();
    }

    public static final class EventStream {
        public final BufferedReader reader;
        private final HttpURLConnection connection;

        EventStream(BufferedReader reader, HttpURLConnection connection) {
            this.reader = reader;
            this.connection = connection;
        }

        public void disconnect() {
            connection.disconnect();
        }
    }
}
