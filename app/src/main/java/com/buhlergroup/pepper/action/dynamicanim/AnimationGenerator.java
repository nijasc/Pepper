package com.buhlergroup.pepper.action.dynamicanim;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.openai.ModelSelector;
import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnimationGenerator {

    private static final String TAG = "DynAnim";
    private static final String MODEL =
            ModelSelector.modelFor(ModelSelector.ModelTask.GENERATION);
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_SECONDS = 30;
    private static final int GENERATION_TIMEOUT_MS = 120000;
    private static final int DANCE_TIMEOUT_MS = 180000;
    private static final int RESEARCH_TIMEOUT_MS = 90000;

    public enum Stage {
        SEARCH,
        ANALYZE,
        CHOREOGRAPH,
        AUDIO
    }

    public interface ProgressListener {
        void onStage(Stage stage);
    }

    private final OpenAIService openAi = new OpenAIService(new ArrayList<>());

    public String generateValidated(Context context, String command) {
        return generateValidated(context, command, 0);
    }

    public String generateValidated(Context context, String command, int targetSeconds) {
        int seconds = Math.min(MAX_SECONDS, Math.max(0, targetSeconds));
        return generate(context, gestureSystemPrompt(seconds), "Movement request: " + command, false);
    }

    public String generateValidatedDance(Context context, String songName, int seconds) {
        return generateValidatedDance(context, songName, seconds, null, null);
    }

    public String generateValidatedDance(Context context, String songName, int seconds, String editNote) {
        return generateValidatedDance(context, songName, seconds, editNote, null);
    }

    public String generateValidatedDance(Context context, String songName, int seconds,
                                         String editNote, String mood) {
        return generateValidatedDance(context, songName, seconds, editNote, mood, null);
    }

    public String generateValidatedDance(Context context, String songName, int seconds,
                                         String editNote, String mood, ProgressListener progress) {
        int target = Math.min(MAX_SECONDS, Math.max(8, seconds));
        openAi.setC(context);
        if (progress != null) {
            progress.onStage(Stage.ANALYZE);
        }
        SongResearch research = researchSong(context, songName);
        String userMessage = "Song: " + songName;
        if (research != null) {
            userMessage += "\n\nResearched facts about this song (base the choreography on them):\n"
                    + research.brief();
        }
        if (editNote != null && !editNote.trim().isEmpty()) {
            userMessage += "\n\nApply this specific change to the choreography: " + editNote.trim();
        }
        String effectiveMood = research != null && research.mood != null ? research.mood : mood;
        if (progress != null) {
            progress.onStage(Stage.CHOREOGRAPH);
        }
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(message("system",
                        danceCompactPrompt() + researchCoupling() + moodGuidance(effectiveMood)));
                messages.add(message("user", userMessage));

                Map<String, Object> body = new HashMap<>();
                body.put("model", MODEL);
                body.put("messages", messages);
                body.put("reasoning_effort", "high");

                String response = openAi.sendOpenAiRequest("/chat/completions", body, DANCE_TIMEOUT_MS);
                String content = new JSONObject(response)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                JSONObject plan = new JSONObject(extractJson(content));
                String xml = buildDanceXml(plan, target);
                Document doc = XmlUtils.parse(xml);
                return postProcess(doc, xml, true);
            } catch (Exception e) {
                Log.w(TAG, "Compact dance attempt " + attempt + " failed: " + e.getMessage());
            }
        }
        return null;
    }

    public static final class SongPlan {
        public final String query;
        public final int startSeconds;
        public final String mood;

        public SongPlan(String query, int startSeconds, String mood) {
            this.query = query;
            this.startSeconds = startSeconds;
            this.mood = mood;
        }
    }

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

            String response = openAi.sendOpenAiRequest("/chat/completions", body, 15000);
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

    public static final class SongResearch {
        public final String genre;
        public final String tempo;
        public final String signature;
        public final String mood;
        public final String era;
        public final String signatureMoves;
        public final String structure;

        SongResearch(String genre, String tempo, String signature, String mood,
                     String era, String signatureMoves, String structure) {
            this.genre = genre;
            this.tempo = tempo;
            this.signature = signature;
            this.mood = mood;
            this.era = era;
            this.signatureMoves = signatureMoves;
            this.structure = structure;
        }

        String brief() {
            StringBuilder sb = new StringBuilder();
            line(sb, "Genre", genre);
            line(sb, "Tempo/BPM", tempo);
            line(sb, "Time signature", signature);
            line(sb, "Mood", mood);
            line(sb, "Era", era);
            line(sb, "Signature/iconic dance moves", signatureMoves);
            line(sb, "Song structure / hook timing", structure);
            return sb.toString();
        }

        private static void line(StringBuilder sb, String label, String value) {
            if (value != null && !value.trim().isEmpty()) {
                sb.append("- ").append(label).append(": ").append(value.trim()).append('\n');
            }
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

            String response = openAi.sendOpenAiRequest("/chat/completions", body, RESEARCH_TIMEOUT_MS);
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

    private String researchCoupling() {
        return "\n\nUSE THE RESEARCHED FACTS (if provided):\n"
                + "- Tempo: faster songs use a smaller frameStep (about 12), calmer songs a larger one "
                + "(about 18-20).\n"
                + "- Mood and genre: match the movement character - gentle and flowing for calm songs, "
                + "upbeat and punchy (but still smooth and safe) for lively ones.\n"
                + "- Signature/iconic moves: if any are listed, recreate them faithfully and in order so the "
                + "dance is instantly recognisable; otherwise invent an original groove that fits the genre "
                + "and era.\n"
                + "- Structure/hook: build the motif so its biggest, clearest poses land on the hook or chorus.\n";
    }

    public static final class DanceEdit {
        public final Integer startSeconds;
        public final String choreography;

        public DanceEdit(Integer startSeconds, String choreography) {
            this.startSeconds = startSeconds;
            this.choreography = choreography;
        }
    }

    public DanceEdit interpretEdit(Context context, String songName, long currentStartMs, String instruction) {
        openAi.setC(context);
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message("system",
                    "You edit a saved Pepper robot dance from a spoken instruction. The dance plays a roughly "
                            + "30-second music preview (currently starting " + (currentStartMs / 1000)
                            + " seconds in) with a generated choreography. Reply with ONLY a JSON object "
                            + "{\"startSeconds\":N,\"choreography\":\"...\"}. Set startSeconds to the new start "
                            + "offset in whole seconds (0-29) if the instruction asks to change WHERE the music "
                            + "starts, otherwise null. Set choreography to a short English instruction describing "
                            + "the requested change to the DANCE MOVES (for example 'smoother arm movements', "
                            + "'bigger gestures', 'add more head movement') if the instruction asks to change the "
                            + "moves, otherwise null. No prose, only the JSON."));
            messages.add(message("user", instruction));

            Map<String, Object> body = new HashMap<>();
            body.put("model", ModelSelector.FAST);
            body.put("messages", messages);

            String response = openAi.sendOpenAiRequest("/chat/completions", body, 15000);
            String content = new JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            JSONObject obj = new JSONObject(extractJson(content));
            Integer startSeconds = obj.isNull("startSeconds") ? null : obj.optInt("startSeconds");
            String choreography = obj.isNull("choreography") ? null : obj.optString("choreography", "").trim();
            if (choreography != null && choreography.isEmpty()) {
                choreography = null;
            }
            Log.i(TAG, "Edit interpreted: start=" + startSeconds + " choreo=" + choreography);
            return new DanceEdit(startSeconds, choreography);
        } catch (Exception e) {
            Log.w(TAG, "Edit interpretation failed: " + e.getMessage());
            return new DanceEdit(null, null);
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
                startSeconds = Integer.parseInt(matcher.group(1));
            }
        }
        if (query == null || query.trim().isEmpty()) {
            query = fallbackQuery;
        }
        return new SongPlan(query, Math.max(0, Math.min(15, startSeconds)), mood);
    }

    private String generate(Context context, String systemPrompt, String userBase,
                            boolean normalizeBody) {
        openAi.setC(context);
        String lastValidationError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String qianim = stripFences(requestAnimation(systemPrompt, userBase, lastValidationError));
                Document doc;
                String error;
                try {
                    doc = XmlUtils.parse(qianim);
                    error = QianimValidator.validate(doc);
                } catch (Exception e) {
                    doc = null;
                    error = "not parseable: " + e.getMessage();
                }
                if (error == null) {
                    return postProcess(doc, qianim, normalizeBody);
                }
                Log.w(TAG, "Attempt " + attempt + " invalid: " + error);
                lastValidationError = error;
            } catch (Exception e) {
                Log.w(TAG, "Attempt " + attempt + " failed (client/network, retrying fresh): "
                        + e.getMessage());
                lastValidationError = null;
            }
        }
        return null;
    }

    private String postProcess(Document doc, String original, boolean normalizeBody) {
        try {
            QianimLooper.expand(doc);
            if (normalizeBody) {
                QianimPostProcessor.normalizeBodyJoints(doc);
            }
            QianimPostProcessor.ensureTangents(doc);
            return XmlUtils.serialize(doc);
        } catch (Exception e) {
            Log.w(TAG, "Post-processing failed, using validated qianim: " + e.getMessage());
            return original;
        }
    }

    private String requestAnimation(String systemPrompt, String userBase, String previousError)
            throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        String userContent = userBase;
        if (previousError != null) {
            userContent += "\n\nYour previous attempt was rejected: " + previousError
                    + "\nFix it and return only the corrected animation.";
        }
        messages.add(message("user", userContent));

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("reasoning_effort", "low");

        String response = openAi.sendOpenAiRequest("/chat/completions", body, GENERATION_TIMEOUT_MS);
        return new JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    private String danceCompactPrompt() {
        return "You are an expert choreographer for the Pepper humanoid robot. Output ONLY a compact JSON "
                + "object describing one looping dance motif - no Markdown, no code fences, no prose.\n\n"
                + "FORMAT\n"
                + "Schema: {\"frameStep\":F,\"curves\":{\"JOINT\":[v0,v1,...],...}}\n"
                + "- The animation runs at 25 fps (so 25 frames = 1 second).\n"
                + "- Each number in a joint's array is a keyframe pose; consecutive keyframes are F frames "
                + "apart. F is an integer 12-20 (12 = quicker/punchier, 20 = slower/calmer).\n"
                + "- Give EVERY joint array the SAME length N (use 8-14; more keys = richer, smoother motion). "
                + "The poses are interpolated smoothly between keyframes.\n"
                + "- LOOP CONDITION: the first and last value of each array MUST be identical so the motif "
                + "loops seamlessly. The runtime tiles (repeatCycles) the motif to fill the song and then "
                + "returns Pepper to neutral - do NOT author the repeats or the final return yourself.\n"
                + "- Use at most 2 decimals. Values are radians (hands are 0..1, dimensionless).\n\n"
                + "JOINTS AND THEIR PHYSICAL MEANING (what each does, read from a few metres away)\n"
                + "- Shoulders carry the dance - they are the most visible from a distance:\n"
                + "  LShoulderPitch/RShoulderPitch [-2.08,2.08]: arm up/down (negative = arm raised forward/up, "
                + "positive = arm lowered). Use the full range for big, readable raises and lowerings.\n"
                + "  LShoulderRoll [0.01,1.56] / RShoulderRoll [-1.56,-0.01]: arm out to the side. They are "
                + "MIRRORED - to open both arms symmetrically, LShoulderRoll goes positive while RShoulderRoll "
                + "goes negative by the same amount.\n"
                + "- Elbows shape the arm: LElbowRoll [-1.56,-0.01] / RElbowRoll [0.01,1.56] bend the forearm "
                + "(also mirrored, opposite signs); LElbowYaw/RElbowYaw [-2.08,2.08] rotate it.\n"
                + "- Wrists/hands are accents: LWristYaw/RWristYaw [-1.82,1.82] turn the hand; LHand/RHand [0,1] "
                + "open (1) or close (0) the hand - use them for small expressive details.\n"
                + "- Head makes Pepper look alive: HeadYaw [-2.08,2.08] left/right, HeadPitch [-0.70,0.63] "
                + "up/down. Add gentle head motion on most cycles.\n"
                + "- Lower body is for a SUBTLE sway only - keep it small for balance and safety: "
                + "HipRoll [-0.15,0.15], HipPitch [-0.20,0.20], KneePitch [-0.10,0.10]. Never exceed these.\n\n"
                + "SIGNATURE MOVES\n"
                + "If the song has a famous routine (Macarena = arms out, palms up, hands to opposite "
                + "shoulders, hands behind head, hands to hips; Watch Me = the whip and the nae-nae; "
                + "Y.M.C.A. = spell Y M C A with the arms; Gangnam Style = overhead lasso arm), recreate its "
                + "characteristic moves faithfully and IN ORDER with the arms and head so it is instantly "
                + "recognisable. Otherwise invent an original groove that genuinely fits the song.\n\n"
                + "DO\n"
                + "- Make it a REAL choreography: every keyframe is a distinct, deliberate pose that flows "
                + "into the next; let the motif EVOLVE across the cycle instead of repeating one beat.\n"
                + "- Use big, sweeping, clearly readable arm arcs; contrast the left and right arms.\n"
                + "- Always move both shoulders and both elbows; add head, and some wrists/hands and a little "
                + "hip. Include 8-12 joints total.\n"
                + "DON'T\n"
                + "- Don't jump large angles between adjacent keyframes (keep motion smooth, no jerks).\n"
                + "- Don't exceed any range above. Don't use joints not listed. Don't author repeats or the "
                + "return to neutral. Don't output anything except the JSON object.";
    }

    private String moodGuidance(String mood) {
        if ("calm".equalsIgnoreCase(mood)) {
            return "\n- This song is calm and gentle: keep the dance slow, soft and flowing; use the "
                    + "larger frameStep values (about 16 to 20) and graceful, minimal motion.";
        }
        if ("lively".equalsIgnoreCase(mood)) {
            return "\n- This song is lively: keep the dance upbeat but still smooth and controlled; "
                    + "use the middle of the frameStep range (about 12 to 16).";
        }
        return "";
    }

    private double clamp(double value, float[] limit) {
        if (limit == null) {
            return value;
        }
        return Math.max(limit[0], Math.min(limit[1], value));
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

    private String buildDanceXml(JSONObject plan, int targetSeconds) throws Exception {
        int frameStep = Math.max(10, Math.min(25, plan.optInt("frameStep", 16)));
        JSONObject curves = plan.getJSONObject("curves");
        JSONArray names = curves.names();

        List<String> joints = new ArrayList<>();
        int n = 0;
        for (int i = 0; names != null && i < names.length(); i++) {
            String joint = names.getString(i);
            if (QianimValidator.limitsFor(joint) == null) {
                continue;
            }
            JSONArray arr = curves.optJSONArray(joint);
            if (arr == null || arr.length() < 2) {
                continue;
            }
            joints.add(joint);
            n = Math.max(n, arr.length());
        }
        if (joints.isEmpty()) {
            throw new Exception("no usable curves");
        }
        n = Math.max(2, Math.min(n, 16));

        int motifFrames = (n - 1) * frameStep;
        int maxCycles = Math.max(1, (MAX_SECONDS * 25 - 25) / motifFrames);
        int repeatCycles = Math.max(1, Math.min(maxCycles,
                Math.round((float) (targetSeconds * 25) / motifFrames)));

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<Animation typeVersion=\"2.0\" xmlns:editor=\"")
                .append("http://www.aldebaran.com/animation/editor\" repeatCycles=\"")
                .append(repeatCycles).append("\">\n");
        for (String joint : joints) {
            JSONArray arr = curves.getJSONArray(joint);
            float[] limit = QianimValidator.limitsFor(joint);
            String unit = "LHand".equals(joint) || "RHand".equals(joint) ? "dimensionless" : "radian";
            sb.append("  <ActuatorCurve fps=\"25\" actuator=\"").append(joint)
                    .append("\" mute=\"false\" unit=\"").append(unit).append("\">\n");
            double first = clamp(arr.optDouble(0, 0), limit);
            for (int i = 0; i < n; i++) {
                double value;
                if (i == n - 1) {
                    value = first;
                } else if (i < arr.length()) {
                    value = clamp(arr.optDouble(i, first), limit);
                } else {
                    value = clamp(arr.optDouble(arr.length() - 1, first), limit);
                }
                sb.append("    <Key value=\"").append(String.format(Locale.US, "%.4f", value))
                        .append("\" frame=\"").append(i * frameStep).append("\"/>\n");
            }
            sb.append("  </ActuatorCurve>\n");
        }
        sb.append("</Animation>");
        return sb.toString();
    }

    private String gestureSystemPrompt(int targetSeconds) {
        String duration = targetSeconds > 0
                ? "- Target total duration: about " + targetSeconds + " seconds ("
                + (targetSeconds * 25) + " frames). Honour it exactly using one of the two modes above.\n"
                : "- No duration was requested: pick a natural duration of 1-4 seconds with repeatCycles=\"1\".\n";
        return "You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML "
                + "(no Markdown, no code fences, no explanation).\n\n"
                + jointRulesHeader()
                + repeatRules()
                + duration
                + "- Only include curves for the joints that must move for the requested gesture.\n"
                + "- Space keyframes a few frames apart for smooth motion; do not jump large angles between "
                + "adjacent frames.\n"
                + jointRangesFooter();
    }

    private String repeatRules() {
        return "- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles "
                + "your keyframes K times back to back before playing, so K cycles cost you no extra output.\n"
                + "- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author "
                + "exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 "
                + "and at the last frame, and set repeatCycles so cycle length times K matches the target "
                + "duration. The shared boundary pose must be a natural point WITHIN the ongoing motion "
                + "(e.g. mid-swing), NOT a neutral stand - the cycles must flow into each other without any "
                + "visible reset. After the final cycle the runtime automatically brings Pepper back to a "
                + "neutral stand; do not author that return yourself.\n"
                + "- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and "
                + "author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it "
                + "with two identical keys spanning the hold time, then return to neutral in the final second. "
                + "The last frame must be at the target duration.\n"
                + "- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n";
    }

    private String jointRulesHeader() {
        return "Structure (the first line must be exactly the XML declaration shown):\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" "
                + "repeatCycles=\"K\">\n"
                + "  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n"
                + "    <Key value=\"FLOAT\" frame=\"INT\"/>\n"
                + "  </ActuatorCurve>\n"
                + "</Animation>\n\n"
                + "Rules:\n"
                + "- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n"
                + "- fps is always 25. Frames are integers starting at 0.\n";
    }

    private String jointRangesFooter() {
        return "- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n"
                + "- Values MUST stay within these safe ranges (radians, hands dimensionless):\n"
                + "  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n"
                + "  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n"
                + "  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n"
                + "  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n"
                + "  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n"
                + "  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n"
                + "  LHand [0,1], RHand [0,1],\n"
                + "  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n"
                + "- Use only those joint names. No other actuators.";
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    private String stripFences(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline >= 0) {
                text = text.substring(firstNewline + 1);
            }
            int lastFence = text.lastIndexOf("```");
            if (lastFence >= 0) {
                text = text.substring(0, lastFence);
            }
        }
        text = text.trim();
        int start = text.indexOf("<Animation");
        int end = text.lastIndexOf("</Animation>");
        if (start < 0 || end < 0) {
            return text;
        }
        String body = text.substring(start, end + "</Animation>".length());
        return XML_PROLOG + "\n" + body;
    }
}
