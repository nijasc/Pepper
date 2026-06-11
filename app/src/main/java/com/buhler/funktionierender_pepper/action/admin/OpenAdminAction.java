package com.buhler.funktionierender_pepper.action.admin;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;

public class OpenAdminAction extends Action {

    @Override
    public void execute(QiContext context, String input) {
        AdminController.get().open();
    }

    @Override
    public String getDescription() {
        return "Opens the PIN-protected admin area. Use only when the user explicitly asks for the admin area, settings or configuration (e.g. \"Admin\", \"Admin-Bereich\", \"Einstellungen\", \"open settings\").";
    }
}
