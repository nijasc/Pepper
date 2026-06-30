package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.action.dynamicanim.QianimValidator;
import com.buhlergroup.pepper.action.dynamicanim.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class GestureBooster {

    private static final String TAG = "GestureBooster";

    private static final double SPEED = 0.62;
    private static final double AMP_UPPER = 1.45;
    private static final double AMP_LOWER = 1.0;
    private static final int MAX_FRAME = 750;

    private GestureBooster() {
    }

    static String boost(Context context, int rawRes) {
        try {
            Document doc = XmlUtils.parse(readRaw(context, rawRes));
            NodeList curves = doc.getElementsByTagName("ActuatorCurve");
            for (int i = 0; i < curves.getLength(); i++) {
                boostCurve((Element) curves.item(i));
            }
            return XmlUtils.serialize(doc);
        } catch (Exception e) {
            Log.w(TAG, "Boost fehlgeschlagen, nutze Rohanimation: " + e.getMessage());
            return null;
        }
    }

    private static void boostCurve(Element curve) {
        String actuator = curve.getAttribute("actuator");
        float[] limit = QianimValidator.limitsFor(actuator);
        double amp = isLowerBody(actuator) ? AMP_LOWER : AMP_UPPER;

        NodeList keys = curve.getElementsByTagName("Key");
        Double baseline = null;
        int prevFrame = -1;
        for (int k = 0; k < keys.getLength(); k++) {
            Element key = (Element) keys.item(k);

            double value = parse(key.getAttribute("value"));
            if (baseline == null) {
                baseline = value;
            }
            double boosted = clamp(baseline + (value - baseline) * amp, limit);
            key.setAttribute("value", fmt(boosted));

            int frame = (int) Math.round(parse(key.getAttribute("frame")) * SPEED);
            frame = Math.max(0, Math.min(MAX_FRAME, frame));
            if (frame <= prevFrame) {
                frame = Math.min(MAX_FRAME, prevFrame + 1);
            }
            prevFrame = frame;
            key.setAttribute("frame", Integer.toString(frame));

            scaleTangents(key, amp);
        }
    }

    private static void scaleTangents(Element key, double amp) {
        NodeList tangents = key.getElementsByTagName("Tangent");
        for (int t = 0; t < tangents.getLength(); t++) {
            Node node = tangents.item(t);
            if (!(node instanceof Element)) {
                continue;
            }
            Element tangent = (Element) node;
            if (tangent.hasAttribute("abscissaParam")) {
                tangent.setAttribute("abscissaParam",
                        fmt(parse(tangent.getAttribute("abscissaParam")) * SPEED));
            }
            if (tangent.hasAttribute("ordinateParam")) {
                tangent.setAttribute("ordinateParam",
                        fmt(parse(tangent.getAttribute("ordinateParam")) * amp));
            }
        }
    }

    private static boolean isLowerBody(String actuator) {
        return actuator.startsWith("Hip") || actuator.startsWith("Knee");
    }

    private static double clamp(double value, float[] limit) {
        if (limit == null) {
            return value;
        }
        return Math.max(limit[0], Math.min(limit[1], value));
    }

    private static double parse(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private static String readRaw(Context context, int rawRes) throws Exception {
        try (InputStream in = context.getResources().openRawResource(rawRes)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
