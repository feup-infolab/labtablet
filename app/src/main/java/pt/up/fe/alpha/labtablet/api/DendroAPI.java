package pt.up.fe.alpha.labtablet.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import pt.up.fe.alpha.labtablet.async.AsyncUploader;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.utils.FileMgr;
import pt.up.fe.alpha.labtablet.utils.Utils;

public class DendroAPI {


    public static String authenticate(Context context) throws Exception{

        DendroConfiguration conf = FileMgr.getDendroConf(context);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpParams httpParameters = new BasicHttpParams();
        int timeout1 = 10000;
        int timeout2 = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeout1);
        HttpConnectionParams.setSoTimeout(httpParameters, timeout2);
        httpclient.setParams(httpParameters);
        HttpPost httppost = new HttpPost(conf.getAddress() + "/login");
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("username", conf.getUsername());
        json.put("password", conf.getPassword());

        StringEntity se = new StringEntity(json.toString());
        httppost.setEntity(se);

        HttpResponse resp = httpclient.execute(httppost);
        HttpEntity ent = resp.getEntity();
        AsyncUploader.DendroResponse loginResponse = new Gson().fromJson(EntityUtils.toString(ent), AsyncUploader.DendroResponse.class);
        if (loginResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR)) {
            Log.e("AUTH", loginResponse.result + ": " + loginResponse.message);
            throw new Exception(loginResponse.result + ": " + loginResponse.message);
        }
        return httpclient.getCookieStore().getCookies().get(0).getValue();
    }
}
