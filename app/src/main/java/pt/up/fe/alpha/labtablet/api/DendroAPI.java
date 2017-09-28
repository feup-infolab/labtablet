package pt.up.fe.alpha.labtablet.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pt.up.fe.alpha.labtablet.async.AsyncUploader;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.utils.FileMgr;
import pt.up.fe.alpha.labtablet.utils.Utils;

public class DendroAPI {


    public static String authenticate(Context context) throws Exception{

        DendroConfiguration conf = FileMgr.getDendroConf(context);


        URL url = new URL(conf.getAddress() + "/login");

        final String COOKIES_HEADER = "Set-Cookie";

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept","application/json");
        conn.setDoOutput(true);

        JSONObject json = new JSONObject();
        json.put("username", conf.getUsername());
        json.put("password", conf.getPassword());

        OutputStream os = conn.getOutputStream();
        os.write(json.toString().getBytes());
        os.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = br.readLine()) != null) {
            response.append(output);
            response.append('\r');
        }
        String mes = response.toString();
        conn.disconnect();

        AsyncUploader.DendroResponse loginResponse = new Gson().fromJson(mes, AsyncUploader.DendroResponse.class);

        if (loginResponse.result.equals(Utils.DENDRO_RESPONSE_ERROR)) {
            Log.e("AUTH", loginResponse.result + ": " + loginResponse.message);
            throw new Exception(loginResponse.result + ": " + loginResponse.message);
        }

        CookieManager msCookieManager = new CookieManager();
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }

        return msCookieManager.getCookieStore().getCookies().get(0).getValue();
    }
}
