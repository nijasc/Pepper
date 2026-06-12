package com.buhlergroup.pepper.action.dynamicanim;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationGenerator {

    private static final String TAG = "DynAnim";
    private static final String MODEL = "gpt-5.5";
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_SECONDS = 30;
    private static final int GENERATION_TIMEOUT_MS = 120000;

    private final OpenAIService openAi = new OpenAIService(new ArrayList<>());

    public String generateValidated(Context context, String command) {
        return generateValidated(context, command, 0);
    }

    public String generateValidated(Context context, String command, int targetSeconds) {
        int seconds = Math.min(MAX_SECONDS, Math.max(0, targetSeconds));
        return generate(context, gestureSystemPrompt(seconds), "Movement request: " + command);
    }

    public String generateValidatedDance(Context context, String songName, int seconds) {
        int target = Math.min(MAX_SECONDS, Math.max(8, seconds));
        return generate(context, danceSystemPrompt(target),
                "Choreograph a full-body dance for this song: " + songName);
    }

    private String generate(Context context, String systemPrompt, String userBase) {
        openAi.setC(context);
        String lastValidationError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String qianim = stripFences(requestAnimation(systemPrompt, userBase, lastValidationError));
                String error = QianimValidator.validate(qianim);
                if (error == null) {
                    return QianimPostProcessor.ensureTangents(QianimLooper.expand(qianim));
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

    private String danceSystemPrompt(int seconds) {
        return "You generate a single rhythmic full-body Pepper robot DANCE in qianim 2.0 XML and output "
                + "ONLY the raw XML (no Markdown, no code fences, no explanation).\n\n"
                + jointRulesHeader()
                + repeatRules()
                + "- Target total duration: about " + seconds + " seconds (" + (seconds * 25) + " frames).\n"
                + "- Author ONE musical motif of 4-8 seconds (100-200 frames) and set repeatCycles so that "
                + "motif length times repeatCycles is close to the target duration.\n"
                + "- This is a DANCE: move several joints together (arms, head, hips) in a lively rhythm with "
                + "regularly spaced beats (a keyframe roughly every 8-15 frames).\n"
                + "- The motif must start and end on exactly the same pose (every moving joint has identical "
                + "values at frame 0 and at the last frame), so repetitions chain seamlessly.\n"
                + "- Keep that shared start/end pose close to a neutral stand so entering and leaving the dance "
                + "is smooth.\n"
                + jointRangesFooter();
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
                + "duration.\n"
                + "- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and "
                + "author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it "
                + "with two identical keys spanning the hold time, then return to neutral in the final second. "
                + "The last frame must be at the target duration.\n"
                + "- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n"
                + "- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n";
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
