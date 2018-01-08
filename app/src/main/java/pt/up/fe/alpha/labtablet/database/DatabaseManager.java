package pt.up.fe.alpha.labtablet.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Room;
import android.content.Context;

public class DatabaseManager {


    private AppDatabase db = null;

    /*
    public AppDatabase createDatabase(String name, Context context)
    {
        this.db = Room.databaseBuilder(context, AppDatabase.class, name).build();
        return this.db;
    };*/

    /*
    public AppDatabase getDatabase()
    {
        return this.db;
    }*/

    public DatabaseManager(AppDatabase database) {
        this.db = database;
    }
}
