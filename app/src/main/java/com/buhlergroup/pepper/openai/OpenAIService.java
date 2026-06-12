package com.buhlergroup.pepper.openai;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.util.IOUtils;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.openai.history.HistoryManager;
import com.buhlergroup.pepper.perception.EmotionReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenAIService {

    public static final String DEFAULT_MODEL = "gpt-5.4";
    private static final int MAX_OUTPUT_TOKENS = 600;
    private static final String URL = "https://api.openai.com/v1";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmotionReader emotionReader = new EmotionReader();
    private final List<Action> actions;
    private static String cachedToken;
    private Context c;

    private static final Pattern LANG_TAG =
            Pattern.compile("\\[\\[\\s*lang\\s*:\\s*([A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?)\\s*\\]\\]");
    private String lastLanguageTag;

    public OpenAIService(List<Action> actions) {
        this.actions = actions;
    }

    public String getResponse(HistoryManager historyManager, QiContext context) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", DEFAULT_MODEL);
        body.put("input", historyManager.toInput());
        body.put("instructions", formDefaultSystemPrompt(context));
        body.put("max_output_tokens", MAX_OUTPUT_TOKENS);
        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        body.put("reasoning", reasoning);

        long started = System.currentTimeMillis();
        try {
            String res = sendOpenAiRequest("/responses", body);
            res = parseOutput(res);
            res = extractLanguageTag(res);
            Log.i("LATENCY", "getResponse took " + (System.currentTimeMillis() - started) + "ms");
            return sanitizeResponse(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Etwas ist unerwartet schief gelaufen.";
    }

    private String extractLanguageTag(String text) {
        if (text == null) {
            lastLanguageTag = null;
            return null;
        }
        Matcher matcher = LANG_TAG.matcher(text);
        lastLanguageTag = matcher.find() ? matcher.group(1) : null;
        return LANG_TAG.matcher(text).replaceAll("").trim();
    }

    public String lastLanguageTag() {
        return lastLanguageTag;
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

        prompt.append("\n## Internal Language Marker\n")
                .append("Begin every reply with a machine marker of the exact form [[lang:CODE]] where CODE is the ")
                .append("ISO 639-1 code of the language you are replying in (for example [[lang:de]], [[lang:en]], ")
                .append("[[lang:ja]]). Write the marker exactly once at the very start with nothing before it, then ")
                .append("your normal spoken reply. The marker is removed automatically before your reply is spoken ")
                .append("and must never appear inside the reply or influence its wording.\n");

        String moodHint = emotionReader.moodHintForPrompt(context);
        if (moodHint != null) {
            prompt.append("\n## Visitor Emotion\n")
                    .append("The person in front of you right now ").append(moodHint).append(". ")
                    .append("You may occasionally and subtly acknowledge this if it fits the ")
                    .append("conversation, but never mention it in every reply and never force it.\n");
        }

        appendRaffleHint(context, prompt);

        return prompt.toString();
    }

    private void appendRaffleHint(Context context, StringBuilder prompt) {
        try {
            RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();
            if (raffle == null) {
                return;
            }
            if (raffle.status == RaffleStatus.ACTIVE) {
                String end = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                        .format(new Date(raffle.endDate));
                prompt.append("\n## Active Raffle\n")
                        .append("There is currently an active raffle the visitor can join: \"")
                        .append(raffle.title).append("\". ");
                if (raffle.description != null && !raffle.description.isEmpty()) {
                    prompt.append(raffle.description).append(' ');
                }
                prompt.append("It ends on ").append(end).append(". ")
                        .append("To take part the visitor gives their name and a valid e-mail address");
                if (raffle.requiresPhone) {
                    prompt.append(", and a phone number");
                }
                if (raffle.requiresSelfie) {
                    prompt.append(", and takes a selfie with you");
                }
                prompt.append(". Proactively and naturally invite the visitor to take part when it fits ")
                        .append("the conversation, but do not repeat it in every single reply.\n");
            } else if (raffle.status == RaffleStatus.ENDED) {
                prompt.append("\n## Raffle Ended\n")
                        .append("If the visitor asks about the raffle, tell them it has unfortunately ")
                        .append("already ended. Do not invite them to take part.\n");
            }
        } catch (Exception ignored) {
        }
    }

    public String sendOpenAiRequest(String path, @Nullable Map<String, Object> body) throws IOException {
        URL url = new URL(URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setConnectTimeout(8000);
        con.setReadTimeout(20000);
        con.setRequestProperty("Authorization", "Bearer " + getAuthToken(c));
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip");

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
