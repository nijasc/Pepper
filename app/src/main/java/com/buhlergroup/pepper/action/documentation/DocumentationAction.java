package com.buhlergroup.pepper.action.documentation;

import android.content.Context;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.OpenAIService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class DocumentationAction extends Action {
    private static final String DOCUMENTATION_URL =
            "https://raw.githubusercontent.com/nijasc/Pepper/main/README.md";

    private final OpenAIService service;
    private String fallbackDocumentation;

    public DocumentationAction(List<Action> actions) {
        this.service = new OpenAIService(actions);
    }

    @Override
    public void execute(QiContext context, String input) {
        String documentation = getDocumentation(context);
        if (documentation == null) {
            SpeechManager.getInstance().systemSay(context,
                    "Ich konnte die Dokumentation nicht laden.");
            return;
        }

        Map<String, Object> docMessage = new HashMap<>();
        docMessage.put("role", "system");
        docMessage.put("content",
                "Answer the user's question only from this Pepper documentation; "
                        + "if it isn't covered, say so. Keep the answer to at most 2-3 short "
                        + "sentences and make it complete in itself.\n\n" + documentation);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", input);

        List<Object> conversation = new ArrayList<>(getHistoryManager().toInput());
        conversation.add(docMessage);
        conversation.add(userMessage);

        Map<String, Object> body = new HashMap<>();
        body.put("model", OpenAIService.DEFAULT_MODEL);
          body.put("instructions", service.formDefaultSystemPrompt(context));
        body.put("input", conversation);

        try {
            String response = service.sendOpenAiRequest("/responses", body);
            String answer = parseAnswer(response);
            getHistoryManager().addUser(input);
            getHistoryManager().addAssistant(answer, this);
            SpeechManager.getInstance().systemSay(context, answer);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            SpeechManager.getInstance().systemSay(context,
                    "Bei der Analyse der Dokumentation ist ein Fehler aufgetreten. ");
        }
    }

    private String getDocumentation(Context context) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(DOCUMENTATION_URL).openConnection();
            ((HttpsURLConnection) connection).setSSLSocketFactory(buildSocketFactory(context));
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return fallbackDocumentation;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                fallbackDocumentation = sb.toString();
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return fallbackDocumentation;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String parseAnswer(String response) throws JSONException {
        JSONObject json = new JSONObject(response);

        JSONObject error = json.optJSONObject("error");
        if (error != null) {
            throw new JSONException("OpenAI error: " + error.optString("message"));
        }

        JSONArray output = json.getJSONArray("output");
        for (int i = 0; i < output.length(); i++) {
            JSONArray content = output.getJSONObject(i).optJSONArray("content");
            if (content == null) {
                continue;
            }
            for (int j = 0; j < content.length(); j++) {
                JSONObject part = content.getJSONObject(j);
                if ("output_text".equals(part.optString("type"))) {
                    return part.getString("text");
                }
            }
        }
        throw new JSONException("No output_text in response");
    }

    private SSLSocketFactory buildSocketFactory(Context ctx) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;
        try (InputStream in = ctx.getResources().openRawResource(R.raw.gh_root)) {
            ca = cf.generateCertificate(in);
        }
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("gh_root", ca);

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(null, tmf.getTrustManagers(), null);
        return ssl.getSocketFactory();
    }

    @Override
    public String getDescription() {
        return "Answers questions about Pepper itself from its developer documentation.";
    }
}