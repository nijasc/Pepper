package com.buhlergroup.pepper.action.dance.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {DanceEntity.class}, version = 5)
public abstract class DanceDatabase extends RoomDatabase {

    private static volatile DanceDatabase instance;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dances ADD COLUMN preview_url TEXT");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `dances_new` (`youtube_id` TEXT NOT NULL, "
                    + "`song_name` TEXT NOT NULL, `qianim_path` TEXT, `duration_ms` INTEGER NOT NULL, "
                    + "`favorite` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, "
                    + "PRIMARY KEY(`youtube_id`))");
            database.execSQL("INSERT INTO `dances_new` (`youtube_id`, `song_name`, `qianim_path`, "
                    + "`duration_ms`, `favorite`, `created_at`) SELECT `youtube_id`, `song_name`, "
                    + "`qianim_path`, `duration_ms`, `favorite`, `created_at` FROM `dances`");
            database.execSQL("DROP TABLE `dances`");
            database.execSQL("ALTER TABLE `dances_new` RENAME TO `dances`");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `dances` ADD COLUMN `audio_start_ms` "
                    + "INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `dances` ADD COLUMN `audio_path` TEXT");
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build();
                }
            }
        }
        return instance;
    }
}
