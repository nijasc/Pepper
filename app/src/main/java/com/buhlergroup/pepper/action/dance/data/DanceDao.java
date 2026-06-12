package com.buhlergroup.pepper.action.dance.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DanceEntity dance);

    @Query("SELECT * FROM dances ORDER BY favorite DESC, created_at DESC")
    List<DanceEntity> getAll();

    @Query("SELECT * FROM dances WHERE youtube_id = :youtubeId LIMIT 1")
    DanceEntity findById(String youtubeId);

    @Query("SELECT * FROM dances WHERE song_name LIKE :query LIMIT 1")
    DanceEntity findBySongName(String query);

    @Query("UPDATE dances SET favorite = :favorite WHERE youtube_id = :youtubeId")
    void setFavorite(String youtubeId, boolean favorite);

    @Query("UPDATE dances SET song_name = :name WHERE youtube_id = :youtubeId")
    void rename(String youtubeId, String name);

    @Query("UPDATE dances SET qianim_path = :qianimPath WHERE youtube_id = :youtubeId")
    void setQianimPath(String youtubeId, String qianimPath);

    @Query("DELETE FROM dances WHERE youtube_id = :youtubeId")
    void deleteById(String youtubeId);
}
