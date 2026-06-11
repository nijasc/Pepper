package com.buhlergroup.pepper.action.raffle.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "raffles")
public class RaffleEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @NonNull
    public String description;

    @NonNull
    public RaffleStatus status;

    @ColumnInfo(name = "requires_selfie")
    public boolean requiresSelfie;

    @ColumnInfo(name = "requires_phone")
    public boolean requiresPhone;

    @ColumnInfo(name = "end_date")
    public long endDate;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public RaffleEntity(@NonNull String title, @NonNull String description, @NonNull RaffleStatus status,
                        boolean requiresSelfie, boolean requiresPhone, long endDate, long createdAt) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.requiresSelfie = requiresSelfie;
        this.requiresPhone = requiresPhone;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }
}
