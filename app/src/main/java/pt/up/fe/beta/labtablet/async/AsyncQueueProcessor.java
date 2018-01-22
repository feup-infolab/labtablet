package pt.up.fe.beta.labtablet.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.api.ChangelogManager;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.models.ChangelogItem;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.models.FavoriteItem;
import pt.up.fe.beta.labtablet.utils.FileMgr;
import pt.up.fe.beta.labtablet.utils.Utils;

/**
 * Process metadata queues after editing or validating metadata records
 */
public class AsyncQueueProcessor extends AsyncTask<Object, Integer, Void> {

    private final AsyncTaskHandler<Void> mHandler;
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
        } else if (!(params[0] instanceof FavoriteItem ||
                params[1] instanceof Activity ||
                params[2] instanceof ArrayList ||
                params[3] instanceof ArrayList)) {
            error = new Exception("Was expecting a String, an Activity and two queues, received" + params[0].getClass() + " and " + params[1].getClass());
            return null;
        }

        Activity mContext = (Activity) params[1];
        ArrayList<Descriptor> deletionQueue = (ArrayList<Descriptor>) params[2];
        ArrayList<Descriptor> migrationQueue = (ArrayList<Descriptor>) params[3];

        //Item to be updated
        FavoriteItem fItem = (FavoriteItem) params[0];
        ArrayList<Descriptor> itemMetadata = fItem.getMetadataItems();
        ArrayList<DataItem> itemData = fItem.getDataItems();

        //Delete descriptors
        if (deletionQueue.size() > 0) {
            for (Descriptor desc : deletionQueue) {
                //remove entries

                int i = itemMetadata.size();
                itemMetadata.remove(desc);
                if (itemMetadata.size() < i) {
                    throw new AssertionError();
                }

                //if there is a file associated, remove it
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
        if (migrationQueue.size() > 0) {
            for (Descriptor desc : migrationQueue) {

                //Remove metadata resource
                itemMetadata.remove(desc);

                String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + mContext.getResources().getString(R.string.app_name)
                        + File.separator + fItem.getTitle() + File.separator
                        + desc.getValue();

                File src = new File(desc.getFilePath());
                File dst = new File(destinationPath);
                try {
                    FileMgr.moveFile(src, dst);
                } catch (Exception e) {
                    error = e;
                }

                //create data item entry
                DataItem dataItem = new DataItem();
                dataItem.setLocalPath(dst.getPath());
                dataItem.setHumanReadableSize(FileMgr.humanReadableByteCount(dst.length(), false));
                dataItem.setMimeType(FileMgr.getMimeType(dst.getPath()));

                ArrayList<Descriptor> itemLevelMetadata = new ArrayList<>();

                ArrayList<Descriptor> loadedDescriptors =
                        FavoriteMgr.getBaseDescriptors(mContext);

                //If additional metadata is available, it should me added here
                for (Descriptor dataDesc : loadedDescriptors) {
                    String tag = dataDesc.getTag();
                    switch (tag) {
                        case Utils.TITLE_TAG:
                            dataDesc.setValue(dst.getName());
                            itemLevelMetadata.add(dataDesc);
                            break;
                        case Utils.CREATED_TAG:
                            dataDesc.setValue("" + new Date());
                            itemLevelMetadata.add(dataDesc);
                            break;
                        case Utils.DESCRIPTION_TAG:
                            dataDesc.setValue("No description provided");
                            itemLevelMetadata.add(dataDesc);
                            break;
                    }
                }
                dataItem.setFileLevelMetadata(itemLevelMetadata);
                itemData.add(dataItem);
            }
        }

        fItem.setDataItems(itemData);
        fItem.setMetadataItems(itemMetadata);
        FavoriteMgr.updateFavoriteEntry(fItem.getTitle(), fItem, mContext);
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
