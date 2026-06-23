package com.buhlergroup.pepper.action.profile.data;

import androidx.room.TypeConverter;

public final class ProfileConverters {

    @TypeConverter
    public static String fromType(ResourceType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static ResourceType toType(String value) {
        return value == null ? null : ResourceType.valueOf(value);
    }
}
