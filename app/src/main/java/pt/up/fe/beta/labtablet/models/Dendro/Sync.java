package pt.up.fe.beta.labtablet.models.Dendro;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.AsyncTask;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.up.fe.beta.labtablet.database.AppDatabase;

/**
 * Instance of synchronization record with Dendro
 */
@Entity(tableName = "Sync")
public class Sync {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "folder_title")
    private String folderTitle;

    @ColumnInfo(name = "dendro_instance_uri")
    private String dendroInstanceUri;

    @ColumnInfo(name = "dendro_folder_uri")
    private String dendroFolderUri;

    @ColumnInfo(name = "export_date")
    private Date exportDate;

    @ColumnInfo(name = "ok")
    private boolean ok;

    public String getDendroInstanceUri() {
        return dendroInstanceUri;
    }

    public void setDendroInstanceUri(String dendroInstanceUri) {
        this.dendroInstanceUri = dendroInstanceUri;
    }

    public Sync(String folderTitle, String dendroInstanceUri, String dendroFolderUri, Date exportDate, boolean ok) {
        this.folderTitle = folderTitle;
        this.dendroFolderUri = dendroFolderUri;
        this.dendroInstanceUri = dendroInstanceUri;
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

    /*
    public void insertAsync(AsyncTask<Void, Void, Void> callback) {

    }*/


    public Boolean insertSync(AppDatabase db)
    {
        try {
            Boolean result = new InsertTask(db).execute(this).get();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertAsync(AppDatabase db)
    {
        new InsertTask(db).execute(this);
    }

    public static List<Sync> getAllSync(AppDatabase db)
    {

        try {
            List<Sync> results = new GetAllTask(db).execute().get();
            return results;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Sync> getAllWithTitleSync(AppDatabase db, String folderTitle)
    {

        try {
            List<Sync> results = new GetAllWithTitleTask(db).execute(folderTitle).get();
            return results;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateSync(AppDatabase db)
    {
        Boolean result = false;
        try {
            result = new UpdateTask(db).execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateAsync(AppDatabase db)
    {
        new UpdateTask(db).execute(this);
    }

    public boolean deleteSync(AppDatabase db)
    {
        try {
            Boolean result = new DeleteTask(db).execute(this).get();
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteAsync(AppDatabase db)
    {
        new DeleteTask(db).execute(this);
    }



    private static class UpdateTask extends AsyncTask<Sync, Void, Boolean>
    {
        private AppDatabase db;
        UpdateTask(AppDatabase db)
        {
            this.db = db;
        }

        @Override
        protected Boolean doInBackground(Sync... syncs) {
            db.syncDao().updateRestoredFolder(syncs[0]);
            return true;
        }

        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
        }
    }

    private static class DeleteTask extends AsyncTask<Sync, Void, Boolean>
    {
        private AppDatabase db;
        DeleteTask(AppDatabase db)
        {
            this.db = db;
        }

        @Override
        protected Boolean doInBackground(Sync... syncs) {
            db.syncDao().deleteRestoredFolder(syncs[0]);
            return true;
        }

        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
        }
    }

    private static class GetAllTask extends AsyncTask<Void, Void, List<Sync>>
    {
        private AppDatabase db;
        GetAllTask(AppDatabase db)
        {
            this.db = db;
        }

        @Override
        protected List<Sync> doInBackground(Void... params) {
            List<Sync> listOfRestoredFolders = db.syncDao().getAll();
            return listOfRestoredFolders;
        }

        protected void onPostExecute(List<Sync> results)
        {
            super.onPostExecute(results);
        }
    }

    private static class GetAllWithTitleTask extends AsyncTask<String, Void, List<Sync>>
    {
        private AppDatabase db;
        GetAllWithTitleTask(AppDatabase db)
        {
            this.db = db;
        }

        @Override
        protected List<Sync> doInBackground(String... params) {
            List<Sync> listOfRestoredFolders = db.syncDao().getAllWithFolderTitle(params[0]);
            return listOfRestoredFolders;
        }

        protected void onPostExecute(List<Sync> results)
        {
            super.onPostExecute(results);
        }
    }

    private static class InsertTask extends AsyncTask<Sync, Void, Boolean>
    {
        private AppDatabase db;
        InsertTask(AppDatabase db)
        {
            this.db = db;
        }

        @Override
        protected Boolean doInBackground(Sync... syncs) {
            db.syncDao().insert(syncs[0]);
            return true;
        }

        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
        }
    }

}
