package com.buhlergroup.pepper.action.raffle;

import android.util.Log;
import android.util.Patterns;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.SelfieController;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;
import com.buhlergroup.pepper.lang.SpeechManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RaffleJoinController {

    private static final String TAG = "RaffleJoin";
    private static final long JOIN_TIMEOUT_MS = 180000;

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
                public void onSubmit(String name, String email, String phone) {
                    processSubmit(context, raffle, board, name, email, phone, preCapturedSelfieId, done);
                }

                @Override
                public void onCancel() {
                    board.hide();
                    done.countDown();
                }
            });
            done.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            board.hide();
            busy = false;
            notifyState(false);
        }
    }

    private void processSubmit(QiContext context, RaffleEntity raffle, RaffleJoinView board,
                               String rawName, String rawEmail, String rawPhone,
                               String preCapturedSelfieId, CountDownLatch done) {
        String name = rawName.trim();
        String email = rawEmail.trim();
        String phone = rawPhone.trim();

        if (name.isEmpty()) {
            board.showError(R.string.raffle_join_name_required);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            board.showError(R.string.raffle_join_email_invalid);
            return;
        }
        if (raffle.requiresPhone && phone.isEmpty()) {
            board.showError(R.string.raffle_join_phone_required);
            return;
        }

        board.setSubmitting(true);
        dbExecutor.submit(() -> {
            RaffleRepository repo = RaffleRepository.get(board.getContext());
            RaffleEntity current = repo.getCurrentRaffle();
            if (current == null || current.id != raffle.id || current.status != RaffleStatus.ACTIVE) {
                say(context, "Die Verlosung ist gerade nicht mehr aktiv.");
                done.countDown();
                return;
            }
            if (repo.hasEntryWithEmail(raffle.id, email)) {
                board.showError(R.string.raffle_join_duplicate);
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

            repo.addEntry(raffle.id, name, email, phone.isEmpty() ? null : phone, selfieId);
            say(context, "Super, ich habe dich für die Verlosung eingetragen. Viel Glück!");
            done.countDown();
        });
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }
}
