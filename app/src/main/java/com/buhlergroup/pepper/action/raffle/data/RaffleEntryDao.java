package com.buhlergroup.pepper.action.raffle.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RaffleEntryDao {

    @Insert
    long insert(RaffleEntryEntity entry);

    @Query("SELECT * FROM raffle_entries WHERE raffle_id = :raffleId ORDER BY created_at DESC")
    List<RaffleEntryEntity> getEntries(long raffleId);

    @Query("SELECT COUNT(*) FROM raffle_entries WHERE raffle_id = :raffleId")
    int countEntries(long raffleId);

    @Query("SELECT COUNT(*) FROM raffle_entries WHERE raffle_id = :raffleId AND email = :email")
    int countByEmail(long raffleId, String email);

    @Query("SELECT COUNT(*) FROM raffle_entries WHERE raffle_id = :raffleId AND phone = :phone")
    int countByPhone(long raffleId, String phone);

    @Query("SELECT DISTINCT selfie_id FROM raffle_entries WHERE selfie_id IS NOT NULL")
    List<String> getLinkedSelfieIds();

    @Query("SELECT * FROM raffle_entries WHERE raffle_id = :raffleId ORDER BY RANDOM() LIMIT 1")
    RaffleEntryEntity getRandomEntry(long raffleId);

    @Query("SELECT * FROM raffle_entries WHERE id = :entryId LIMIT 1")
    RaffleEntryEntity findEntry(long entryId);
}
