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
        openAi.setC(context);
        String lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String qianim = stripFences(requestAnimation(command, lastError));
                String error = QianimValidator.validate(qianim);
                if (error == null) {
                    return qianim;
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

    private String requestAnimation(String command, String previousError) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt()));
        String userContent = "Movement request: " + command;
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

    private String systemPrompt() {
        return "You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML "
                + "(no Markdown, no code fences, no explanation).\n\n"
                + "Structure:\n"
                + "<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\">\n"
                + "  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n"
                + "    <Key value=\"FLOAT\" frame=\"INT\"/>\n"
                + "    ... more keys ...\n"
                + "  </ActuatorCurve>\n"
                + "  ... more curves ...\n"
                + "</Animation>\n\n"
                + "Rules:\n"
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

    private Map<String, String> message(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

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
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
        }
        int start = text.indexOf("<Animation");
        int end = text.lastIndexOf("</Animation>");
        if (start >= 0 && end >= 0) {
            text = text.substring(start, end + "</Animation>".length());
        }
        return text.trim();
    }
}
