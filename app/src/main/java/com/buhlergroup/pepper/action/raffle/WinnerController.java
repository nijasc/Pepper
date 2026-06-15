package com.buhlergroup.pepper.action.raffle;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.buhlergroup.pepper.lang.SpeechManager;

public final class WinnerController {

    private static final String TAG = "WinnerController";

    public interface StateListener {
        void onWinnerStateChanged(boolean active);
    }

    private static final WinnerController INSTANCE = new WinnerController();

    private volatile WinnerView view;
    private volatile boolean active;
    private volatile StateListener stateListener;

    private WinnerController() {
    }

    public static WinnerController get() {
        return INSTANCE;
    }

    public void attachView(WinnerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    public boolean isActive() {
        return active;
    }

    private void notifyState(boolean value) {
        StateListener l = stateListener;
        if (l != null) {
            l.onWinnerStateChanged(value);
        }
    }

    public void celebrate(QiContext context, String winnerName) {
        WinnerView board = view;
        if (board == null || context == null || active) {
            return;
        }
        Thread thread = new Thread(() -> {
            active = true;
            notifyState(true);
            try {
                board.showSuspense();
                say(context, "Achtung, jetzt wird es spannend! Trommelwirbel, bitte!");
                say(context, "Und der Gewinner unserer Verlosung ist…");
                Thread.sleep(1000);
                say(context, "Drei…");
                Thread.sleep(600);
                say(context, "Zwei…");
                Thread.sleep(600);
                say(context, "Eins…");
                Thread.sleep(400);
                board.revealWinner(winnerName);
                say(context, "Herzlichen Glückwunsch, " + winnerName + "!");
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                board.hide();
                active = false;
                notifyState(false);
            }
        }, "winner-celebrate");
        thread.setDaemon(true);
        thread.start();
    }

    private void say(QiContext context, String text) {
        try {
            SpeechManager.getInstance().systemSay(context, text);
        } catch (Exception e) {
            Log.w(TAG, "say failed: " + e.getMessage());
        }
    }
}
