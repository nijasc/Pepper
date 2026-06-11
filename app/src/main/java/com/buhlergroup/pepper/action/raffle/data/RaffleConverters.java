package com.buhlergroup.pepper.action.raffle.data;

import androidx.room.TypeConverter;

public final class RaffleConverters {

    @TypeConverter
    public static String fromStatus(RaffleStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static RaffleStatus toStatus(String value) {
        return value == null ? null : RaffleStatus.valueOf(value);
    }
}
