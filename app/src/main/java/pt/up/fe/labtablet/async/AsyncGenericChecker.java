package pt.up.fe.labtablet.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Checks if there is any generic purpose descriptor in use for a specific favorite
 */
public class AsyncGenericChecker extends AsyncTask<Object, Void, Integer> {

    private final AsyncTaskHandler<Integer> mHandler;

    public AsyncGenericChecker(AsyncTaskHandler<Integer> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected Integer doInBackground(Object... params) {

        if (!(params[0] instanceof Context) ||
                !(params[1] instanceof String)) {
            Log.e("GenericChecker", "Received wrong parameter types");
            return 0;
        }

        Context mContext = (Context) params[0];
        String favoriteName = (String) params[1];

        SharedPreferences settings = mContext.getSharedPreferences(
                mContext.getString(R.string.app_name),
                Context.MODE_PRIVATE);
        if(settings == null) {
            return 0;
        }

        ArrayList<Descriptor> worldDescriptors = new ArrayList<Descriptor>();


        if (favoriteName.equals("")) {
            File file = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + mContext.getString(R.string.app_name));
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    FavoriteItem item = FavoriteMgr.getFavorite(mContext, f.getName());
                    worldDescriptors.addAll(item.getMetadataItems());
                }
            }
            return countGenerics(worldDescriptors);
        }

        //get for this particular one
        return countGenerics(FavoriteMgr
                .getFavorite(mContext, favoriteName)
                .getMetadataItems());
    }

    private int countGenerics(ArrayList<Descriptor> descriptors) {
        int counter = 0;

        for (Descriptor desc : descriptors) {
            if (desc != null
                    && desc.getTag().equals(Utils.GENERIC_TAG)) {
                counter ++;
            }
        }
        return counter;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mHandler.onSuccess(result);
    }

}