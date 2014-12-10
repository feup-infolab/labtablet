package pt.up.fe.labtablet.api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.async.AsyncKMLCreator;
import pt.up.fe.labtablet.async.AsyncTaskHandler;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Listener to record the gps coordinates and export them (if any) to
 * the KML file
 */
public class LTLocationListener implements LocationListener {

    final private Context mContext;
    final private String path;
    final private kmlCreatedInterface mKmlInterface;

    private ArrayList<Location> mLocations;
    private LocationManager locationManager;

    public LTLocationListener(Context context, String path, kmlCreatedInterface kmlInterface) {
        mLocations = new ArrayList<Location>();
        this.path = path;
        this.mContext = context;
        this.mKmlInterface = kmlInterface;
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

        if (mLocations.size() == 0)
            return;

        new AsyncKMLCreator(new AsyncTaskHandler<String>() {

            @Override
            public void onSuccess(String result) {
                ChangelogItem log = new ChangelogItem();
                log.setTitle(mContext.getString(R.string.log_added));
                log.setMessage("Geo localization file added");
                ChangelogManager.addLog(log, mContext);
                Toast.makeText(mContext, "Successfully saved location file.", Toast.LENGTH_SHORT).show();
                Descriptor desc = new Descriptor();
                desc.setFilePath(result);
                desc.setValue(Uri.parse(result).getLastPathSegment());
                desc.setState(Utils.DESCRIPTOR_STATE_NOT_VALIDATED);
                desc.setTag(Utils.GEO_TAGS);
                mKmlInterface.kmlCreated(desc);
            }

            @Override
            public void onFailure(Exception error) {

                ChangelogItem item = new ChangelogItem();
                item.setMessage("KML creation: " + error.toString() + "Creating KML files. Is there any storage space left on the device?");
                item.setTitle(mContext.getString(R.string.developer_error));
                item.setDate(Utils.getDate());
                ChangelogManager.addLog(item, mContext);

                Log.e("ERR", error.getMessage());
            }

            @Override
            public void onProgressUpdate(int value) {
            }
        }).execute(mLocations, path);

    }

    public boolean notifyCollectStarted() {

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder
                    .setMessage(mContext.getResources().getString(R.string.gps_disabled))
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.form_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    mContext.startActivity(callGPSSettingIntent);
                                }
                            });
            alertDialogBuilder.setNegativeButton(mContext.getResources().getString(R.string.action_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
            return false;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, this);
        return true;
    }

    public interface kmlCreatedInterface {
        public void kmlCreated(Descriptor kmlDescriptor);
    }

}