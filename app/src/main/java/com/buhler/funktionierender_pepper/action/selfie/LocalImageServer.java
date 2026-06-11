package com.buhler.funktionierender_pepper.action.selfie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class LocalImageServer extends NanoHTTPD {

    private final File rootDir;

    public LocalImageServer(File rootDir, int port) {
        super(port);
        this.rootDir = rootDir;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String name = new File(session.getUri()).getName();
        File file = new File(rootDir, name);

        if (!file.exists() || !file.isFile()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found");
        }

        try {
            Response response = newChunkedResponse(Response.Status.OK, "image/jpeg", new FileInputStream(file));
            response.addHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
            return response;
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error");
        }
    }
}
