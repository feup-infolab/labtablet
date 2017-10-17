package pt.up.fe.alpha.labtablet.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.up.fe.alpha.labtablet.api.DendroAPI;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Dendro.ProjectListResponse;
import pt.up.fe.alpha.labtablet.utils.FileMgr;

/**
 * Retrieves the list of projects from the repository
 */
public class AsyncProjectListFetcher extends AsyncTask<Context, Integer, ProjectListResponse> {
    private final AsyncTaskHandler<ProjectListResponse> mHandler;
    private Exception error;

    public AsyncProjectListFetcher(AsyncTaskHandler<ProjectListResponse> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected ProjectListResponse doInBackground(Context... params) {
        if (params[0] == null) {
            error = new Exception("Expected Context, got null");
            return null;
        }

        Context mContext = params[0];

        try {
            String cookie = DendroAPI.authenticate(mContext);

            DendroConfiguration conf = FileMgr.getDendroConf(mContext);

            URL url = new URL(conf.getAddress() + "/projects/my");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Cookie",  cookie);
            conn.setDoInput(true);



            /*HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(conf.getAddress() + "/projects/my");
            httpget.setHeader("Accept", "application/json");
            httpget.setHeader("Cookie", "connect.sid=" + cookie);*/

            System.out.println(conn.getResponseCode() + ": " + conn.getResponseMessage());


            BufferedReader in = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            /*HttpResponse resp = httpclient.execute(httpget);
            HttpEntity ent = resp.getEntity();*/
            return new Gson().fromJson(response.toString(), ProjectListResponse.class);

        } catch (Exception e) {
            System.out.println(e.toString());
            error = e;
            return null;
        }
    }


    @Override
    protected void onPostExecute(ProjectListResponse result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        mHandler.onProgressUpdate(values[0]);
        super.onProgressUpdate(values[0]);
    }
}
