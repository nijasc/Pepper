package com.buhlergroup.pepper.action.dynamicanim;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class QianimLooper {

    private static final String TAG = "DynAnim";
    private static final String REPEAT_ATTR = "repeatCycles";
    private static final int MAX_TOTAL_FRAMES = 750;
    private static final int RETURN_FRAMES = 25;

    private static final Map<String, Float> NEUTRAL = new HashMap<>();

    static {
        NEUTRAL.put("HeadYaw", 0f);
        NEUTRAL.put("HeadPitch", -0.2f);
        NEUTRAL.put("LShoulderPitch", 1.4f);
        NEUTRAL.put("RShoulderPitch", 1.4f);
        NEUTRAL.put("LShoulderRoll", 0.12f);
        NEUTRAL.put("RShoulderRoll", -0.12f);
        NEUTRAL.put("LElbowYaw", -1.2f);
        NEUTRAL.put("RElbowYaw", 1.2f);
        NEUTRAL.put("LElbowRoll", -0.5f);
        NEUTRAL.put("RElbowRoll", 0.5f);
        NEUTRAL.put("LWristYaw", 0f);
        NEUTRAL.put("RWristYaw", 0f);
        NEUTRAL.put("LHand", 0.6f);
        NEUTRAL.put("RHand", 0.6f);
        NEUTRAL.put("HipRoll", 0f);
        NEUTRAL.put("HipPitch", -0.03f);
        NEUTRAL.put("KneePitch", 0f);
    }

    private QianimLooper() {
    }

    public static void expand(Document doc) {
        Element root = doc.getDocumentElement();
        int cycles = parseInt(root.getAttribute(REPEAT_ATTR), 1);
        root.removeAttribute(REPEAT_ATTR);

        if (cycles <= 1) {
            return;
        }

        int cycleFrames = maxFrame(doc);
        if (cycleFrames <= 0) {
            return;
        }
        int maxCycles = Math.max(1, (MAX_TOTAL_FRAMES - RETURN_FRAMES) / cycleFrames);
        cycles = Math.min(cycles, maxCycles);

        int lastCycleEnd = cycles * cycleFrames;
        NodeList curves = doc.getElementsByTagName("ActuatorCurve");
        for (int c = 0; c < curves.getLength(); c++) {
            Element curve = (Element) curves.item(c);
            List<Element> originals = keysOf(curve);
            Set<Integer> usedFrames = new HashSet<>();
            for (Element key : originals) {
                usedFrames.add(frameOf(key));
            }
            for (int rep = 1; rep < cycles; rep++) {
                int offset = rep * cycleFrames;
                for (Element key : originals) {
                    int newFrame = frameOf(key) + offset;
                    if (newFrame > lastCycleEnd || usedFrames.contains(newFrame)) {
                        continue;
                    }
                    Element clone = (Element) key.cloneNode(true);
                    clone.setAttribute("frame", String.valueOf(newFrame));
                    curve.appendChild(clone);
                    usedFrames.add(newFrame);
                }
            }
            appendNeutralReturn(doc, curve, lastCycleEnd + RETURN_FRAMES);
        }
        Log.i(TAG, "Expanded animation to " + cycles + " cycles of " + cycleFrames
                + " frames plus neutral return");
    }

    private static void appendNeutralReturn(org.w3c.dom.Document doc, Element curve, int frame) {
        Float neutral = NEUTRAL.get(curve.getAttribute("actuator"));
        if (neutral == null) {
            return;
        }
        Element key = doc.createElement("Key");
        key.setAttribute("value", String.format(Locale.US, "%.4f", neutral));
        key.setAttribute("frame", String.valueOf(frame));
        curve.appendChild(key);
    }

    private static List<Element> keysOf(Element curve) {
        List<Element> keys = new ArrayList<>();
        NodeList children = curve.getElementsByTagName("Key");
        for (int i = 0; i < children.getLength(); i++) {
            keys.add((Element) children.item(i));
        }
        return keys;
    }

    private static int maxFrame(Document doc) {
        int max = 0;
        NodeList keys = doc.getElementsByTagName("Key");
        for (int i = 0; i < keys.getLength(); i++) {
            max = Math.max(max, frameOf((Element) keys.item(i)));
        }
        return max;
    }

    private static int frameOf(Element key) {
        return parseInt(key.getAttribute("frame"), 0);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
