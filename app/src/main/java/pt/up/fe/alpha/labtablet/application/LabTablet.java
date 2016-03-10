package pt.up.fe.alpha.labtablet.application;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import pt.up.fe.alpha.R;

@ReportsCrashes(
        formUri = "https://nauticast.cloudant.com/acra-labtablet/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "inglookicklysinserewyese",
        formUriBasicAuthPassword = "c5077ecdcdcf0714879bb8112b220ee1220427b2",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.BRAND,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_submitted
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

