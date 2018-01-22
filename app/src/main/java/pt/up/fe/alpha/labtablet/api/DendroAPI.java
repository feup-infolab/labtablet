package pt.up.fe.alpha.labtablet.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import pt.up.fe.alpha.labtablet.async.AsyncUploader;
import pt.up.fe.alpha.labtablet.database.AppDatabase;
import pt.up.fe.alpha.labtablet.models.Dendro.DendroConfiguration;
import pt.up.fe.alpha.labtablet.models.Dendro.Sync;
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

        System.out.println(conn.getRequestProperties().toString());

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

        return msCookieManager.getCookieStore().getCookies().get(0).toString();
    }


    private static class GetBookmarksTask extends AsyncTask<Void, Void, String>
    {
        private Context context;
        GetBookmarksTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url;
            HttpURLConnection conn;
            String result = "";

            String cookie = null;
            try {
                cookie = authenticate(context);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            try {
                DendroConfiguration conf = FileMgr.getDendroConf(context);
                url = new URL(conf.getAddress() + "/external_repositories/my");
                conn = (HttpURLConnection) url.openConnection();
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

                result = response.toString();

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (ProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String results)
        {
            super.onPostExecute(results);
        }
    }

    public static JsonArray getExportBookmarks(Context context)
    {
        String result = null;
        JsonArray resultAsJsonArray = null;
        try {
            result = new GetBookmarksTask(context).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return resultAsJsonArray;
        }

        JsonParser parser = new JsonParser();
        try{
            JsonArray objArray = parser.parse(result).getAsJsonArray();
            resultAsJsonArray = objArray;
        }
        catch (Exception e)
        {
            JsonObject obj = parser.parse(result).getAsJsonObject();
            resultAsJsonArray.add(obj);
        }

        return resultAsJsonArray;
    }

    public static String executeExportToRepositoryTaskSync(Context context, String folderUri, JsonObject object)
    {
        String result = null;
        try {
            result = new ExportToRepositoryTask(context, folderUri, object).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }


    public static String exportToRepositoryRequest(final Context context, final String folderUri, final JsonObject object)
    {
        String result;
        DendroConfiguration conf = FileMgr.getDendroConf(context);

        String cookie = null;
        try {
            cookie = authenticate(context);
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
            return result;
        }

        URL url = null;
        try {
            //TODO change this because folderUri in this case is already with the baseUrl from dendro
            url = new URL(folderUri + "?export_to_repository");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Cookie", cookie);
            conn.setDoOutput(true);

            System.out.println(conn.getRequestProperties().toString());

            OutputStream os = conn.getOutputStream();
            String g = new Gson().toJson(object);
            os.write(g.getBytes());
            //os.write(object.toString().getBytes());
            //os.write(object.getAsString().getBytes());
            os.flush();

            InputStream error = conn.getErrorStream();

            if(error != null)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader((error)));

                String output;
                StringBuilder response = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    response.append(output);
                    response.append('\r');
                }
                result = response.toString();
                conn.disconnect();
                return result;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
                response.append('\r');
            }
            result = response.toString();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DEBUG ERROR: " + e.getMessage());
            result = e.getMessage();
        }
        return result;
    }


    private static class ExportToRepositoryTask extends AsyncTask<Void, Void, String>{
        private Context context;
        private String folderUri;
        private JsonObject object;

        public ExportToRepositoryTask(Context context, String folderUri, JsonObject object) {
            this.context = context;
            this.folderUri = folderUri;
            this.object = object;
        }

        @Override
        protected String doInBackground(Void... voids) {
            DendroConfiguration conf = FileMgr.getDendroConf(context);
            String result = null;

            String cookie = null;
            try {
                cookie = authenticate(context);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }

            URL url = null;
            try {
                //TODO change this because folderUri in this case is already with the baseUrl from dendro
                url = new URL(folderUri + "?export_to_repository");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setRequestProperty("Cookie", cookie);
                conn.setDoOutput(true);

                System.out.println(conn.getRequestProperties().toString());

                OutputStream os = conn.getOutputStream();
                String g = new Gson().toJson(object);
                os.write(g.getBytes());
                //os.write(object.toString().getBytes());
                //os.write(object.getAsString().getBytes());
                os.flush();

                InputStream error = conn.getErrorStream();

                if(error != null)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader((error)));

                    String output;
                    StringBuilder response = new StringBuilder();
                    while ((output = br.readLine()) != null) {
                        response.append(output);
                        response.append('\r');
                    }
                    result = response.toString();
                    conn.disconnect();
                    return result;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                StringBuilder response = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    response.append(output);
                    response.append('\r');
                }
                result = response.toString();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("DEBUG ERROR: " + e.getMessage());
                result = e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String results)
        {
            super.onPostExecute(results);
        }
    }
}
