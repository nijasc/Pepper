package com.buhlergroup.pepper.action.raffle;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.buhlergroup.pepper.action.raffle.data.RaffleDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleDatabase;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryDao;
import com.buhlergroup.pepper.action.raffle.data.RaffleEntryEntity;
import com.buhlergroup.pepper.action.raffle.data.RaffleStatus;
import com.buhlergroup.pepper.action.selfie.SelfieRepository;
import com.buhlergroup.pepper.debug.DebugLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RaffleRepository {

    private static final String TAG = "RaffleRepository";
    private static final int MAX_RETENTION_MULTIPLIER = 4;
    private static final ExecutorService maintenanceExecutor = Executors.newSingleThreadExecutor();
    private static volatile RaffleRepository instance;
    private final Context appContext;
    private final RaffleDatabase database;
    private final RaffleDao raffleDao;
    private final RaffleEntryDao entryDao;

    private RaffleRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = RaffleDatabase.get(context);
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

    public static void purgeExpiredAsync(Context context) {
        Context app = context.getApplicationContext();
        maintenanceExecutor.submit(() -> {
            try {
                get(app).purgeExpired();
            } catch (Exception e) {
                Log.w("RaffleRepository", "purgeExpired failed: " + e.getMessage());
            }
        });
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
        raffleDao.setFinished(raffleId, System.currentTimeMillis());
    }

    public void purgeExpired() {
        int days = RaffleSettings.getRetentionDays(appContext);
        if (days <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long cutoff = now - days * 24L * 60L * 60L * 1000L;
        List<RaffleEntity> expired = raffleDao.getFinishedBefore(cutoff);
        for (RaffleEntity raffle : expired) {
            deleteRaffleCompletely(raffle.id);
        }
        purgeAbandoned(now, days);
    }

    private void purgeAbandoned(long now, int days) {
        long maxCutoff = now - days * (long) MAX_RETENTION_MULTIPLIER * 24L * 60L * 60L * 1000L;
        List<Long> stale = staleRaffleIds(maxCutoff);
        if (stale.isEmpty()) {
            return;
        }
        DebugLog.get().w(TAG, "Erzwinge Aufbewahrungslimit für " + stale.size() + " überfällige Verlosung(en)");
        for (long id : stale) {
            deleteRaffleCompletely(id);
        }
    }

    private List<Long> staleRaffleIds(long maxCutoff) {
        List<Long> ids = new ArrayList<>();
        Cursor cursor = database.query(
                "SELECT id FROM raffles WHERE created_at > 0 AND created_at < ?",
                new Object[]{maxCutoff});
        try {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
        } finally {
            cursor.close();
        }
        return ids;
    }

    public long addEntry(long raffleId, String name, String email, String phone, String selfieId) {
        RaffleEntryEntity entry = new RaffleEntryEntity(raffleId, name, email, phone, selfieId,
                System.currentTimeMillis());
        return entryDao.insert(entry);
    }

    public JoinResult joinRaffle(long raffleId, String name, String email, String phone,
                                 boolean requiresPhone, String selfieId) {
        final JoinResult[] result = {JoinResult.NOT_ACTIVE};
        database.runInTransaction(() -> {
            RaffleEntity raffle = raffleDao.findById(raffleId);
            if (raffle == null || raffle.status != RaffleStatus.ACTIVE) {
                result[0] = JoinResult.NOT_ACTIVE;
                return;
            }
            if (raffle.endDate > 0 && raffle.endDate < System.currentTimeMillis()) {
                result[0] = JoinResult.NOT_ACTIVE;
                return;
            }
            if (entryDao.countByEmail(raffleId, email) > 0) {
                result[0] = JoinResult.DUPLICATE_EMAIL;
                return;
            }
            if (requiresPhone && phone != null && entryDao.countByPhone(raffleId, phone) > 0) {
                result[0] = JoinResult.DUPLICATE_PHONE;
                return;
            }
            entryDao.insert(new RaffleEntryEntity(raffleId, name, email, phone, selfieId,
                    System.currentTimeMillis()));
            result[0] = JoinResult.SUCCESS;
        });
        if (result[0] == JoinResult.SUCCESS) {
            com.buhlergroup.pepper.stats.Stats.increment(appContext,
                    com.buhlergroup.pepper.stats.Stats.RAFFLE_JOINS);
        }
        return result[0];
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

    public String buildAccessReport(String email) {
        List<RaffleEntryEntity> entries = entryDao.getEntriesByEmail(email);
        if (entries.isEmpty()) {
            return null;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        StringBuilder sb = new StringBuilder();
        for (RaffleEntryEntity entry : entries) {
            RaffleEntity raffle = raffleDao.findById(entry.raffleId);
            sb.append("Verlosung: ")
                    .append(raffle != null ? raffle.title : ("#" + entry.raffleId)).append('\n');
            sb.append("Name: ").append(entry.name).append('\n');
            sb.append("E-Mail: ").append(entry.email).append('\n');
            sb.append("Telefon: ")
                    .append(entry.phone != null && !entry.phone.isEmpty() ? entry.phone : "-").append('\n');
            sb.append("Selfie: ")
                    .append(entry.selfieId != null && !entry.selfieId.isEmpty() ? entry.selfieId : "-")
                    .append('\n');
            sb.append("Eingetragen: ").append(fmt.format(new Date(entry.createdAt))).append("\n\n");
        }
        return sb.toString().trim();
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

    public RaffleEntryEntity pickReplacementWinner(long raffleId) {
        RaffleEntity raffle = raffleDao.findById(raffleId);
        Long exclude = raffle != null ? raffle.winnerId : null;
        RaffleEntryEntity entry = exclude != null
                ? entryDao.getRandomEntryExcluding(raffleId, exclude)
                : entryDao.getRandomEntry(raffleId);
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

    public enum JoinResult {
        SUCCESS,
        NOT_ACTIVE,
        DUPLICATE_EMAIL,
        DUPLICATE_PHONE
    }
}
