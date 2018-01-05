package pt.up.fe.alpha.labtablet.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import pt.up.fe.alpha.labtablet.database.daos.SyncDao;
import pt.up.fe.alpha.labtablet.models.Dendro.Sync;

@Database(entities = {Sync.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
    private static AppDatabase INSTANCE;

    public abstract SyncDao syncDao();

    public static AppDatabase getDatabase(Context context)
    {
        if(INSTANCE == null)
        {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "LabTabletDB").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
