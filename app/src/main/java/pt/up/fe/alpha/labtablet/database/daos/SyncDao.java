package pt.up.fe.alpha.labtablet.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.os.AsyncTask;

import java.util.List;

import pt.up.fe.alpha.labtablet.models.Dendro.Sync;

@Dao
public interface SyncDao {
    @Insert
    void insert(Sync syncedFolder);

    @Insert
    void bulkInsert(Sync... syncedFolders);

    @Query("SELECT * FROM Sync")
    List<Sync> getAll();

    @Query("SELECT * FROM Sync WHERE dendro_folder_uri LIKE :folderUri LIMIT 1")
    Sync getByUri(String folderUri);

    @Delete
    void deleteRestoredFolder(Sync syncedFolder);
}
