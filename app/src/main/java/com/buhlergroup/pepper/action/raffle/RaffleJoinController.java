package com.buhlergroup.pepper.action.raffle;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RaffleJoinController {

    private static final String TAG = "RaffleJoin";
    private static final long JOIN_TIMEOUT_MS = 60000;

    private static final RaffleJoinController INSTANCE = new RaffleJoinController();

    public interface StateListener {
        void onJoinStateChanged(boolean active);
    }

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private volatile RaffleJoinView view;
    private volatile boolean busy = false;
    private volatile StateListener stateListener;

    private RaffleJoinController() {
    }

    public static RaffleJoinController get() {
        return INSTANCE;
    }

    public void attachView(RaffleJoinView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public boolean isBusy() {
        return busy;
    }

    private void notifyState(boolean active) {
        StateListener l = stateListener;
        if (l != null) {
            l.onJoinStateChanged(active);
        }
    }

    public void join(QiContext context, RaffleEntity raffle) {
        join(context, raffle, null);
    }

    public void join(QiContext context, RaffleEntity raffle, String preCapturedSelfieId) {
        RaffleJoinView board = view;
        if (board == null) {
            say(context, "Mein Tablet ist gerade nicht bereit.");
            return;
        }
        if (busy) {
            return;
        }
        busy = true;
        notifyState(true);
        try {
            CountDownLatch done = new CountDownLatch(1);
            board.show(raffle.title, raffle.requiresPhone, new RaffleJoinView.Listener() {
                @Override
                public void onStepShown(int stepType) {
                    dbExecutor.submit(() -> say(context, stepPrompt(stepType)));
                }

                @Override
                public void onValidationError(int stepType) {
                    dbExecutor.submit(() -> say(context, stepErrorHint(stepType)));
                }

                @Override
                public void onSubmit(String name, String email, String phone) {
                    handleSubmit(context, raffle, board, name, email, phone, preCapturedSelfieId, done);
                }

                @Override
                public void onCancel() {
                    board.hide();
                    done.countDown();
                }
            });
            boolean completed = done.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!completed) {
                say(context, "Kein Problem, ich breche das Eintragen jetzt ab. "
                        + "Du kannst jederzeit noch einmal mitmachen.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            board.hide();
            busy = false;
            notifyState(false);
        }
    }

    private void handleSubmit(QiContext context, RaffleEntity raffle, RaffleJoinView board,
                              String name, String email, String phone,
                              String preCapturedSelfieId, CountDownLatch done) {
        board.setSubmitting(true);
        dbExecutor.submit(() -> {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
            RaffleRepository repo = RaffleRepository.get(board.getContext());
            RaffleEntity current = repo.getCurrentRaffle();
            if (current == null || current.id != raffle.id || current.status != RaffleStatus.ACTIVE) {
                say(context, "Die Verlosung ist gerade nicht mehr aktiv.");
                done.countDown();
                return;
            }
            if (repo.hasEntryWithEmail(raffle.id, normalizedEmail)) {
                say(context, "Mit dieser E-Mail bist du bereits eingetragen.");
                board.goToStep(RaffleJoinView.STEP_EMAIL, R.string.raffle_join_duplicate);
                return;
            }
            if (raffle.requiresPhone && repo.hasEntryWithPhone(raffle.id, phone)) {
                say(context, "Mit dieser Telefonnummer bist du bereits eingetragen.");
                board.goToStep(RaffleJoinView.STEP_PHONE, R.string.raffle_join_phone_duplicate);
                return;
            }

            String selfieId = preCapturedSelfieId;
            if (raffle.requiresSelfie && selfieId == null) {
                board.hide();
                SelfieEntity selfie = SelfieController.get().takeSelfieForRaffle(context);
                if (selfie == null) {
                    say(context, "Ohne Selfie kann ich dich leider nicht eintragen.");
                    done.countDown();
                    return;
                }
                selfieId = selfie.id;
            }

            RaffleRepository.JoinResult result = repo.joinRaffle(raffle.id, name, normalizedEmail,
                    phone.isEmpty() ? null : phone, raffle.requiresPhone, selfieId);
            switch (result) {
                case SUCCESS:
                    say(context, "Super, " + name + "! Du bist jetzt dabei. Viel Glück bei der Verlosung!");
                    board.showConfirmation(name, done::countDown);
                    break;
                case DUPLICATE_EMAIL:
                    say(context, "Mit dieser E-Mail bist du bereits eingetragen.");
                    board.goToStep(RaffleJoinView.STEP_EMAIL, R.string.raffle_join_duplicate);
                    break;
                case DUPLICATE_PHONE:
                    say(context, "Mit dieser Telefonnummer bist du bereits eingetragen.");
                    board.goToStep(RaffleJoinView.STEP_PHONE, R.string.raffle_join_phone_duplicate);
                    break;
                case NOT_ACTIVE:
                default:
                    say(context, "Die Verlosung ist gerade nicht mehr aktiv.");
                    done.countDown();
                    break;
            }
        });
    }

    private String stepPrompt(int stepType) {
        switch (stepType) {
            case RaffleJoinView.STEP_EMAIL:
                return "Und wie lautet deine E-Mail-Adresse?";
            case RaffleJoinView.STEP_PHONE:
                return "Darf ich auch deine Telefonnummer haben?";
            case RaffleJoinView.STEP_NAME:
            default:
                return "Wie heißt du?";
        }
    }

    private String stepErrorHint(int stepType) {
        switch (stepType) {
            case RaffleJoinView.STEP_EMAIL:
                return "Diese E-Mail-Adresse sieht nicht ganz richtig aus. Magst du sie noch einmal prüfen?";
            case RaffleJoinView.STEP_PHONE:
                return "Diese Telefonnummer sieht nicht ganz richtig aus. Magst du sie noch einmal prüfen?";
            case RaffleJoinView.STEP_NAME:
            default:
                return "Ich habe deinen Namen nicht ganz erfasst. Magst du ihn noch einmal eingeben?";
        }
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }
}
