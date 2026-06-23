package com.buhlergroup.pepper.action.profile.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProfileEntity profile);

    @Update
    void update(ProfileEntity profile);

    @Query("SELECT * FROM profiles ORDER BY builtin DESC, name COLLATE NOCASE ASC")
    List<ProfileEntity> getAll();

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    ProfileEntity findById(String id);

    @Query("SELECT * FROM profiles WHERE builtin = 1 ORDER BY created_at ASC LIMIT 1")
    ProfileEntity getBuiltin();

    @Query("SELECT COUNT(*) FROM profiles")
    int count();

    @Query("UPDATE profiles SET name = :name, updated_at = :updatedAt WHERE id = :id")
    void setName(String id, String name, long updatedAt);

    @Query("UPDATE profiles SET instructions = :instructions, updated_at = :updatedAt WHERE id = :id")
    void setInstructions(String id, String instructions, long updatedAt);

    @Query("UPDATE profiles SET content_summary = :summary, summary_dirty = 0, updated_at = :updatedAt WHERE id = :id")
    void setSummary(String id, String summary, long updatedAt);

    @Query("UPDATE profiles SET summary_dirty = :dirty, updated_at = :updatedAt WHERE id = :id")
    void setDirty(String id, boolean dirty, long updatedAt);

    @Query("DELETE FROM profiles WHERE id = :id AND builtin = 0")
    int deleteById(String id);
}
