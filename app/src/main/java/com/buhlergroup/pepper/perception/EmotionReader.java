package com.buhlergroup.pepper.perception;

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
            Log.i(TAG, "Stimmung '" + mood + "' wird nicht erwaehnt (neutral oder unbekannt).");
            mentionedLastTime = false;
            return null;
        }
        if (mentionedLastTime) {
            Log.i(TAG, "Stimmung '" + mood + "' erkannt, aber Cooldown aktiv - diesmal kein Hinweis.");
            mentionedLastTime = false;
            return null;
        }
        mentionedLastTime = true;
        Log.i(TAG, "Stimmung '" + mood + "' wird als Kontext an den Systemprompt uebergeben.");
        return mood.getPromptHint();
    }

    public BasicEmotion readMood(QiContext context) {
        try {
            List<Human> humans = context.getHumanAwareness().getHumansAround();
            if (humans == null || humans.isEmpty()) {
                Log.i(TAG, "Keine Person wahrgenommen - keine Emotion erkannt.");
                return BasicEmotion.UNKNOWN;
            }

            Log.i(TAG, "Personen wahrgenommen: " + humans.size() + " - werte die naechste aus.");
            Emotion emotion = humans.get(0).getEmotion();
            if (emotion == null) {
                Log.i(TAG, "Person erkannt, aber keine Emotionsdaten verfuegbar.");
                return BasicEmotion.UNKNOWN;
            }

            PleasureState pleasure = emotion.getPleasure();
            ExcitementState excitement = emotion.getExcitement();
            BasicEmotion mood = map(pleasure, excitement);
            Log.i(TAG, "Rohwerte - Pleasure: " + pleasure + ", Excitement: " + excitement
                    + " -> Grundstimmung: " + mood);
            return mood;
        } catch (Exception e) {
            Log.w(TAG, "Emotion konnte nicht gelesen werden: " + e.getMessage(), e);
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
