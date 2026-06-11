package com.buhler.funktionierender_pepper.action.selfie;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LocalImageServer {

    private static final String TAG = "Selfie";

    private final File rootDir;
    private final int port;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private volatile ServerSocket serverSocket;
    private volatile boolean running = false;

    public LocalImageServer(File rootDir, int port) {
        this.rootDir = rootDir;
        this.port = port;
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        serverSocket = new ServerSocket(port);
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2 || !"GET".equals(parts[0])) {
                writeStatus(out, "405 Method Not Allowed");
                return;
            }

            String path = parts[1];
            int query = path.indexOf('?');
            if (query >= 0) {
                path = path.substring(0, query);
            }
            String name = new File(path).getName();
            File file = new File(rootDir, name);

            if (name.isEmpty() || !file.isFile() || !isWithin(rootDir, file)) {
                writeStatus(out, "404 Not Found");
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
