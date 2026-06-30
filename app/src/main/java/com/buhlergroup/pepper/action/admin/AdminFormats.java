package com.buhlergroup.pepper.action.admin;

import java.text.SimpleDateFormat;
import java.util.Locale;

final class AdminFormats {

    private AdminFormats() {
    }

    /**
     * Returns a fresh {@link SimpleDateFormat} for the admin date/time pattern.
     * A new instance is returned per call because SimpleDateFormat is not thread-safe.
     */
    static SimpleDateFormat dateTime() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    }
}
