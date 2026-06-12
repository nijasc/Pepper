package com.buhlergroup.pepper.action.dance.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {DanceEntity.class}, version = 2)
public abstract class DanceDatabase extends RoomDatabase {

    private static volatile DanceDatabase instance;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dances ADD COLUMN preview_url TEXT");
        }
    };

    public abstract DanceDao danceDao();

    public static DanceDatabase get(Context context) {
        if (instance == null) {
            synchronized (DanceDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DanceDatabase.class,
                                    "dances.db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
