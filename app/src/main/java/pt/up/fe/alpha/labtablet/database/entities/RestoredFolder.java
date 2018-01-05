package pt.up.fe.alpha.labtablet.database.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "RestoredFolder")
public class RestoredFolder {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String uri;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "restored_at_date")
    private String restoredAtDate;

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setRestoredAtDate(String restoredAtDate)
    {
        this.restoredAtDate = restoredAtDate;
    }

    public String getUri()
    {
        return this.uri;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getRestoredAtDate()
    {
        return this.restoredAtDate;
    }
}
