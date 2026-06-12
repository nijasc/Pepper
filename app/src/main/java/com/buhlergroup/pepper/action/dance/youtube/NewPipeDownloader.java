package com.buhlergroup.pepper.action.dance.youtube;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public final class NewPipeDownloader extends Downloader {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 30000;

    private static volatile NewPipeDownloader instance;

    private NewPipeDownloader() {
    }

    public static NewPipeDownloader getInstance() {
        if (instance == null) {
            synchronized (NewPipeDownloader.class) {
                if (instance == null) {
                    instance = new NewPipeDownloader();
                }
            }
        }
        return instance;
    }

    @Override
    public Response execute(@Nonnull Request request) throws IOException, ReCaptchaException {
        final String httpMethod = request.httpMethod();
        final String url = request.url();
        final Map<String, List<String>> headers = request.headers();
        final byte[] dataToSend = request.dataToSend();

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            connection.removeRequestProperty(name);
            for (String value : entry.getValue()) {
                connection.addRequestProperty(name, value);
            }
        }

        if (dataToSend != null) {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(dataToSend.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(dataToSend);
            }
        }

        try {
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            if (responseCode == 429) {
                throw new ReCaptchaException("reCaptcha Challenge requested", url);
            }

            InputStream stream = responseCode >= 400
                    ? connection.getErrorStream() : connection.getInputStream();
            String body = stream == null ? "" : readAll(stream);
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
            String latestUrl = connection.getURL().toString();

            return new Response(responseCode, responseMessage, responseHeaders, body, latestUrl);
        } finally {
            connection.disconnect();
        }
    }

    private String readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
