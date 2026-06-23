package com.buhlergroup.pepper.action.profile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResourceDao {

    @Insert
    void insert(ResourceEntity resource);

    @Query("SELECT * FROM profile_resources WHERE profile_id = :profileId ORDER BY added_at ASC")
    List<ResourceEntity> getForProfile(String profileId);

    @Query("SELECT * FROM profile_resources WHERE id = :id LIMIT 1")
    ResourceEntity findById(String id);

    @Query("DELETE FROM profile_resources WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM profile_resources WHERE profile_id = :profileId")
    void deleteForProfile(String profileId);
}
