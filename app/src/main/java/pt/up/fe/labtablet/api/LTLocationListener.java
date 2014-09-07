package pt.up.fe.labtablet.api;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Created by ricardo on 17-03-2014.
 */
public class LTLocationListener implements LocationListener {

    private Context mContext;
    private String path;
    private String favoriteName;
    private LocationManager locationManager;
    private kmlCreatedInterface mKmlInterface;

    ArrayList<Location> mLocations;

    public LTLocationListener(Context context, String path, String favoriteName, kmlCreatedInterface kmlInterface) {
        mLocations = new ArrayList<Location>();
        this.path = path;
        this.mContext = context;
        this.favoriteName = favoriteName;
        this.mKmlInterface = kmlInterface;
    }

    public int getNumberCollectedLocations() {
        return this.mLocations.size();
    }

    @Override
    public void onLocationChanged(Location loc) {
        Toast.makeText(
                mContext,
                "LNG" + loc.getLongitude() + "LAT" + loc.getLatitude(),
                Toast.LENGTH_SHORT).show();
        Log.v("GPS", "LNG" + loc.getLongitude() + "LAT" + loc.getLatitude());
        mLocations.add(loc);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("GPS", provider + "Disconnected");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("GPS", provider + "Connected");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(mContext, "Status: " + status, Toast.LENGTH_SHORT).show();
    }

    public void notifyCollectStopped() {

        locationManager.removeUpdates(this);
        /*
        if(mLocations.size() == 0)
            return;
        */
        new AsyncKMLCreator(new AsyncTaskHandler<String>() {

            @Override
            public void onSuccess(String result) {
                ChangelogItem log = new ChangelogItem();
                log.setTitle(mContext.getString(R.string.log_added));
                log.setMessage("Geo localization file added");
                ChangelogManager.addLog(log, mContext);
                Toast.makeText(mContext, "Successfuly saved location file.", Toast.LENGTH_SHORT).show();
                Descriptor desc = new Descriptor();
                desc.setFilePath(result);
                desc.setValue(Uri.parse(result).getLastPathSegment());
                desc.setState(Utils.DESCRIPTOR_STATE_NOT_VALIDATED);
                desc.setTag(Utils.GEO_TAGS);
                mKmlInterface.kmlCreated(desc);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("ERR", error.getMessage());
            }

            @Override
            public void onProgressUpdate(int value) {
            }
        }).execute(mLocations, path);

    }

    public void notifyCollectStarted () {

        locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, this);
    }

    public interface kmlCreatedInterface {
        public void kmlCreated(Descriptor kmlDescriptor);
    }

}