package com.choosemuse.example.libmuse.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.choosemuse.example.libmuse.dao.dataUiDao;
import com.choosemuse.example.libmuse.dao.sampleDataDao;
import com.choosemuse.example.libmuse.entity.dataUi;
import com.choosemuse.example.libmuse.entity.sampleData;

@Database(entities = {dataUi.class, sampleData.class}, version = 1)
public abstract class RelaxDatabase extends RoomDatabase {

    private static RelaxDatabase INSTANCE;

    public abstract dataUiDao dataUi();
    public abstract sampleDataDao sampleData();

    public static RelaxDatabase getRelaxDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), RelaxDatabase.class, "focus-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
