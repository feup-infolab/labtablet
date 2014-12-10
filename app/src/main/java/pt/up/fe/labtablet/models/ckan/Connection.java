package pt.up.fe.labtablet.models.ckan;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Connection holds the connection details for this session
 *
 * @author Ross Jones <ross.jones@okfn.org>
 * @version 1.7
 * @since 2012-05-01
 */
public final class Connection {

    private String m_host;
    private int m_port;
    private String _apikey = null;

    public Connection(String host) {
        this(host, 80);
    }

    public Connection(String host, int port) {
        this.m_host = host;
        this.m_port = port;

        try {
            URL u = new URL(this.m_host + ":" + this.m_port + "/api");
        } catch (MalformedURLException mue) {
            Log.e("connection", mue.toString());
        }

    }

    public void setApiKey(String key) {
        this._apikey = key;
    }


    /**
     * Makes a POST request
     * <p/>
     * Submits a POST HTTP request to the CKAN instance configured within
     * the constructor, returning tne entire contents of the response.
     *
     * @param path The URL path to make the POST request to
     * @param data The data to be posted to the URL
     * @return The String contents of the response
     * @throws CKANException if the request fails
     */
    protected String Post(String path, String data)
            throws CKANException {
        URL url;

        try {
            url = new URL(this.m_host + ":" + this.m_port + path);
        } catch (MalformedURLException mue) {
            Log.e("connection", mue.toString());
            return null;
        }

        String body = "";

        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost postRequest = new HttpPost(url.toString());
            postRequest.setHeader("X-CKAN-API-Key", this._apikey);

            StringEntity input = new StringEntity(data);
            input.setContentType("application/json");
            postRequest.setEntity(input);

            HttpResponse response = httpclient.execute(postRequest);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String line;
            while ((line = br.readLine()) != null) {
                body += line;
            }
        } catch (IOException ioe) {
            Log.e("connection", ioe.toString());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        return body;
    }

}






