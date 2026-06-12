package com.buhlergroup.pepper.action.dance.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DanceEntity.class}, version = 1)
public abstract class DanceDatabase extends RoomDatabase {

    private static volatile DanceDatabase instance;

    public abstract DanceDao danceDao();

    public static DanceDatabase get(Context context) {
        if (instance == null) {
            synchronized (DanceDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DanceDatabase.class,
                                    "dances.db")
                            .build();
                }
            }
        }
        return instance;
    }
}
