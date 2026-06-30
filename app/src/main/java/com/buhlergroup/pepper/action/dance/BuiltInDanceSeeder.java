package com.buhlergroup.pepper.action.dance;

import android.content.Context;
import android.util.Log;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.dance.data.DanceDao;
import com.buhlergroup.pepper.action.dance.data.DanceDatabase;
import com.buhlergroup.pepper.action.dance.data.DanceEntity;

import java.io.File;
import java.io.IOException;

/**
 * Seeds the built-in dances (and their bundled audio) from raw resources into the
 * dance database/cache on first run, and backfills missing audio for existing entries.
 * Stateless; all methods are static.
 */
final class BuiltInDanceSeeder {

    private static final String TAG = "Dance";

    static final String BUILTIN_PREFIX = "builtin_";
    private static final String BUILTIN_HULA_ID = BUILTIN_PREFIX + "hula";
    private static final String BUILTIN_SIX_SEVEN_ID = BUILTIN_PREFIX + "six_seven";

    private BuiltInDanceSeeder() {
    }

    static void ensureBuiltInDances(Context context) {
        try {
            seedBuiltIn(context, BUILTIN_HULA_ID, "Hula",
                    R.raw.hula_dance,
                    R.raw.summer, 12000L);
            seedBuiltIn(context, BUILTIN_SIX_SEVEN_ID, "Six Seven",
                    R.raw.six_seven,
                    R.raw.wyoming, 15000L);
        } catch (Exception e) {
            Log.w(TAG, "Could not seed built-in dances: " + e.getMessage());
        }
    }

    private static void seedBuiltIn(Context context, String id, String name, int rawRes,
                                    int audioRawRes, long durationMs) throws IOException {
        DanceDao dao = DanceDatabase.get(context).danceDao();
        DanceEntity existing = dao.findById(id);
        if (existing != null && existing.qianimPath != null
                && new File(existing.qianimPath).exists()) {
            if (audioRawRes != 0
                    && (existing.audioPath == null || !new File(existing.audioPath).exists())) {
                File audio = new File(DanceFileStore.danceDir(context), id + ".mp3");
                DanceFileStore.copyRawToFile(context, audioRawRes, audio);
                dao.setAudioPath(id, audio.getAbsolutePath());
                Log.i(TAG, "Backfilled audio for built-in '" + name + "'");
            }
            return;
        }
        File target = new File(DanceFileStore.danceDir(context), id + ".qianim");
        DanceFileStore.copyRawToFile(context, rawRes, target);
        DanceEntity entity = new DanceEntity(
                id, name, target.getAbsolutePath(), durationMs, false, System.currentTimeMillis());
        if (audioRawRes != 0) {
            File audio = new File(DanceFileStore.danceDir(context), id + ".mp3");
            DanceFileStore.copyRawToFile(context, audioRawRes, audio);
            entity.audioPath = audio.getAbsolutePath();
        }
        dao.insert(entity);
        Log.i(TAG, "Seeded built-in dance '" + name + "'");
    }
}
