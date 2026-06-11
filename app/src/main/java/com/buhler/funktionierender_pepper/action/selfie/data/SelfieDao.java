package com.buhler.funktionierender_pepper.action.selfie.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SelfieDao {

    @Insert
    long insert(SelfieEntity selfie);

    @Query("SELECT * FROM selfies ORDER BY number DESC")
    List<SelfieEntity> getAll();

    @Query("SELECT * FROM selfies WHERE id = :id LIMIT 1")
    SelfieEntity findById(String id);

    @Query("UPDATE selfies SET favorite = :favorite WHERE id = :id")
    void setFavorite(String id, boolean favorite);

    @Query("DELETE FROM selfies WHERE id = :id")
    void deleteById(String id);
}
