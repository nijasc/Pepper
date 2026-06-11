package com.buhlergroup.pepper.action.raffle.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {RaffleEntity.class, RaffleEntryEntity.class}, version = 1)
@TypeConverters(RaffleConverters.class)
public abstract class RaffleDatabase extends RoomDatabase {

    private static volatile RaffleDatabase instance;

    public abstract RaffleDao raffleDao();

    public abstract RaffleEntryDao raffleEntryDao();

    public static RaffleDatabase get(Context context) {
        if (instance == null) {
            synchronized (RaffleDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RaffleDatabase.class,
                                    "raffle.db")
                            .build();
                }
            }
        }
        return instance;
    }
}
