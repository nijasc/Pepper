package com.buhlergroup.pepper.action.hold;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;

/**
 * ASR phrase listener for the "stop holding" voice command.
 * Stateless: it builds and runs a one-shot {@link Listen} and invokes the
 * supplied callback when a release phrase is heard. It carries no session
 * state of its own — the caller owns the state machine.
 */
final class HoldSpeechListener {

    private static final String TAG = "HoldMyBeer";

    private HoldSpeechListener() {
    }

    /**
     * Starts an async listen for release phrases. When a phrase is heard,
     * {@code onPhraseHeard} runs. Returns the underlying future so the caller
     * can poll {@code isDone()} and request cancellation, or {@code null} on
     * setup failure.
     */
    static Future<ListenResult> start(QiContext context, Runnable onPhraseHeard) {
        try {
            PhraseSet phrases = PhraseSetBuilder.with(context)
                    .withTexts("stopp", "stop", "danke", "danke schön", "gib her", "fertig",
                            "thanks", "thank you", "give it back", "done")
                    .build();
            Listen listen = ListenBuilder.with(context)
                    .withPhraseSet(phrases)
                    .build();
            Future<ListenResult> future = listen.async().run();
            future.thenConsume(f -> {
                if (f.isCancelled() || f.hasError()) {
                    return;
                }
                ListenResult result = f.get();
                if (result != null && result.getHeardPhrase() != null
                        && !result.getHeardPhrase().getText().isEmpty()) {
                    onPhraseHeard.run();
                }
            });
            return future;
        } catch (Exception e) {
            Log.w(TAG, "Stop listener failed: " + e.getMessage());
            return null;
        }
    }
}
