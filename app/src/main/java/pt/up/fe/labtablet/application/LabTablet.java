package pt.up.fe.labtablet.application;

import android.app.Application;
import android.content.Context;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


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

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }


    public static Context getContext() {
        return mContext;
    }
}

