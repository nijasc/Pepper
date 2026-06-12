package com.buhlergroup.pepper.action.dynamicanim;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class QianimPostProcessor {

    private static final String TAG = "DynAnim";
    private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String EDITOR_NS = "http://www.aldebaran.com/animation/editor";

    private QianimPostProcessor() {
    }

    public static String ensureTangents(String xml) {
        if (xml == null) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            Element root = doc.getDocumentElement();
            if (root != null && !root.hasAttribute("xmlns:editor")) {
                root.setAttribute("xmlns:editor", EDITOR_NS);
            }

            NodeList keys = doc.getElementsByTagName("Key");
            for (int i = 0; i < keys.getLength(); i++) {
                Element key = (Element) keys.item(i);
                if (key.getElementsByTagName("Tangent").getLength() > 0) {
                    continue;
                }
                key.appendChild(makeTangent(doc, "left", -3.0f));
                key.appendChild(makeTangent(doc, "right", 3.0f));
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return XML_PROLOG + "\n" + writer.toString().trim();
        } catch (Exception e) {
            Log.w(TAG, "ensureTangents failed, using original qianim: " + e.getMessage());
            return xml;
        }
    }

    private static Element makeTangent(Document doc, String side, float abscissa) {
        Element tangent = doc.createElement("Tangent");
        tangent.setAttribute("side", side);
        tangent.setAttribute("abscissaParam", String.valueOf(abscissa));
        tangent.setAttribute("ordinateParam", "0");
        tangent.setAttribute("editor:interpType", "bezier_auto");
        return tangent;
    }
}
