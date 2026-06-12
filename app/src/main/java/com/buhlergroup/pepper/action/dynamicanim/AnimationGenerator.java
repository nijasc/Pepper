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
    private static final String MODEL = "gpt-4o";
    private static final int MAX_ATTEMPTS = 3;

    private final OpenAIService openAi = new OpenAIService(new ArrayList<>());

    public String generateValidated(Context context, String command) {
        return generate(context, gestureSystemPrompt(), "Movement request: " + command);
    }

    public String generateValidatedDance(Context context, String songName, int seconds) {
        return generate(context, danceSystemPrompt(seconds),
                "Choreograph a full-body dance for this song: " + songName);
    }

    private String generate(Context context, String systemPrompt, String userBase) {
        openAi.setC(context);
        String lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String qianim = stripFences(requestAnimation(systemPrompt, userBase, lastError));
                String error = QianimValidator.validate(qianim);
                if (error == null) {
                    return QianimPostProcessor.ensureTangents(qianim);
                }
                Log.w(TAG, "Attempt " + attempt + " invalid: " + error);
                lastError = error;
            } catch (Exception e) {
                Log.w(TAG, "Attempt " + attempt + " failed: " + e.getMessage());
                lastError = e.getMessage();
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

        String response = openAi.sendOpenAiRequest("/chat/completions", body);
        return new JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    private String danceSystemPrompt(int seconds) {
        int lastFrame = Math.min(750, Math.max(125, seconds * 25));
        return "You generate a single rhythmic full-body Pepper robot DANCE in qianim 2.0 XML and output "
                + "ONLY the raw XML (no Markdown, no code fences, no explanation).\n\n"
                + jointRulesHeader()
                + "- fps is always 25. Frames are integers starting at 0. Make the dance about " + seconds
                + " seconds long, so the last frame is around " + lastFrame + ".\n"
                + "- This is a DANCE: move several joints together (arms, head, hips) in a lively, repeating "
                + "rhythm with regularly spaced beats (a keyframe roughly every 8-15 frames).\n"
                + "- Keep a steady beat: reuse a short motif and repeat/vary it across the whole duration.\n"
                + "- Start and end near a neutral standing pose so the transition in and out is smooth.\n"
                + jointRangesFooter();
    }

    private String gestureSystemPrompt() {
        return "You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML "
                + "(no Markdown, no code fences, no explanation).\n\n"
                + "Structure (the first line must be exactly the XML declaration shown):\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\">\n"
                + "  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n"
                + "    <Key value=\"FLOAT\" frame=\"INT\"/>\n"
                + "    ... more keys ...\n"
                + "  </ActuatorCurve>\n"
                + "  ... more curves ...\n"
                + "</Animation>\n\n"
                + "Rules:\n"
                + "- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n"
                + "- fps is always 25. Frames are integers starting at 0. Keep it short: last frame <= 125 (5 seconds).\n"
                + "- Only include curves for the joints that must move for the requested gesture.\n"
                + "- Every moving joint needs at least a start key at frame 0 and a key returning near the start "
                + "pose at the end, so the motion is smooth and Pepper does not stay frozen in an extreme pose.\n"
                + "- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n"
                + "- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n"
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

    private String jointRulesHeader() {
        return "Structure (the first line must be exactly the XML declaration shown):\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\">\n"
                + "  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n"
                + "    <Key value=\"FLOAT\" frame=\"INT\"/>\n"
                + "  </ActuatorCurve>\n"
                + "</Animation>\n\n"
                + "Rules:\n"
                + "- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n";
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
