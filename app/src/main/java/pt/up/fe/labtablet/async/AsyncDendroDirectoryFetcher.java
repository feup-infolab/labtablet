package pt.up.fe.labtablet.async;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.DendroAPI;
import pt.up.fe.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.labtablet.models.Dendro.DendroFolderItem;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Loads the directory structure from the repository
 */
public class AsyncDendroDirectoryFetcher extends AsyncTask<Object, Integer, ArrayList<DendroFolderItem>> {
    //input, remove, output
    private final AsyncTaskHandler<ArrayList<DendroFolderItem>> mHandler;
    private Exception error;

    public AsyncDendroDirectoryFetcher(AsyncTaskHandler<ArrayList<DendroFolderItem>> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected ArrayList<DendroFolderItem> doInBackground(Object... params) {

        ArrayList<DendroFolderItem> dendroFolderItems = new ArrayList<>();
        if (params[0] == null || params[1] == null) {
            error = new Exception("Params for this asynctaks were not provided");
            return dendroFolderItems;
        } else if (!(params[0] instanceof String || params[1] instanceof Activity)) {
            error = new Exception("Was expecting a String and Context, received" + params[0].getClass() + " and " + params[1].getClass());
            return dendroFolderItems;
        }

        Context mContext = (Context) params[1];

        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            error = new Exception("Dendro configurations were not found");
            return dendroFolderItems;
        }

        DendroConfiguration conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
        String destUri = conf.getAddress();


        HttpResponse response;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        //"http://172.30.29.127:3000/project/" + dirName + "?ls"

        Log.i("getDendroDirs", destUri + "/project/" + params[0] + "?ls");
        String requestString = destUri + "/project/" + params[0] + "?ls";
        requestString = requestString.replace(" ", "%20");

        try {
            String cookie = DendroAPI.authenticate(mContext);
            request.setURI(new URI(requestString));
            request.setHeader("Accept", "application/json");
            request.setHeader("Cookie", "connect.sid=" + cookie);

            response = client.execute(request);

            if (response == null) {
                Log.e("checkIfDirExists", "Failed");
                return dendroFolderItems;
            }

            String result = EntityUtils.toString(response.getEntity());
            JsonParser parser = new JsonParser();
            JsonArray obj = parser.parse(result).getAsJsonArray();

            dendroFolderItems = new Gson().fromJson(
                    obj,
                    Utils.ARRAY_DIRECTORY_LISTING);

            return dendroFolderItems;

        } catch (Exception e) {
            error = e;
            return new ArrayList<>();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<DendroFolderItem> result) {
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
