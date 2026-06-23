package com.buhlergroup.pepper.action.profile.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ProfileEntity.class, ResourceEntity.class}, version = 1)
@TypeConverters(ProfileConverters.class)
public abstract class ProfileDatabase extends RoomDatabase {

    private static volatile ProfileDatabase instance;

    public static ProfileDatabase get(Context context) {
        if (instance == null) {
            synchronized (ProfileDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ProfileDatabase.class,
                            "profiles.db")
                            .addMigrations(Migrations.ALL)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract ProfileDao profileDao();

    public abstract ResourceDao resourceDao();
}
