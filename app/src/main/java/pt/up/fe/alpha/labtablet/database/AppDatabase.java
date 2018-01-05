package pt.up.fe.alpha.labtablet.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import pt.up.fe.alpha.labtablet.database.daos.RestoredFolderDao;
import pt.up.fe.alpha.labtablet.database.entities.RestoredFolder;

@Database(entities = {RestoredFolder.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
    public abstract RestoredFolderDao restoredFolderDao();
}
