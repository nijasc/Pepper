package com.buhler.funktionierender_pepper.action.selfie;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;

public class SelfieAction extends Action {

    @Override
    public void execute(QiContext context, String input) {
        SelfieController.get().takeSelfie(context);
    }

    @Override
    public String getDescription() {
        return "Takes a selfie together with the user using Pepper's camera and shows a QR code on the tablet so the user can download the photo.";
    }
}
