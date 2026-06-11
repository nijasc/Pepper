package com.buhlergroup.pepper.action.raffle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "raffle_entries")
public class RaffleEntryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "raffle_id")
    public long raffleId;

    @NonNull
    public String name;

    @NonNull
    public String email;

    @Nullable
    public String phone;

    @ColumnInfo(name = "selfie_id")
    @Nullable
    public String selfieId;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public RaffleEntryEntity(long raffleId, @NonNull String name, @NonNull String email,
                             @Nullable String phone, @Nullable String selfieId, long createdAt) {
        this.raffleId = raffleId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.selfieId = selfieId;
        this.createdAt = createdAt;
    }
}
