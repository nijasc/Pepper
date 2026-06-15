package com.buhlergroup.pepper.action.raffle.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RaffleDao {

    @Insert
    long insert(RaffleEntity raffle);

    @Update
    void update(RaffleEntity raffle);

    @Query("SELECT * FROM raffles ORDER BY created_at DESC LIMIT 1")
    RaffleEntity getLatest();

    @Query("SELECT * FROM raffles WHERE status != 'FINISHED' ORDER BY created_at DESC LIMIT 1")
    RaffleEntity getCurrent();

    @Query("SELECT * FROM raffles WHERE id = :id LIMIT 1")
    RaffleEntity findById(long id);

    @Query("SELECT COUNT(*) FROM raffles WHERE status = 'ACTIVE'")
    int countActive();

    @Query("UPDATE raffles SET status = :status WHERE id = :id")
    void setStatus(long id, RaffleStatus status);

    @Query("UPDATE raffles SET status = 'FINISHED', finished_at = :finishedAt WHERE id = :id")
    void setFinished(long id, long finishedAt);

    @Query("SELECT * FROM raffles WHERE status = 'FINISHED' AND finished_at > 0 AND finished_at < :cutoff")
    java.util.List<RaffleEntity> getFinishedBefore(long cutoff);

    @Query("UPDATE raffles SET winnerId = :entryId WHERE id = :raffleId")
    void setWinner(long raffleId, long entryId);

    @Query("DELETE FROM raffles WHERE id = :id")
    void deleteById(long id);
}
