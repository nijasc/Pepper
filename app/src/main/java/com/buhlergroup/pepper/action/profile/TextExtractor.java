package com.buhlergroup.pepper.action.profile;

import android.content.Context;

import com.buhlergroup.pepper.action.profile.data.ResourceType;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class TextExtractor {

    private static final int MAX_CHARS = 200000;
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 15000;

    private TextExtractor() {
    }

    public static String fromBytes(ResourceType type, byte[] bytes, Context context) throws IOException {
        if (type == ResourceType.PDF) {
            return extractPdf(bytes, context);
        }
        return clamp(new String(bytes, StandardCharsets.UTF_8));
    }

    public static String fetchUrl(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", "PepperProfile/1.0");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + code + " für " + urlString);
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null && body.length() < MAX_CHARS) {
                    body.append(line).append('\n');
                }
            }
            return clamp(stripHtml(body.toString()));
        } finally {
            connection.disconnect();
        }
    }

    public static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        String noScript = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ");
        String noTags = noScript.replaceAll("(?s)<[^>]+>", " ");
        String decoded = noTags
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        return decoded.replaceAll("[ \\t\\x0B\\f]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String extractPdf(byte[] bytes, Context context) throws IOException {
        PDFBoxResourceLoader.init(context.getApplicationContext());
        try (InputStream in = new ByteArrayInputStream(bytes);
             PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return clamp(stripper.getText(document));
        }
    }

    private static String clamp(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > MAX_CHARS ? trimmed.substring(0, MAX_CHARS) : trimmed;
    }
}
