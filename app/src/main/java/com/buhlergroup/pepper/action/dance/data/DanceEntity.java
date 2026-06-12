package com.buhlergroup.pepper.action.dance.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dances")
public class DanceEntity {

    @PrimaryKey
    @ColumnInfo(name = "youtube_id")
    @NonNull
    public String youtubeId;

    @ColumnInfo(name = "song_name")
    @NonNull
    public String songName;

    @ColumnInfo(name = "audio_path")
    @NonNull
    public String audioPath;

    @ColumnInfo(name = "qianim_path")
    @Nullable
    public String qianimPath;

    @ColumnInfo(name = "duration_ms")
    public long durationMs;

    public boolean favorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public DanceEntity(@NonNull String youtubeId, @NonNull String songName, @NonNull String audioPath,
                       @Nullable String qianimPath, long durationMs, boolean favorite, long createdAt) {
        this.youtubeId = youtubeId;
        this.songName = songName;
        this.audioPath = audioPath;
        this.qianimPath = qianimPath;
        this.durationMs = durationMs;
        this.favorite = favorite;
        this.createdAt = createdAt;
    }
}
