package com.buhlergroup.pepper.action.navigation.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "waypoints")
public class WaypointEntity {

    public static final String TYPE_GENERAL = "GENERAL";
    public static final String TYPE_FOTOSTAND = "FOTOSTAND";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "scan_id")
    @NonNull
    public String scanId;

    @NonNull
    public String name;

    @NonNull
    public String type;

    public double x;
    public double y;
    public double theta;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public WaypointEntity(@NonNull String scanId, @NonNull String name, @NonNull String type,
                          double x, double y, double theta, long createdAt) {
        this.scanId = scanId;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.theta = theta;
        this.createdAt = createdAt;
    }
}
