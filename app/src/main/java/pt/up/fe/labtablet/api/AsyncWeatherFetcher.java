package pt.up.fe.labtablet.api;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URI;

import pt.up.fe.labtablet.utils.Utils;

public class AsyncWeatherFetcher extends AsyncTask<Context, Void, Integer> {
    //input, remove, output
    private AsyncTaskHandler<Integer> mHandler;
    private Exception error;

    public AsyncWeatherFetcher(AsyncTaskHandler<Integer> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected Integer doInBackground(Context... params) {

        HttpResponse response;
        try {
            LocationManager lm = (LocationManager) params[0].getSystemService(Context.LOCATION_SERVICE);
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(crit, true);

            //TODO if the last known location is not known, start an async task to get it
            Location loc = lm.getLastKnownLocation(provider);

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();

            request.setURI(new URI(Utils.WEATHER_URL + "lat=" +
                    loc.getLatitude() +
                    "&lon=" +
                    +loc.getLongitude()
                    + "&units=metric"));

            response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(result);
            JSONObject jsonWeather = jsonResponse.getJSONObject("main");
            return jsonWeather.getInt("temp");

        } catch (Exception e) {
            error = e;
        }
        return 0;
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}