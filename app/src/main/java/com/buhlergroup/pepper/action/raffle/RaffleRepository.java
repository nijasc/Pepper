package com.buhlergroup.pepper.action.raffle;

import android.content.Context;

import com.buhlergroup.pepper.action.raffle.data.RaffleDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleDatabase;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;

import java.util.List;

public final class RaffleRepository {

    private static volatile RaffleRepository instance;

    private final RaffleDao raffleDao;
    private final RaffleEntryDao entryDao;

    private RaffleRepository(Context context) {
        RaffleDatabase database = RaffleDatabase.get(context);
        this.raffleDao = database.raffleDao();
        this.entryDao = database.raffleEntryDao();
    }

    public static RaffleRepository get(Context context) {
        if (instance == null) {
            synchronized (RaffleRepository.class) {
                if (instance == null) {
                    instance = new RaffleRepository(context);
                }
            }
        }
        return instance;
    }

    public long createRaffle(String title, String description, boolean requiresSelfie,
                             boolean requiresPhone, long endDate) {
        if (raffleDao.countActive() > 0) {
            return -1;
        }
        RaffleEntity raffle = new RaffleEntity(title, description, RaffleStatus.ACTIVE,
                requiresSelfie, requiresPhone, endDate, System.currentTimeMillis());
        return raffleDao.insert(raffle);
    }

    public boolean hasActiveRaffle() {
        return raffleDao.countActive() > 0;
    }

    public RaffleEntity getCurrentRaffle() {
        return raffleDao.getCurrent();
    }

    public RaffleEntity findRaffle(long id) {
        return raffleDao.findById(id);
    }

    public void setStatus(long raffleId, RaffleStatus status) {
        raffleDao.setStatus(raffleId, status);
    }

    public void endRaffle(long raffleId) {
        raffleDao.setStatus(raffleId, RaffleStatus.ENDED);
    }

    public void finishRaffle(long raffleId) {
        raffleDao.setStatus(raffleId, RaffleStatus.FINISHED);
    }

    public long addEntry(long raffleId, String name, String email, String phone, String selfieId) {
        RaffleEntryEntity entry = new RaffleEntryEntity(raffleId, name, email, phone, selfieId,
                System.currentTimeMillis());
        return entryDao.insert(entry);
    }

    public List<RaffleEntryEntity> getEntries(long raffleId) {
        return entryDao.getEntries(raffleId);
    }

    public int countEntries(long raffleId) {
        return entryDao.countEntries(raffleId);
    }
}
