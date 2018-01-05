package pt.up.fe.alpha.labtablet.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pt.up.fe.alpha.labtablet.database.entities.RestoredFolder;

@Dao
public interface RestoredFolderDao {
    @Insert
    void insert(RestoredFolder restoredFolder);

    @Insert
    void bulkInsert(RestoredFolder... restoredFolders);

    @Query("SELECT * FROM RestoredFolder")
    List<RestoredFolder> getAll();

    @Query("SELECT * FROM RestoredFolder WHERE uri LIKE :folderUri LIMIT 1")
    RestoredFolder getByUri(String folderUri);

    @Delete
    void deleteRestoredFolder(RestoredFolder restoredFolder);
}
