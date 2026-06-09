package com.buhler.funktionierender_pepper.action.follow;

import com.aldebaran.qi.sdk.QiContext;
import com.buhler.funktionierender_pepper.action.Action;
import com.buhler.funktionierender_pepper.lang.SpeechManager;

public class FollowMeAction extends Action {
    @Override
    public void execute(QiContext context, String input) {
        if (FollowController.get().isFollowing()) {
            SpeechManager.getInstance().say(context, "Ich folge dir bereits.");
            return;
        }
        SpeechManager.getInstance().say(context,
                "Okay, ich folge dir jetzt. Sag mir Bescheid, wenn ich stoppen soll.");
        FollowController.get().requestFollow(context);
    }

    @Override
    public String getDescription() {
        return "Makes Pepper physically walk after and follow the user.";
    }
}