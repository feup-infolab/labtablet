package pt.up.fe.alpha.labtablet.async;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.api.DendroAPI;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroFolderItem;
import pt.up.fe.alpha.labtablet.utils.Utils;

// params, progress, result
public class AsyncItemMetadataFetcher extends AsyncTask<Object, String, String> {
    private final AsyncTaskHandler<String> mHandler;
    private Exception error;

    public AsyncItemMetadataFetcher(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected String doInBackground(Object... params) {
        String resultOfRequest = "";
        if (params[0] == null || params[1] == null) {
            error = new Exception("Params for this asynctaks were not provided");
            return resultOfRequest;
        } else if (!(params[0] instanceof String || params[1] instanceof Activity)) {
            error = new Exception("Was expecting a String and Context, received" + params[0].getClass() + " and " + params[1].getClass());
            return resultOfRequest;
        }

        Context mContext = (Context) params[1];

        SharedPreferences settings = mContext.getSharedPreferences(mContext.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!settings.contains(Utils.DENDRO_CONFS_ENTRY)) {
            error = new Exception("Dendro configurations were not found");
            return resultOfRequest;
        }

        DendroConfiguration conf = new Gson().fromJson(settings.getString(Utils.DENDRO_CONFS_ENTRY, ""), DendroConfiguration.class);
        String destUri = conf.getAddress();

        Log.i("getDendroItemMetadata", destUri + params[0] + "?metadata");
        String requestString = destUri + params[0] + "?metadata";
        requestString = requestString.replace(" ", "%20");

        try {
            String cookie = DendroAPI.authenticate(mContext);

            URL url = new URL(requestString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Accept","application/json");
            conn.setDoInput(true);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response == null) {
                Log.e("getDendroItemMetadata", "Failed");
                return resultOfRequest;
            }

            String result = response.toString();
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(result).getAsJsonObject();
            return obj.toString();

        } catch (Exception e) {
            error = e;
            return resultOfRequest;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    /*@Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mHandler.onProgressUpdate(values);
    }*/
}
