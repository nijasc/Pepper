package com.buhlergroup.pepper.action.dynamicanim;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public final class QianimPostProcessor {

    private static final String EDITOR_NS = "http://www.aldebaran.com/animation/editor";
    private static final float SAFETY_MARGIN = 0.02f;

    private QianimPostProcessor() {
    }

    public static void ensureTangents(Document doc) {
        Element root = doc.getDocumentElement();
        if (root != null && !root.hasAttribute("xmlns:editor")) {
            root.setAttribute("xmlns:editor", EDITOR_NS);
        }

        NodeList curves = doc.getElementsByTagName("ActuatorCurve");
        for (int i = 0; i < curves.getLength(); i++) {
            rebuildCurve(doc, (Element) curves.item(i));
        }
    }

    private static void rebuildCurve(Document doc, Element curve) {
        List<Element> rawKeys = new ArrayList<>();
        NodeList nodes = curve.getElementsByTagName("Key");
        for (int i = 0; i < nodes.getLength(); i++) {
            rawKeys.add((Element) nodes.item(i));
        }

        TreeMap<Integer, Element> byFrame = new TreeMap<>();
        for (Element key : rawKeys) {
            int frame = parseInt(key.getAttribute("frame"), -1);
            if (frame < 0 || byFrame.containsKey(frame)) {
                curve.removeChild(key);
                continue;
            }
            byFrame.put(frame, key);
        }

        float[] limits = QianimValidator.limitsFor(curve.getAttribute("actuator"));
        List<Integer> frames = new ArrayList<>(byFrame.keySet());
        for (int i = 0; i < frames.size(); i++) {
            Element key = byFrame.get(frames.get(i));
            clampValue(key, limits);
            removeTangents(key);
            if (i > 0) {
                float abscissa = -(frames.get(i) - frames.get(i - 1)) / 3f;
                key.appendChild(makeTangent(doc, "left", abscissa));
            }
            if (i < frames.size() - 1) {
                float abscissa = (frames.get(i + 1) - frames.get(i)) / 3f;
                key.appendChild(makeTangent(doc, "right", abscissa));
            }
            curve.removeChild(key);
            curve.appendChild(key);
        }
    }

    private static void clampValue(Element key, float[] limits) {
        if (limits == null) {
            return;
        }
        Float value = parseFloat(key.getAttribute("value"));
        if (value == null) {
            return;
        }
        float min = limits[0] + SAFETY_MARGIN;
        float max = limits[1] - SAFETY_MARGIN;
        if (min > max) {
            min = limits[0];
            max = limits[1];
        }
        float clamped = Math.max(min, Math.min(max, value));
        if (clamped != value) {
            key.setAttribute("value", String.format(Locale.US, "%.6f", clamped));
        }
    }

    private static void removeTangents(Element key) {
        NodeList tangents = key.getElementsByTagName("Tangent");
        List<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < tangents.getLength(); i++) {
            toRemove.add(tangents.item(i));
        }
        for (Node node : toRemove) {
            key.removeChild(node);
        }
    }

    private static Element makeTangent(Document doc, String side, float abscissa) {
        Element tangent = doc.createElement("Tangent");
        tangent.setAttribute("side", side);
        tangent.setAttribute("abscissaParam", String.format(Locale.US, "%.4f", abscissa));
        tangent.setAttribute("ordinateParam", "0");
        tangent.setAttribute("editor:interpType", "bezier_auto");
        return tangent;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Float parseFloat(String value) {
        try {
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
