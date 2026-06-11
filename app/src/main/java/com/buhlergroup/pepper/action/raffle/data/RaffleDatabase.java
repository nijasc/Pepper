package com.buhlergroup.pepper.action.raffle.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {RaffleEntity.class, RaffleEntryEntity.class}, version = 2)
@TypeConverters(RaffleConverters.class)
public abstract class RaffleDatabase extends RoomDatabase {

    private static volatile RaffleDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE raffles ADD COLUMN winnerId INTEGER");
        }
    };

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
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
