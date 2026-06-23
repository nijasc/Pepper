package com.buhlergroup.pepper.action.dynamicanim;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.openai.ModelSelector;
import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SongResearcher {

    private static final String TAG = "DynAnim";
    private static final String MODEL =
            ModelSelector.modelFor(ModelSelector.ModelTask.GENERATION);
    private static final int RESEARCH_TIMEOUT_MS = 90000;

    private final OpenAIService openAi = OpenAIService.shared();

    public SongPlan planSong(Context context, String utterance) {
        openAi.setC(context);
        String fallbackQuery = utterance == null ? "" : utterance.trim();
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message("system",
                    "The user asked a Pepper robot to dance. From their utterance work out which song to "
                            + "play and reply with ONLY a JSON object "
                            + "{\"query\":\"...\",\"startSeconds\":N,\"mood\":\"calm|lively\"}. "
                            + "\"query\" is a clean music-search string of artist and title (for example "
                            + "\"Los del Rio Macarena\" or \"Silento Watch Me Whip Nae Nae\") for the song they "
                            + "want - strip greetings, filler words and commands, and translate a described song "
                            + "into its real title. If they name no specific song, choose a famous, upbeat, "
                            + "danceable song yourself. The audio will be the roughly 30-second iTunes preview of "
                            + "that song (which usually starts near the chorus); \"startSeconds\" is an integer "
                            + "from 0 to 15 for how many seconds into that preview the song's signature danceable "
                            + "hook lands, or 0 if unsure. \"mood\" is \"calm\" for slow, gentle or romantic "
                            + "songs and \"lively\" for upbeat or energetic songs. No prose, only the JSON."));
            messages.add(message("user", fallbackQuery));

            Map<String, Object> body = new HashMap<>();
            body.put("model", ModelSelector.FAST);
            body.put("messages", messages);

            String response = openAi.chat(com.buhlergroup.pepper.openai.ModelSelector.ModelTask.CLASSIFICATION, body, 15000);
            String content = new JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            SongPlan plan = parseSongPlan(content, fallbackQuery);
            Log.i(TAG, "Planned dance: query='" + plan.query + "' start=" + plan.startSeconds + "s");
            return plan;
        } catch (Exception e) {
            Log.w(TAG, "Song planning failed, using raw query: " + e.getMessage());
            return new SongPlan(fallbackQuery, 0, "lively");
        }
    }

    public SongResearch researchSong(Context context, String songName) {
        if (songName == null || songName.trim().isEmpty()) {
            return null;
        }
        openAi.setC(context);
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message("system",
                    "You are a music and choreography researcher. For the given song, reply with ONLY a JSON "
                            + "object with facts useful for choreographing a robot dance: "
                            + "{\"genre\":\"...\",\"tempo\":\"slow|medium|fast plus an approximate BPM\","
                            + "\"timeSignature\":\"e.g. 4/4\",\"mood\":\"calm|lively\",\"era\":\"decade or year\","
                            + "\"signatureMoves\":\"the song's iconic or characteristic dance moves in order, or "
                            + "'none' if it has no famous routine\",\"structure\":\"where the danceable hook/chorus "
                            + "sits and how the energy builds\"}. Be accurate; if unsure of a field, give your best "
                            + "estimate from the genre. No prose, only the JSON."));
            messages.add(message("user", "Song: " + songName.trim()));

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("messages", messages);
            body.put("reasoning_effort", "high");

            String response = openAi.chat(com.buhlergroup.pepper.openai.ModelSelector.ModelTask.GENERATION, body, RESEARCH_TIMEOUT_MS);
            String content = new JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            JSONObject obj = new JSONObject(extractJson(content));
            String mood = obj.optString("mood", "").trim().toLowerCase(Locale.ROOT);
            if (!mood.equals("calm") && !mood.equals("lively")) {
                mood = null;
            }
            SongResearch research = new SongResearch(
                    obj.optString("genre", "").trim(),
                    obj.optString("tempo", "").trim(),
                    obj.optString("timeSignature", "").trim(),
                    mood,
                    obj.optString("era", "").trim(),
                    obj.optString("signatureMoves", "").trim(),
                    obj.optString("structure", "").trim());
            Log.i(TAG, "Song research '" + songName + "': genre=" + research.genre
                    + " tempo=" + research.tempo + " mood=" + research.mood);
            return research;
        } catch (Exception e) {
            Log.w(TAG, "Song research failed, proceeding without: " + e.getMessage());
            return null;
        }
    }

    private SongPlan parseSongPlan(String content, String fallbackQuery) {
        String query = fallbackQuery;
        int startSeconds = 0;
        String mood = "lively";
        try {
            String json = content.substring(content.indexOf('{'), content.lastIndexOf('}') + 1);
            JSONObject obj = new JSONObject(json);
            String parsed = obj.optString("query", "").trim();
            if (!parsed.isEmpty()) {
                query = parsed;
            }
            startSeconds = obj.optInt("startSeconds", 0);
            String parsedMood = obj.optString("mood", "").trim().toLowerCase(Locale.ROOT);
            if (parsedMood.equals("calm") || parsedMood.equals("lively")) {
                mood = parsedMood;
            }
        } catch (Exception e) {
            Matcher matcher = Pattern.compile("startSeconds\"?\\s*[:=]\\s*(\\d{1,3})").matcher(content);
            if (matcher.find()) {
                startSeconds = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
            }
        }
        if (query == null || query.trim().isEmpty()) {
            query = fallbackQuery;
        }
        return new SongPlan(query, Math.max(0, Math.min(15, startSeconds)), mood);
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            return "{}";
        }
        return content.substring(start, end + 1);
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }
}
