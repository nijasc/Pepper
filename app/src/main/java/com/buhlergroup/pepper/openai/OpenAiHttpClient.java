package com.buhlergroup.pepper.openai;

import android.util.Log;

import androidx.annotation.Nullable;

import com.buhlergroup.pepper.BuildConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

final class OpenAiHttpClient {

    private static final String TAG = "OpenAIService";
    private static final String BASE_URL = "https://api.openai.com/v1";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenSupplier tokenSupplier;

    OpenAiHttpClient(TokenSupplier tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    String request(String path, @Nullable Map<String, Object> body, int readTimeoutMs)
            throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setConnectTimeout(8000);
        con.setReadTimeout(readTimeoutMs);
        con.setRequestProperty("Authorization", "Bearer " + tokenSupplier.token());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip");

        if (body != null) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            String json = objectMapper.writeValueAsString(body);
            if (BuildConfig.DEBUG) {
                body.remove("instructions");
                Log.i("OPENREQ", objectMapper.writeValueAsString(body));
            }

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        } else {
            con.setRequestMethod("GET");
        }

        int code = con.getResponseCode();
        InputStream rawStream = code >= 400 ? con.getErrorStream() : con.getInputStream();
        if ("gzip".equalsIgnoreCase(con.getContentEncoding()) && rawStream != null) {
            rawStream = new GZIPInputStream(rawStream);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(rawStream, StandardCharsets.UTF_8)
        );

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "OpenAI Response: " + content);
        }
        return content.toString();
    }

    EventStream openEventStream(Map<String, Object> body) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(BASE_URL + "/responses").openConnection();
        con.setConnectTimeout(8000);
        con.setReadTimeout(30000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + tokenSupplier.token());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "text/event-stream");

        try {
            try (OutputStream os = con.getOutputStream()) {
                os.write(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            if (code >= 400) {
                throw new IOException("Streaming request failed with HTTP " + code);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            return new EventStream(reader, con);
        } catch (IOException e) {
            con.disconnect();
            throw e;
        }
    }

    interface TokenSupplier {
        String token();
    }

    static final class EventStream {
        final BufferedReader reader;
        private final HttpURLConnection connection;

        EventStream(BufferedReader reader, HttpURLConnection connection) {
            this.reader = reader;
            this.connection = connection;
        }

        void disconnect() {
            connection.disconnect();
        }
    }
}
