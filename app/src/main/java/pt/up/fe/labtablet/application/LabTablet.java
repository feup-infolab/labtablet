package pt.up.fe.labtablet.application;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;

import pt.up.fe.labtablet.R;



@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        formUri = "http://paginas.fe.up.pt/~rcamorim/labtablet/acra_dump_v.php?vcode=" + 16,
        mode = ReportingInteractionMode.TOAST,
        deleteOldUnsentReportsOnApplicationStart = false,
        additionalSharedPreferences={"LabTablet"},
        forceCloseDialogAfterToast = false,
        resToastText = R.string.app_name
)
public class LabTablet extends Application {

    private static Context mContext;
    //private static LabTablet mInstance;

    public enum TrackerName {
        APP_TRACKER,// Tracker used only in this app.
    }

    private static final String PROPERTY_ID = "UA-39850271-3";
    private static final String TAG = "LabTablet";
    public static int GENERAL_TRACKER = 0;

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }



    public static Context getContext() {
        return mContext;
    }
}

