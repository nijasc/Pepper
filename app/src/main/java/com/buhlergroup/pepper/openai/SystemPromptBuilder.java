package com.buhlergroup.pepper.openai;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.action.Action;
import com.buhlergroup.pepper.action.profile.ProfileRepository;
import com.buhlergroup.pepper.action.raffle.RaffleRepository;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.perception.EmotionReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Assembles the system prompt for the conversation model, reaching into the
 * profile, emotion and raffle subsystems. Extracted from {@code OpenAIService}
 * to keep prompt assembly cohesive and independent of transport concerns.
 */
final class SystemPromptBuilder {

    private static final String TAG = "SystemPromptBuilder";

    private SystemPromptBuilder() {
    }

    static String build(QiContext context, List<Action> actions, EmotionReader emotionReader,
                        boolean withRouting) {
        ProfileRepository profiles = ProfileRepository.get(context);
        String instructions = profiles.getActiveInstructions(context);
        StringBuilder prompt = new StringBuilder(instructions);
        String contentSummary = profiles.getActiveContentSummary(context);
        if (contentSummary != null && !contentSummary.trim().isEmpty()) {
            prompt.append("\n## Wissensbasis\n").append(contentSummary).append("\n");
        }
        for (Action action : actions) {
            prompt.append("- ").append(action.getDescription()).append("\n");
        }

        prompt.append("\n## Internal Language Marker\n")
                .append("Begin every reply with a machine marker of the exact form [[lang:CODE]] where CODE is the ")
                .append("ISO 639-1 code of the language you are replying in (for example [[lang:de]], [[lang:en]], ")
                .append("[[lang:ja]]). Write the marker exactly once at the very start with nothing before it, then ")
                .append("your normal spoken reply. The marker is removed automatically before your reply is spoken ")
                .append("and must never appear inside the reply or influence its wording.\n");

        if (withRouting) {
            prompt.append("\n## Action Routing\n")
                    .append("Immediately after the language marker, decide which of these actions handles the ")
                    .append("user's message and write a second machine marker of the exact form [[action:NAME]]:\n");
            for (Action action : actions) {
                prompt.append("- ").append(action.getClass().getSimpleName()).append(": ")
                        .append(action.getDescription()).append('\n');
            }
            prompt.append("If you answer the user yourself with a normal spoken reply, use [[action:SayAction]] ")
                    .append("and then write the reply. For ANY other action output ONLY the two markers and ")
                    .append("nothing else - no reply text. Both markers are removed automatically and must ")
                    .append("never appear inside the spoken reply.\n");
        }

        String moodHint = emotionReader.moodHintForPrompt(context);
        if (moodHint != null) {
            prompt.append("\n## Visitor Emotion\n")
                    .append("The person in front of you right now ").append(moodHint).append(". ")
                    .append("You may occasionally and subtly acknowledge this if it fits the ")
                    .append("conversation, but never mention it in every reply and never force it.\n");
        }

        appendRaffleHint(context, prompt);

        return prompt.toString();
    }

    private static void appendRaffleHint(Context context, StringBuilder prompt) {
        try {
            RaffleEntity raffle = RaffleRepository.get(context).getCurrentRaffle();
            if (raffle == null) {
                return;
            }
            if (raffle.status == RaffleStatus.ACTIVE) {
                String end = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
                        .format(new Date(raffle.endDate));
                prompt.append("\n## Active Raffle\n")
                        .append("There is currently an active raffle the visitor can join: \"")
                        .append(raffle.title).append("\". ");
                if (!raffle.description.isEmpty()) {
                    prompt.append(raffle.description).append(' ');
                }
                prompt.append("It ends on ").append(end).append(". ")
                        .append("To take part the visitor gives their name and a valid e-mail address");
                if (raffle.requiresPhone) {
                    prompt.append(", and a phone number");
                }
                if (raffle.requiresSelfie) {
                    prompt.append(", and takes a selfie with you");
                }
                prompt.append(". Proactively and naturally invite the visitor to take part when it fits ")
                        .append("the conversation, but do not repeat it in every single reply.\n");
            } else if (raffle.status == RaffleStatus.ENDED) {
                prompt.append("\n## Raffle Ended\n")
                        .append("If the visitor asks about the raffle, tell them it has unfortunately ")
                        .append("already ended. Do not invite them to take part.\n");
            }
        } catch (Exception e) {
            Log.d(TAG, "appendRaffleHint failed", e);
        }
    }
}
