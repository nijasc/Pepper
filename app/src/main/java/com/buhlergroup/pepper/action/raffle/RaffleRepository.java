package com.buhlergroup.pepper.action.raffle;

import android.content.Context;

import com.buhlergroup.pepper.action.raffle.data.RaffleDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleDatabase;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;

import java.util.List;

public final class RaffleRepository {

    private static volatile RaffleRepository instance;

    private final Context appContext;
    private final RaffleDao raffleDao;
    private final RaffleEntryDao entryDao;

    private RaffleRepository(Context context) {
        this.appContext = context.getApplicationContext();
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
        refreshExpired();
        return raffleDao.countActive() > 0;
    }

    public RaffleEntity getCurrentRaffle() {
        refreshExpired();
        return raffleDao.getCurrent();
    }

    private void refreshExpired() {
        RaffleEntity current = raffleDao.getCurrent();
        if (current != null
                && current.status == RaffleStatus.ACTIVE
                && current.endDate > 0
                && current.endDate < System.currentTimeMillis()) {
            raffleDao.setStatus(current.id, RaffleStatus.ENDED);
        }
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

    public void deleteEntry(RaffleEntryEntity entry) {
        if (entry == null) {
            return;
        }
        entryDao.deleteById(entry.id);
        deleteSelfieIfOrphaned(entry.selfieId);
    }

    public void deleteRaffleCompletely(long raffleId) {
        List<RaffleEntryEntity> entries = entryDao.getEntries(raffleId);
        entryDao.deleteByRaffle(raffleId);
        for (RaffleEntryEntity entry : entries) {
            deleteSelfieIfOrphaned(entry.selfieId);
        }
        raffleDao.deleteById(raffleId);
    }

    private void deleteSelfieIfOrphaned(String selfieId) {
        if (selfieId == null || selfieId.isEmpty()) {
            return;
        }
        if (entryDao.countBySelfieId(selfieId) == 0) {
            SelfieRepository.get(appContext).delete(selfieId);
        }
    }

    public int countEntries(long raffleId) {
        return entryDao.countEntries(raffleId);
    }

    public boolean hasEntryWithEmail(long raffleId, String email) {
        return entryDao.countByEmail(raffleId, email) > 0;
    }

    public boolean hasEntryWithPhone(long raffleId, String phone) {
        return entryDao.countByPhone(raffleId, phone) > 0;
    }

    public List<String> linkedSelfieIds() {
        return entryDao.getLinkedSelfieIds();
    }

    public RaffleEntryEntity pickWinner(long raffleId) {
        RaffleEntryEntity entry = entryDao.getRandomEntry(raffleId);
        if (entry == null) {
            return null;
        }
        raffleDao.setWinner(raffleId, entry.id);
        return entry;
    }

    public RaffleEntryEntity getWinner(long raffleId) {
        RaffleEntity raffle = raffleDao.findById(raffleId);
        if (raffle == null || raffle.winnerId == null) {
            return null;
        }
        return entryDao.findEntry(raffle.winnerId);
    }
}
