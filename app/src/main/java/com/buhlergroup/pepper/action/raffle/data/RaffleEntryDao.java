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
}
