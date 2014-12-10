package pt.up.fe.labtablet.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.db_handlers.DataResourcesMgr;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Process metadata queues after editing or validating metadata records
 */
public class AsyncQueueProcessor extends AsyncTask<Object, Integer, Void> {

    private AsyncTaskHandler<Void> mHandler;
    private Exception error;

    public AsyncQueueProcessor(AsyncTaskHandler<Void> mHandler) {
        this.mHandler = mHandler;
    }


    @Override
    protected Void doInBackground(Object... params) {

        if (params[0] == null || params[1] == null
                || params[2] == null || params[3] == null) {
            error = new Exception("Params for this asynctaks were not provided");
            return null;
        } else if (!(params[0] instanceof String ||
                params[1] instanceof Activity ||
                params[2] instanceof ArrayList ||
                params[3] instanceof ArrayList)) {
            error = new Exception("Was expecting a String, an Activity and two queues, received" + params[0].getClass() + " and " + params[1].getClass());
            return null;
        }

        String favoriteName = (String) params[0];
        Activity mContext = (Activity) params[1];
        ArrayList<Descriptor> deletionQueue = (ArrayList<Descriptor>) params[2];
        ArrayList<Descriptor> migrationQueue = (ArrayList<Descriptor>) params[3];

        //Delete descriptors
        if (deletionQueue != null && deletionQueue.size() > 0) {
            for (Descriptor desc : deletionQueue) {
                if (!desc.getFilePath().equals("")) {
                    if (!(new File(desc.getFilePath()).delete())) {
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("Queue Processor" + "Failed to delete file " + desc.getFilePath());
                        item.setTitle(mContext.getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, mContext);
                    }
                }
            }
        }

        //Move resources to the root folder, and add them as DataItems
        //TO THE DATA KINGDOM!
        if (migrationQueue != null && migrationQueue.size() > 0) {
            for (Descriptor desc : migrationQueue) {
                String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/" + mContext.getResources().getString(R.string.app_name)
                        + "/" + favoriteName + "/"
                        + desc.getValue();

                File src = new File(desc.getFilePath());
                File dst = new File(destinationPath);
                try {
                    FileMgr.moveFile(src, dst);
                } catch (Exception e) {
                    error = e;
                }

                DataItem item = new DataItem();
                item.setResourceName(dst.getName());
                item.setParent(favoriteName);
                item.setLocalPath(dst.getPath());
                item.setHumanReadableSize(FileMgr.humanReadableByteCount(dst.length(), false));
                item.setMimeType(FileMgr.getMimeType(dst.getPath()));

                ArrayList<Descriptor> itemLevelMetadata = new ArrayList<Descriptor>();

                ArrayList<Descriptor> loadedDescriptors =
                        FavoriteMgr.getDescriptors(Utils.DESCRIPTORS_CONFIG_ENTRY, mContext);

                //If additional metadata is available, it should me added here
                for (Descriptor dataDesc : loadedDescriptors) {
                    String tag = desc.getTag();
                    if (tag.equals(Utils.TITLE_TAG)) {
                        dataDesc.setValue(dst.getName());
                        itemLevelMetadata.add(dataDesc);
                    } else if (tag.equals(Utils.CREATED_TAG)) {
                        dataDesc.setValue("" + new Date());
                        itemLevelMetadata.add(dataDesc);
                    } else if (tag.equals(Utils.DESCRIPTION_TAG)) {
                        dataDesc.setValue("No description provided");
                        itemLevelMetadata.add(dataDesc);
                    }
                }

                item.setFileLevelMetadata(itemLevelMetadata);
                DataResourcesMgr.addDataItem(mContext, item, favoriteName);
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(null);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mHandler.onProgressUpdate(values[0]);
    }
}
