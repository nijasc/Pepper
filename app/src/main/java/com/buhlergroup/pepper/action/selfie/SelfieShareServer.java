package com.buhlergroup.pepper.action.selfie;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

final class SelfieShareServer {

    private static final String TAG = "Selfie";
    private static final int SERVER_PORT = 8080;

    private volatile LocalImageServer server;
    private int qrViewers = 0;

    synchronized void stop() {
        qrViewers = 0;
        LocalImageServer current = server;
        if (current != null) {
            current.stop();
            server = null;
        }
    }

    synchronized void acquire(Context context) {
        qrViewers++;
        try {
            ensureServer(SelfieRepository.get(context).imagesDir(), NetworkUtils.localInetAddress(context));
        } catch (IOException e) {
            Log.w(TAG, "Could not start image server: " + e.getMessage());
        }
    }

    synchronized void release() {
        if (qrViewers > 0) {
            qrViewers--;
        }
        if (qrViewers == 0) {
            LocalImageServer current = server;
            if (current != null) {
                current.stop();
                server = null;
            }
        }
    }

    int port() {
        return SERVER_PORT;
    }

    String tokenFor(String filename) {
        LocalImageServer current = server;
        return current != null ? current.tokenFor(filename) : "";
    }

    String downloadUrl(Context context, String filename) {
        String ip = NetworkUtils.localIp(context);
        if (ip == null) {
            return null;
        }
        return "http://" + ip + ":" + SERVER_PORT + "/" + filename + "?token=" + tokenFor(filename);
    }

    private void ensureServer(File imagesDir, InetAddress bindAddress) throws IOException {
        if (server == null) {
            LocalImageServer started = new LocalImageServer(imagesDir, SERVER_PORT, bindAddress);
            started.start();
            server = started;
        }
    }
}
