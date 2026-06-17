package com.buhlergroup.pepper.action.raffle;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

public class JoinRaffleAction extends Action {

    public JoinRaffleAction(com.buhlergroup.pepper.openai.history.HistoryManager historyManager) {
        super(historyManager);
    }

    @Override
    public void execute(QiContext context, String input) {
        RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();

        if (raffle == null || raffle.status == RaffleStatus.FINISHED) {
            SpeechManager.getInstance().systemSay(context,
                    "Es läuft momentan leider keine Verlosung, bei der du mitmachen kannst.");
            return;
        }
        if (raffle.status == RaffleStatus.ENDED) {
            SpeechManager.getInstance().systemSay(context,
                    "Die Verlosung ist leider schon beendet, ein Beitritt ist nicht mehr möglich.");
            return;
        }

        if (raffle.requiresSelfie) {
            SpeechManager.getInstance().systemSay(context,
                    "Wie schön, dass du mitmachen möchtest! Für die Teilnahme machen wir zuerst ein Selfie.");
            SelfieEntity selfie = SelfieController.get().takeSelfieForRaffle(context);
            if (selfie == null) {
                SpeechManager.getInstance().systemSay(context,
                        "Ohne Selfie kann ich dich leider nicht eintragen.");
                return;
            }
            SpeechManager.getInstance().systemSay(context,
                    "Super! Bitte gib jetzt noch deine Daten auf meinem Tablet ein.");
            RaffleJoinController.get().join(context, raffle, selfie.id);
        } else {
            SpeechManager.getInstance().systemSay(context,
                    "Wie schön, dass du bei der Verlosung mitmachen möchtest! "
                            + "Bitte gib deine Daten auf meinem Tablet ein.");
            RaffleJoinController.get().join(context, raffle);
        }
    }

    @Override
    public String getDescription() {
        return "Lets the visitor sign up for / take part in the current prize raffle "
                + "(Verlosung beitreten, mitmachen, teilnehmen). Use when the visitor wants to enter the raffle.";
    }
}
