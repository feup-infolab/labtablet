package pt.up.fe.alpha.labtablet.models.Dendro;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.application.LabTablet;
import pt.up.fe.alpha.labtablet.utils.FileMgr;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Instance of synchronization record with Dendro
 */
@Entity(tableName = "Sync")
public class Sync {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "folder_title")
    private String folderTitle;

    @ColumnInfo(name = "dendro_folder_uri")
    private String dendroFolderUri;

    @ColumnInfo(name = "export_date")
    private String exportDate;

    @ColumnInfo(name = "ok")
    private boolean ok;

    public Sync(String folderTitle, String dendroFolderUri, String exportDate, boolean ok) {
        this.folderTitle = folderTitle;
        this.dendroFolderUri = dendroFolderUri;
        this.exportDate = exportDate;
        this.ok = ok;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getFolderTitle() {
        return folderTitle;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public String getDendroFolderUri() {
        return dendroFolderUri;
    }

    public void setDendroFolderUri(String dendroFolderUri) {
        this.dendroFolderUri = dendroFolderUri;
    }

    public String getExportDate() {
        return exportDate;
    }

    public void setExportDate(String exportDate) {
        this.exportDate = exportDate;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public void insertAsync()
    {
        
    }

    public void insertAsync(AsyncTask<Void, Void, Void> callback) {

    }

}
