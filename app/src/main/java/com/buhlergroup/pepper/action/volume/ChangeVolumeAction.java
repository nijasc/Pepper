package com.buhlergroup.pepper.action.volume;

import android.content.Context;
import android.media.AudioManager;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.history.HistoryManager;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeVolumeAction extends Action {

    public ChangeVolumeAction(HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext qiContext, String input) {

        Integer percent = extractPercentage(input);

        if (percent == null) {
            SpeechManager.getInstance().say(
                    qiContext,
                    "Bitte gib eine Lautstärke zwischen 0 und 100 an."
            );
            return;
        } else if (percent > 100) {
            SpeechManager.getInstance().say(
                    qiContext,
                    "Das ist ein bisschen zu laut für mich."
            );
            return;
        }

        setSystemVolume(percent, qiContext);

        SpeechManager.getInstance().say(
                qiContext,
                "Die Lautstärke wurde auf " + percent + " Prozent gesetzt."
        );
    }

    private Integer extractPercentage(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+)").matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
        }

        return NumberWords.parse(text);
    }

    private void setSystemVolume(int percent, Context context) {

        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int maxVolume =
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int targetVolume =
                Math.round((percent / 100f) * maxVolume);

        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                AudioManager.FLAG_SHOW_UI
        );
    }

    @Override
    public String getDescription() {
        return "Sets, raises, lowers or mutes Pepper's speaker volume.";
    }
}