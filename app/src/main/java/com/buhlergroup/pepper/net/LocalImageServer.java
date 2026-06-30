package com.buhlergroup.pepper.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LocalImageServer {

    private static final String TAG = "Selfie";

    private static final int MAX_WORKER_THREADS = 4;
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final int ACCEPT_BACKLOG = 16;
    private static final int MAX_REQUEST_LINE_LENGTH = 4096;

    private final File rootDir;
    private final int port;
    private final InetAddress bindAddress;
    private final String secret;
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKER_THREADS);

    private volatile ServerSocket serverSocket;
    private volatile boolean running = false;

    public LocalImageServer(File rootDir, int port, InetAddress bindAddress) {
        this.rootDir = rootDir;
        this.port = port;
        this.bindAddress = bindAddress;
        byte[] random = new byte[16];
        new SecureRandom().nextBytes(random);
        this.secret = toHex(random, random.length);
    }

    private static String toHex(byte[] bytes, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count && i < bytes.length; i++) {
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit(bytes[i] & 0xF, 16));
        }
        return sb.toString();
    }

    private static String paramValue(String query) {
        if (query.isEmpty()) {
            return "";
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals("token")) {
                return pair.substring(eq + 1);
            }
        }
        return "";
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    public String tokenFor(String filename) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((secret + ":" + filename).getBytes(StandardCharsets.UTF_8));
            return toHex(hash, 8);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        if (bindAddress == null) {
            Log.w(TAG, "No local WLAN address available; image server not started");
            throw new IOException("No local WLAN address available");
        }
        serverSocket = new ServerSocket(port, ACCEPT_BACKLOG, bindAddress);
        running = true;
        Thread acceptThread = new Thread(this::acceptLoop, "selfie-http");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Error closing server socket: " + e.getMessage());
        }
        executor.shutdownNow();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handle(socket));
            } catch (IOException e) {
                if (running) {
                    Log.w(TAG, "Accept failed: " + e.getMessage());
                }
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket open = socket;
             InputStream in = open.getInputStream();
             OutputStream out = open.getOutputStream()) {

            open.setSoTimeout(SOCKET_TIMEOUT_MS);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String requestLine = readRequestLine(reader);
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2 || !"GET".equals(parts[0])) {
                writeStatus(out, "405 Method Not Allowed");
                return;
            }

            String path = parts[1];
            String query = "";
            int queryIndex = path.indexOf('?');
            if (queryIndex >= 0) {
                query = path.substring(queryIndex + 1);
                path = path.substring(0, queryIndex);
            }
            String name = new File(path).getName();
            File file = new File(rootDir, name);

            if (name.isEmpty() || !file.isFile() || !isWithin(rootDir, file)) {
                writeStatus(out, "404 Not Found");
                return;
            }

            String token = paramValue(query);
            if (token.isEmpty() || !constantTimeEquals(token, tokenFor(name))) {
                writeStatus(out, "403 Forbidden");
                return;
            }

            String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: image/jpeg\r\n"
                    + "Content-Length: " + file.length() + "\r\n"
                    + "Connection: close\r\n\r\n";
            out.write(headers.getBytes(StandardCharsets.UTF_8));
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            out.flush();
        } catch (IOException e) {
            Log.w(TAG, "Request handling failed: " + e.getMessage());
        }
    }

    private String readRequestLine(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n') {
                break;
            }
            if (c == '\r') {
                continue;
            }
            if (sb.length() >= MAX_REQUEST_LINE_LENGTH) {
                throw new IOException("Request line too long");
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    private void writeStatus(OutputStream out, String status) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n"
                + "Content-Length: 0\r\n"
                + "Connection: close\r\n\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private boolean isWithin(File dir, File file) {
        try {
            return file.getCanonicalPath().startsWith(dir.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }
}
