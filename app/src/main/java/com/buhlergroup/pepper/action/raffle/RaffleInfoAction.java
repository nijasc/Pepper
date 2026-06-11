package com.buhlergroup.pepper.action.raffle;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.lang.SpeechManager;
import com.buhlergroup.pepper.openai.OpenAIService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RaffleInfoAction extends Action {

    private final OpenAIService openAi;

    public RaffleInfoAction(List<Action> actions) {
        this.openAi = new OpenAIService(actions);
    }

    @Override
    public void execute(QiContext context, String input) {
        RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();

        getHistoryManager().addDeveloper(buildInfo(raffle), this);
        getHistoryManager().addUser(input);

        String answer = openAi.getResponse(getHistoryManager(), context);

        getHistoryManager().addAssistant(answer, this);
        SpeechManager.getInstance().systemSay(context, answer);
    }

    private String buildInfo(RaffleEntity raffle) {
        if (raffle == null || raffle.status == RaffleStatus.FINISHED) {
            return "There is no raffle running right now. Tell the visitor there is currently no raffle.";
        }
        if (raffle.status == RaffleStatus.ENDED) {
            return "The raffle \"" + raffle.title + "\" has already ended. "
                    + "Tell the visitor that it is unfortunately already over and they can no longer take part.";
        }
        String end = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(new Date(raffle.endDate));
        StringBuilder sb = new StringBuilder();
        sb.append("Answer the visitor's question about the current raffle using these facts:\n")
                .append("- Title: ").append(raffle.title).append('\n')
                .append("- Description: ").append(raffle.description).append('\n')
                .append("- Ends: ").append(end).append('\n')
                .append("- To take part they give their name and a valid e-mail address");
        if (raffle.requiresPhone) {
            sb.append(", and a phone number");
        }
        if (raffle.requiresSelfie) {
            sb.append(", and take a selfie with you");
        }
        sb.append(".\nInvite the visitor warmly to take part.");
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Provides information about the current prize raffle / Gewinnspiel / Verlosung "
                + "(the prize, how to take part, the deadline) when the visitor asks about it.";
    }
}
