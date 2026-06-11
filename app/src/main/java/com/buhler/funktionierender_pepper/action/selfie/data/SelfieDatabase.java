package com.buhler.funktionierender_pepper.action.selfie.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SelfieEntity.class}, version = 1)
public abstract class SelfieDatabase extends RoomDatabase {

    private static volatile SelfieDatabase instance;

    public abstract SelfieDao selfieDao();

    public static SelfieDatabase get(Context context) {
        if (instance == null) {
            synchronized (SelfieDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    SelfieDatabase.class,
                                    "selfies.db")
                            .addMigrations(Migrations.ALL)
                            .build();
                }
            }
        }
        return instance;
    }
}
