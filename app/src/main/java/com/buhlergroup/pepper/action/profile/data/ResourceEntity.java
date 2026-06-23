package com.buhlergroup.pepper.action.profile.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_resources")
public class ResourceEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "profile_id")
    @NonNull
    public String profileId;

    @NonNull
    public ResourceType type;

    @NonNull
    public String title;

    @ColumnInfo(name = "source_uri")
    @Nullable
    public String sourceUri;

    @ColumnInfo(name = "text_path")
    @NonNull
    public String textPath;

    @ColumnInfo(name = "char_count")
    public int charCount;

    @ColumnInfo(name = "added_at")
    public long addedAt;

    public ResourceEntity(@NonNull String id, @NonNull String profileId, @NonNull ResourceType type,
                          @NonNull String title, @Nullable String sourceUri, @NonNull String textPath,
                          int charCount, long addedAt) {
        this.id = id;
        this.profileId = profileId;
        this.type = type;
        this.title = title;
        this.sourceUri = sourceUri;
        this.textPath = textPath;
        this.charCount = charCount;
        this.addedAt = addedAt;
    }
}
