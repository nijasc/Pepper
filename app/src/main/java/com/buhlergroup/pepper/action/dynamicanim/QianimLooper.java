package com.buhlergroup.pepper.action.dynamicanim;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class QianimLooper {

    private static final String TAG = "DynAnim";
    private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String REPEAT_ATTR = "repeatCycles";
    private static final int MAX_TOTAL_FRAMES = 750;

    private QianimLooper() {
    }

    public static String expand(String xml) {
        if (xml == null) {
            return null;
        }
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            int cycles = parseInt(root.getAttribute(REPEAT_ATTR), 1);
            root.removeAttribute(REPEAT_ATTR);

            if (cycles <= 1) {
                return serialize(doc);
            }

            int cycleFrames = maxFrame(doc);
            if (cycleFrames <= 0) {
                return serialize(doc);
            }
            int maxCycles = Math.max(1, MAX_TOTAL_FRAMES / cycleFrames);
            cycles = Math.min(cycles, maxCycles);

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
                        if (newFrame > MAX_TOTAL_FRAMES || usedFrames.contains(newFrame)) {
                            continue;
                        }
                        Element clone = (Element) key.cloneNode(true);
                        clone.setAttribute("frame", String.valueOf(newFrame));
                        curve.appendChild(clone);
                        usedFrames.add(newFrame);
                    }
                }
            }
            Log.i(TAG, "Expanded animation to " + cycles + " cycles of " + cycleFrames + " frames");
            return serialize(doc);
        } catch (Exception e) {
            Log.w(TAG, "expand failed, using original qianim: " + e.getMessage());
            return xml;
        }
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

    private static String serialize(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return XML_PROLOG + "\n" + writer.toString().trim();
    }
}
