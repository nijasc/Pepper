package com.buhlergroup.pepper.action.system;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.lang.LanguageManager;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.OpenAIService;

import java.util.List;

public class SystemInfoAction extends Action {
    private final LanguageManager lm;
    private final OpenAIService openAi;

    public SystemInfoAction(LanguageManager lm, List<Action> actions) {
        this.lm = lm;
        this.openAi = new OpenAIService(actions);
    }

    @Override
    public void execute(QiContext context, String input) {
        String sysInfo = buildSystemInfo(context);
        Log.i(this.getClass().getSimpleName(), sysInfo);

        getHistoryManager().addDeveloper(sysInfo, this);
        getHistoryManager().addUser(input);

        String answer = openAi.getResponse(getHistoryManager(), context);

        getHistoryManager().addAssistant(answer, this);
        SpeechManager.getInstance().say(context, answer, openAi.lastLanguageTag());
    }

    private String buildSystemInfo(QiContext context) {
        return new StringBuilder()
                .append("Du hast folgende Informationen zur Verfügung, ")
                .append("beantworte dem Benutzer die unten gestellte Frage basierend auf ihnen:\n")
                .append("- Current history length: ").append(getHistoryManager().historySize()).append('\n')
                .append("- Current language set to: ").append(lm.getCurrent().name()).append('\n')
                .append("- Current system volume: ").append(describeVolume(context)).append('\n')
                .toString();
    }

    private String describeVolume(Context context) {
        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager == null) {
            return "unbekannt (AudioManager nicht verfügbar)";
        }

        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int percent = maxVol > 0 ? Math.round((vol / (float) maxVol) * 100) : 0;

        return vol + " of " + maxVol + " (" + percent + "%)";
    }

    @Override
    public String getDescription() {
        return "Reports Pepper's current volume, active language or history length without changing them.";
    }
}