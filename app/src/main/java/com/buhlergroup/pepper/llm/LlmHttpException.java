package com.buhlergroup.pepper.llm;

import java.io.IOException;

public final class LlmHttpException extends IOException {

    public final int statusCode;

    public LlmHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public boolean isModelError() {
        return statusCode == 400 || statusCode == 403 || statusCode == 404;
    }
}
