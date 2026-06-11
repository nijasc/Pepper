package com.buhlergroup.pepper.action.selfie.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "selfies")
public class SelfieEntity {

    @PrimaryKey(autoGenerate = true)
    public long number;

    @NonNull
    public String id;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public boolean favorite;

    @NonNull
    public String filename;

    public SelfieEntity(@NonNull String id, long createdAt, boolean favorite, @NonNull String filename) {
        this.id = id;
        this.createdAt = createdAt;
        this.favorite = favorite;
        this.filename = filename;
    }
}
