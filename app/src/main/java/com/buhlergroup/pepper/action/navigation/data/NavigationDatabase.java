package com.buhlergroup.pepper.action.navigation.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RoomScanEntity.class, WaypointEntity.class}, version = 1)
public abstract class NavigationDatabase extends RoomDatabase {

    private static volatile NavigationDatabase instance;

    public static NavigationDatabase get(Context context) {
        if (instance == null) {
            synchronized (NavigationDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NavigationDatabase.class,
                            "navigation.db")
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract NavigationDao navigationDao();
}
