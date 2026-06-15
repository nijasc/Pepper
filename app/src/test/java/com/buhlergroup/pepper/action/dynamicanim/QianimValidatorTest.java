package com.buhlergroup.pepper.action.dynamicanim;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class QianimValidatorTest {

    private Document parse(String xml) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void acceptsValidAnimation() throws Exception {
        String xml = "<Animation><ActuatorList>"
                + "<ActuatorCurve actuator=\"HeadYaw\" fps=\"25\">"
                + "<Key frame=\"0\" value=\"0.0\"/>"
                + "<Key frame=\"10\" value=\"0.5\"/>"
                + "</ActuatorCurve></ActuatorList></Animation>";
        assertNull(QianimValidator.validate(parse(xml)));
    }

    @Test
    public void rejectsUnknownActuator() throws Exception {
        String xml = "<Animation><ActuatorCurve actuator=\"Nope\" fps=\"25\">"
                + "<Key frame=\"0\" value=\"0.0\"/><Key frame=\"5\" value=\"0.1\"/>"
                + "</ActuatorCurve></Animation>";
        assertNotNull(QianimValidator.validate(parse(xml)));
    }

    @Test
    public void rejectsValueOutsideSafeRange() throws Exception {
        String xml = "<Animation><ActuatorCurve actuator=\"HeadYaw\" fps=\"25\">"
                + "<Key frame=\"0\" value=\"0.0\"/><Key frame=\"5\" value=\"5.0\"/>"
                + "</ActuatorCurve></Animation>";
        assertNotNull(QianimValidator.validate(parse(xml)));
    }

    @Test
    public void rejectsNullDocument() {
        assertNotNull(QianimValidator.validate(null));
    }
}
