package com.buhlergroup.pepper.action.profile.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profiles")
public class ProfileEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    @NonNull
    public String instructions;

    public boolean builtin;

    @ColumnInfo(name = "content_summary")
    @Nullable
    public String contentSummary;

    @ColumnInfo(name = "summary_dirty")
    public boolean summaryDirty;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public ProfileEntity(@NonNull String id, @NonNull String name, @NonNull String instructions,
                         boolean builtin, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.builtin = builtin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
