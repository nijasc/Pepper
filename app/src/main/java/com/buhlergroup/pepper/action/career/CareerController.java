package com.buhlergroup.pepper.action.career;

import android.graphics.Bitmap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class CareerController {

    private static final CareerController INSTANCE = new CareerController();

    private volatile CareerView view;

    private CareerController() {
    }

    public static CareerController get() {
        return INSTANCE;
    }

    public void attachView(CareerView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public boolean present(Bitmap qr, String hint, long displayMs) {
        CareerView board = view;
        if (board == null) {
            return false;
        }
        CountDownLatch dismiss = new CountDownLatch(1);
        board.setOnCloseListener(dismiss::countDown);
        board.show(qr, hint);
        try {
            dismiss.await(displayMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        board.setOnCloseListener(null);
        board.hide();
        return true;
    }
}
