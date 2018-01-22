package pt.up.fe.beta.labtablet.async;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.utils.FileMgr;
import pt.up.fe.beta.labtablet.utils.Utils;

/**
 * Process metadata queues after editing or validating metadata records
 */
public class AsyncFileImporter extends AsyncTask<Object, Integer, DataItem> {

    private final AsyncTaskHandler<DataItem> mHandler;
    private Exception error;

    public AsyncFileImporter(AsyncTaskHandler<DataItem> mHandler) {
        this.mHandler = mHandler;
    }


    @Override
    protected DataItem doInBackground(Object... params) {

        if (params[0] == null || params[1] == null
                || params[2] == null) {
            error = new Exception("Params for this asynctaks were not provided");
            return null;
        } else if (!(params[0] instanceof Activity ||
                params[1] instanceof Intent ||
                params[2] instanceof String)) {
            error = new Exception("Expected [Activity,Intent,String]; received "
                    + params[0].getClass() + "," + params[1].getClass() + "," + params[2].getClass());
            return null;
        }

        Context mContext = (Context) params[0];
        Intent mIntent = (Intent) params[1];
        String favoriteName = (String) params[2];

        Log.e("EXTRA", "PICK FILE");
        File importFile = new File(mIntent.getData().getPath());
        if (!importFile.exists()) {
            //second attempt, as file may be in the media store
            String path = FileMgr.getRealPathFromURI(mContext, mIntent.getData());
            importFile = new File(path);
            if (!importFile.exists()) {
                error = new Exception("Failed to import file: " + importFile.getAbsolutePath());
                return null;
            }
        }

        String destPath = Environment.getExternalStorageDirectory() + File.separator
                + mContext.getResources().getString(R.string.app_name) + File.separator
                + favoriteName + File.separator + importFile.getName();

        File destFile = new File(destPath);
        if (destFile.exists()) {
            error = new Exception("File already exists. The file was not imported as it is already available in the project.");
            return null;
        }

        try {
            InputStream in = new FileInputStream(importFile);
            OutputStream out = new FileOutputStream(destFile);

            publishProgress(0);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            long fileLength = in.available();
            long total = 0;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                total += len;

                //we don't want to divide by zero, now do we?
                if (fileLength != 0) {
                    long progress = (total * 100 / fileLength);
                    publishProgress(Utils.safeLongToInt(progress));
                }
            }
            in.close();
            out.close();
            FileMgr.copy(importFile, destFile);
        } catch (IOException e) {
            error = e;
            return null;
        }

        DataItem dataItem = new DataItem();
        dataItem.setLocalPath(destPath.toLowerCase());
        dataItem.setHumanReadableSize(FileMgr.humanReadableByteCount(destFile.length(), false));
        dataItem.setMimeType(FileMgr.getMimeType(destPath));

        //Build metadata for this file
        ArrayList<Descriptor> itemLevelMetadata = new ArrayList<>();

        ArrayList<Descriptor> loadedDescriptors =
                FavoriteMgr.getBaseDescriptors(mContext);

        //If additional metadata is available, it should me added here
        for (Descriptor desc : loadedDescriptors) {
            String tag = desc.getTag();
            switch (tag) {
                case Utils.TITLE_TAG:
                    desc.setValue(destFile.getName());
                    itemLevelMetadata.add(desc);
                    break;
                case Utils.CREATED_TAG:
                    desc.setValue("" + new Date());
                    itemLevelMetadata.add(desc);
                    break;
                case Utils.DESCRIPTION_TAG:
                    desc.setValue("");
                    itemLevelMetadata.add(desc);
                    break;
            }
        }

        dataItem.setFileLevelMetadata(itemLevelMetadata);
        return dataItem;
    }


    @Override
    protected void onPostExecute(DataItem result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mHandler.onProgressUpdate(values[0]);
    }
}
