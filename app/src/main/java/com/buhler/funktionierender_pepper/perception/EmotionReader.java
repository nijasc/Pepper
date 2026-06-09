package com.buhler.funktionierender_pepper.perception;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Emotion;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;

import java.util.List;

public class EmotionReader {

    private static final String TAG = "EmotionReader";

    private boolean mentionedLastTime = false;

    public String moodHintForPrompt(QiContext context) {
        BasicEmotion mood = readMood(context);

        if (!mood.isMentionable()) {
            mentionedLastTime = false;
            return null;
        }
        if (mentionedLastTime) {
            mentionedLastTime = false;
            return null;
        }
        mentionedLastTime = true;
        return mood.getPromptHint();
    }

    public BasicEmotion readMood(QiContext context) {
        try {
            List<Human> humans = context.getHumanAwareness().getHumansAround();
            if (humans == null || humans.isEmpty()) {
                return BasicEmotion.UNKNOWN;
            }

            Emotion emotion = humans.get(0).getEmotion();
            if (emotion == null) {
                return BasicEmotion.UNKNOWN;
            }

            return map(emotion.getPleasure(), emotion.getExcitement());
        } catch (Exception e) {
            Log.w(TAG, "Emotion konnte nicht gelesen werden: " + e.getMessage());
            return BasicEmotion.UNKNOWN;
        }
    }

    private BasicEmotion map(PleasureState pleasure, ExcitementState excitement) {
        if (pleasure == null) {
            return BasicEmotion.UNKNOWN;
        }
        boolean excited = excitement == ExcitementState.EXCITED;
        switch (pleasure) {
            case POSITIVE:
                return excited ? BasicEmotion.JOYFUL : BasicEmotion.CONTENT;
            case NEGATIVE:
                return excited ? BasicEmotion.ANGRY : BasicEmotion.SAD;
            case NEUTRAL:
                return BasicEmotion.NEUTRAL;
            default:
                return BasicEmotion.UNKNOWN;
        }
    }
}
