package com.buhlergroup.pepper.action.navigation.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "room_scans")
public class RoomScanEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "map_path")
    @NonNull
    public String mapPath;

    public RoomScanEntity(@NonNull String id, @NonNull String name, long createdAt, @NonNull String mapPath) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.mapPath = mapPath;
    }
}
