package pt.up.fe.alpha.labtablet.models.Dendro;

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
public class Sync {

    private String folderTitle;
    private String dendroFolderUri;
    private Date exportDate;
    private boolean ok;

    public Sync(String folderTitle, String dendroFolderUri, Date exportDate, boolean ok) {
        this.folderTitle = folderTitle;
        this.dendroFolderUri = dendroFolderUri;
        this.exportDate = exportDate;
        this.ok = ok;
    }

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

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
