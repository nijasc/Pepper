package com.buhlergroup.pepper.action.dynamicanim;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public final class QianimValidator {

    private static final int MAX_FRAME = 750;
    private static final int MIN_FPS = 1;
    private static final int MAX_FPS = 120;

    private static final Map<String, float[]> LIMITS = new HashMap<>();

    static {
        LIMITS.put("HeadYaw", new float[]{-2.0857f, 2.0857f});
        LIMITS.put("HeadPitch", new float[]{-0.7068f, 0.6371f});
        LIMITS.put("LShoulderPitch", new float[]{-2.0857f, 2.0857f});
        LIMITS.put("RShoulderPitch", new float[]{-2.0857f, 2.0857f});
        LIMITS.put("LShoulderRoll", new float[]{0.0087f, 1.5620f});
        LIMITS.put("RShoulderRoll", new float[]{-1.5620f, -0.0087f});
        LIMITS.put("LElbowYaw", new float[]{-2.0857f, 2.0857f});
        LIMITS.put("RElbowYaw", new float[]{-2.0857f, 2.0857f});
        LIMITS.put("LElbowRoll", new float[]{-1.5620f, -0.0087f});
        LIMITS.put("RElbowRoll", new float[]{0.0087f, 1.5620f});
        LIMITS.put("LWristYaw", new float[]{-1.8239f, 1.8239f});
        LIMITS.put("RWristYaw", new float[]{-1.8239f, 1.8239f});
        LIMITS.put("LHand", new float[]{0f, 1f});
        LIMITS.put("RHand", new float[]{0f, 1f});
        LIMITS.put("HipRoll", new float[]{-0.5149f, 0.5149f});
        LIMITS.put("HipPitch", new float[]{-1.0385f, 1.0385f});
        LIMITS.put("KneePitch", new float[]{-0.5149f, 0.5149f});
    }

    private QianimValidator() {
    }

    public static String validate(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return "empty animation";
        }
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return "not parseable: " + e.getMessage();
        }

        Element root = doc.getDocumentElement();
        if (root == null || !"Animation".equals(root.getNodeName())) {
            return "root element is not <Animation>";
        }

        NodeList curves = doc.getElementsByTagName("ActuatorCurve");
        if (curves.getLength() == 0) {
            return "no ActuatorCurve present";
        }

        int totalKeys = 0;
        for (int i = 0; i < curves.getLength(); i++) {
            Element curve = (Element) curves.item(i);
            String actuator = curve.getAttribute("actuator");
            float[] limit = LIMITS.get(actuator);
            if (limit == null) {
                return "unknown actuator '" + actuator + "'";
            }

            int fps = parseInt(curve.getAttribute("fps"), -1);
            if (fps < MIN_FPS || fps > MAX_FPS) {
                return "invalid fps '" + curve.getAttribute("fps") + "' on " + actuator;
            }

            NodeList keys = curve.getElementsByTagName("Key");
            if (keys.getLength() == 0) {
                return "actuator '" + actuator + "' has no Key";
            }
            for (int k = 0; k < keys.getLength(); k++) {
                Node keyNode = keys.item(k);
                if (!(keyNode instanceof Element)) {
                    continue;
                }
                Element key = (Element) keyNode;
                int frame = parseInt(key.getAttribute("frame"), -1);
                if (frame < 0 || frame > MAX_FRAME) {
                    return "frame '" + key.getAttribute("frame") + "' out of range on " + actuator;
                }
                Float value = parseFloat(key.getAttribute("value"));
                if (value == null) {
                    return "non-numeric value on " + actuator;
                }
                if (value < limit[0] || value > limit[1]) {
                    return actuator + " value " + value + " outside safe range ["
                            + limit[0] + ", " + limit[1] + "]";
                }
                totalKeys++;
            }
        }

        if (totalKeys < 2) {
            return "animation needs at least two keyframes to move";
        }
        return null;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Float parseFloat(String s) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
