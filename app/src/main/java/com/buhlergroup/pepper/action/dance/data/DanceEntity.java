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

    @ColumnInfo(name = "qianim_path")
    @Nullable
    public String qianimPath;

    @ColumnInfo(name = "preview_url")
    @Nullable
    public String previewUrl;

    @ColumnInfo(name = "duration_ms")
    public long durationMs;

    public boolean favorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public DanceEntity(@NonNull String youtubeId, @NonNull String songName,
                       @Nullable String qianimPath, @Nullable String previewUrl,
                       long durationMs, boolean favorite, long createdAt) {
        this.youtubeId = youtubeId;
        this.songName = songName;
        this.qianimPath = qianimPath;
        this.previewUrl = previewUrl;
        this.durationMs = durationMs;
        this.favorite = favorite;
        this.createdAt = createdAt;
    }
}
