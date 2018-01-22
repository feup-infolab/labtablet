package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pt.up.fe.alpha.R;
import pt.up.fe.alpha.labtablet.api.ChangelogManager;
import pt.up.fe.alpha.labtablet.api.DendroAPI;
import pt.up.fe.alpha.labtablet.models.ChangelogItem;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroDescriptor;
import pt.up.fe.alpha.labtablet.models.Descriptor;
import pt.up.fe.alpha.labtablet.utils.FileMgr;
import pt.up.fe.alpha.labtablet.utils.Utils;

/**
 * Loads the descriptor recommendations from the repository for a specific favorite
 */
public class AsyncRecommendationsLoader extends AsyncTask<Object, Integer, ArrayList<Descriptor>> {

    private final AsyncTaskHandler<ArrayList<Descriptor>> mHandler;
    private Exception error;


    public AsyncRecommendationsLoader(AsyncTaskHandler<ArrayList<Descriptor>> mHandler) {
        this.mHandler = mHandler;
    }


    @Override
    protected ArrayList<Descriptor> doInBackground(Object... params) {

        if (params[0] == null || params[1] == null) {
            error = new Exception("Expected Context, String, String; Got nulls");
            return new ArrayList<>();
        }

        if (!(params[0] instanceof Context &&
                params[1] instanceof String)) {
            error = new Exception("Expected Context, String, String; Got "
                    + params[0].getClass() + ", " +
                    params[1].getClass());

            return new ArrayList<>();
        }

        Context mContext = (Context) params[0];
        String projectName = (String) params[1];

        try {
            String cookie = DendroAPI.authenticate(mContext);

            DendroConfiguration conf = FileMgr.getDendroConf(mContext);

            URL url = new URL(conf.getAddress() + "/project/" + projectName + "?metadata_recommendations");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("Accept","application/json");
            conn.setDoInput(true);


            /*DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(conf.getAddress() + "/project/" + projectName + "?metadata_recommendations");
            httpget.setHeader("Accept", "application/json");
            httpget.setHeader("Cookie", "connect.sid=" + cookie);

            HttpResponse resp = httpclient.execute(httpget);
            HttpEntity ent = resp.getEntity();*/


            BufferedReader in = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject respObject = new JSONObject(response.toString());
            ArrayList<DendroDescriptor> recommendedDendroDescriptors =
                    new Gson().fromJson(respObject.get("descriptors").toString(), Utils.ARRAY_DENDRO_DESCRIPTORS);

            ArrayList<Descriptor> recommendedDescriptors = new ArrayList<>();
            for (DendroDescriptor dDesc : recommendedDendroDescriptors) {
                Descriptor desc = new Descriptor();
                desc.setDescriptor(dDesc.getUri());
                desc.setName(dDesc.getShortName());
                desc.setDescription(dDesc.getComment());
                desc.setTag("");
                desc.setValue("");

                recommendedDescriptors.add(desc);
            }

            ChangelogItem log = new ChangelogItem();
            log.setMessage(mContext.getResources().getString(R.string.log_loaded) + " " + projectName + " (" + recommendedDendroDescriptors.size() + ").");
            log.setDate(Utils.getDate());
            log.setTitle(mContext.getResources().getString(R.string.log_loaded));
            ChangelogManager.addLog(log, mContext);

            return recommendedDescriptors;

        } catch (Exception e) {
            error = e;
            return new ArrayList<>();
        }
    }


    @Override
    protected void onPostExecute(ArrayList<Descriptor> result) {
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
