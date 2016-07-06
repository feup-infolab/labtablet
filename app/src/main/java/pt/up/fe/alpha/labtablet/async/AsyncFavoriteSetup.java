package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.models.FavoriteItem;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Async task to load a favorite package into the LabTablet application
 */
public class AsyncFavoriteSetup  extends AsyncTask<Object, Integer, String> {

    //input, remove, output
    private final AsyncTaskHandler<String> mHandler;
    private Exception error;


    public AsyncFavoriteSetup(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mHandler.onProgressUpdate(values[0]);
    }


    @Override
    protected void onCancelled() {
        Log.e("", "cancelled");
        super.onCancelled();
    }

    @Override
    protected String doInBackground(Object... params) {

        if (!(params[0] instanceof Context)) {
            error = new Exception("Type mismatch, (expected Context)");
            return null;
        }

        if (!(params[1] instanceof Uri)) {
            error = new Exception("Type mismatch, (expected Uri)");
            return null;
        }

        Context context = (Context) params[0];
        Uri uri = (Uri) params[1];

        //Unzip
        boolean unzipResult = unpackZip(uri, context);
//        File favoriteFile = new File(destPath);

        //Import metadata

        //Read metadata form file
        String json = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            error = ex;
            Log.e("IMPORT", ex.toString());
            return null;
        }


        //ArrayList<Descriptor> itemMetadata = new Gson().fromJson(json, Utils.ARRAY_DESCRIPTORS);
        //import

        //FavoriteItem newItem = new FavoriteItem(favoriteName);
        return  "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(null);
        }
    }

    /**
     *
     * @param uri
     * @return
     */
    private boolean unpackZip(Uri uri, Context context)
    {
        InputStream is;
        ZipInputStream zis;

        try
        {
            String filename;
            is = context.getContentResolver().openInputStream(uri);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName().substring(ze.getName().lastIndexOf("/") + 1);
                if (ze.isDirectory()) {
                    File fmd = new File(Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name) + ze.getName());
                    if (!fmd.mkdirs()) {
                        Log.e("UNZIP", "Failed to create entry at " + fmd.getPath());
                    }
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name) + ze.getName());

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
